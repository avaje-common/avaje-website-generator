package org.avaje.website.generator;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) throws IOException {
    
    // default to load from system properties
    String source = System.getProperty("source");
    String dest = System.getProperty("dest");

    if (args.length == 2) {
      // assuming sourceDirectory destinationDirectory
      source = args[0];
      dest = args[1];
      
    } else if (args.length > 2){
      // using -source/-s  and -dest/-d argument keys
      Map<String, String> argMap = parseArgs(args);
      source = argMap.get("source");
      dest = argMap.get("dest");
    }

    if (source == null || dest == null) {
      printUsage(source, dest);
      return;
    }
    
    source = source.trim();
    dest = dest.trim();
    
    File sourceDir = new File(source);
    if (!sourceDir.exists()) {
      printError("source directory "+source+" does not exist?");
      System.exit(0);
    }
    if (!sourceDir.isDirectory()) {
      printError("source "+source+" is not a directory?");
      System.exit(0);
    }
    
    File destDir = new File(dest);
    if (!destDir.exists()) {
      if (!destDir.mkdirs()) {
        printError("failed to create destination directory "+dest);
        System.exit(0);        
      }
    } else if (!destDir.isDirectory()) {
      printError("destination "+dest+" is not a directory?");
      System.exit(0);              
    }
    
    SiteWatchRender site = new SiteWatchRender(sourceDir, destDir);
    site.render();
    site.run();
  }
  
  private static void printError(String msg) {
    System.err.println(msg);
  }

  private static Map<String,String> parseArgs(String[] args) {
    
    Map<String,String> argMap = new LinkedHashMap<>();
    
    for (int i = 0; i < args.length; i++) {
      if (isSource(args[i])) {
        argMap.put("source", args[++i]);
      } else if (isDest(args[i])) {
        argMap.put("dest", args[++i]);
      }
    }
    return argMap;
  }
  
  private static boolean isSource(String arg) {
    return "-source".equalsIgnoreCase(arg) || "-s".equalsIgnoreCase(arg);
  }

  private static boolean isDest(String arg) {
    return "-dest".equalsIgnoreCase(arg) || "-d".equalsIgnoreCase(arg);
  }
  
  private static void printUsage(String source, String dest) {
    String noSource = (source != null)? "" : "no source specified";
    String noDest = (dest != null) ? "" : " no destination specified";
    System.out.println("error: "+noSource+noDest);
    System.out.println("usage: -s <source directory> -d <destination directory>");
  }

}
