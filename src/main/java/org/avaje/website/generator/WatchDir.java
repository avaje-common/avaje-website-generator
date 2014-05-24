package org.avaje.website.generator;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

  private static final Logger log = LoggerFactory.getLogger(WatchDir.class);
  
  private final WatchService watcher;
  
  private final Map<WatchKey, Path> keys;
  
  private final boolean recursive;
  
  private boolean trace;

  private final WatchDirCallback callback;
  
  private final WatchDirSkip skipDir;
  
  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  /**
   * Register the given directory with the WatchService
   */
  private void register(Path dir) throws IOException {
    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    if (trace) {
      Path prev = keys.get(key);
      if (prev == null) {
        log.info("register: {}", dir);
      } else {
        if (!dir.equals(prev)) {
          log.info("update: {} -> {}", prev, dir);
        }
      }
    }
    keys.put(key, dir);
  }

  /**
   * Register the given directory, and all its sub-directories, with the WatchService.
   */
  private void registerAll(final Path start) throws IOException {
    
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        
        if (skipDir != null && skipDir.skip(dir)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        
        register(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Creates a WatchService and registers the given directory
   * 
   * <p>No WatchDirSkip specified.
   */
  public WatchDir(Path dir, boolean recursive, WatchDirCallback callback) throws IOException {
    this(dir, recursive, callback, null);
  }
  
  /**
   * Creates a WatchService and registers the given directory
   */
  public WatchDir(Path dir, boolean recursive, WatchDirCallback callback, WatchDirSkip skipDir) throws IOException {
    
    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<WatchKey, Path>();
    this.recursive = recursive;
    this.callback = callback;
    this.skipDir = skipDir;

    if (recursive) {
      log.info("Scanning {} ...", dir);
      registerAll(dir);
      log.info("Watching...");
    } else {
      register(dir);
    }

    // enable trace after initial registration
    this.trace = true;
  }

  /**
   * Process all events for keys queued to the watcher
   */
  public void processEvents() {
    for (;;) {

      // wait for key to be signalled
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException x) {
        return;
      }

      Path dir = keys.get(key);
      if (dir == null) {
        log.error("WatchKey {} not recognized!!", key);
        continue;
      }

      for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
          log.error("OVERFLOW event is not handled");
          continue;
        }

        // Context for directory entry event is the file name of entry
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        // print out event
        if (callback != null) {
          callback.event(ev, child, event.kind().toString());
          
        } else {
         log.info(" event: {} {}", event.kind().name(), child);          
        }

        // if directory is created, and watching recursively, then
        // register it and its sub-directories
        if (recursive && (kind == ENTRY_CREATE)) {
          try {
            if (skipDir != null && skipDir.skip(child)) {
              
            } else {
              if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                registerAll(child);
              }
            }
          } catch (IOException e) {
            log.error("Error registering new directory", e);
          }
        }
      }

      // reset key and remove from set if directory no longer accessible
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);

        // all directories are inaccessible
        if (keys.isEmpty()) {
          break;
        }
      }
    }
  }

  static void usage() {
    System.err.println("usage: java WatchDir [-r] dir");
    System.exit(-1);
  }

  public static void main(String[] args) throws IOException {
    // parse arguments
    if (args.length == 0 || args.length > 2) {
      usage();
    }
    boolean recursive = false;
    int dirArg = 0;
    if (args[0].equals("-r")) {
      if (args.length < 2) {
        usage();
      }
      recursive = true;
      dirArg++;
    }

    // register directory and process its events
    Path dir = Paths.get(args[dirArg]);
    new WatchDir(dir, recursive, null, null).processEvents();
  }
}