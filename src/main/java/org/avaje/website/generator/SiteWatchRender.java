package org.avaje.website.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Watches a directory structure and renders the pages as events occur.
 */
public class SiteWatchRender {

	public static final Logger log = LoggerFactory.getLogger(SiteWatchRender.class);

	private final Path sourceDirectory;

	private final File destinationDirectory;

	private final TemplateRender renderService;

	/**
	 * Construct with a source directory that is watched and a destination directory that is written
	 * to.
	 */
	public SiteWatchRender(File sourceDirectory, File destinationDirectory) {
		this.sourceDirectory = sourceDirectory.toPath();
		this.destinationDirectory = destinationDirectory;
		this.renderService = new TemplateRender(sourceDirectory);
		this.destinationDirectory.mkdirs();
	}

	/**
	 * Called when watcher detects file events in the source directory.
	 */
	private class Callback implements WatchDirCallback {

		/**
		 * Process the file event.
		 */
		@Override
		public void event(WatchEvent<Path> event, Path child, String eventKind) {

			log.trace("watch processing event:{} file:{}", eventKind, child);

			if (isDelete(eventKind)) {
				processFileDelete(child);

			} else {
				processFile(child);
			}
		}

		private boolean isDelete(String eventKind) {
			return "ENTRY_DELETE".equals(eventKind);
		}
	}

	/**
	 * Register the watch service on the source directory and process any events.
	 * <p>
	 * This event does not return.
	 */
	public void run() throws IOException {

		WatchDir watchDir = new WatchDir(sourceDirectory, true, new Callback(), null);
		watchDir.processEvents();
	}

	/**
	 * Walks the source directory and processes the files.
	 */
	public void render() throws IOException {

		log.info("source:{} dest:{}", sourceDirectory, destinationDirectory);
		FileVisitor<Path> fileProcessor = new DirectoryWalkProcessFile();
		Files.walkFileTree(sourceDirectory, fileProcessor);
	}

	/**
	 * Ignore hidden files (like git files).
	 */
	private boolean isIgnoreFile(Path file) {
		return file.getFileName().toString().startsWith(".");
	}

	/**
	 * Ignore hidden files (like git files).
	 */
	private boolean isIgnoreFile(String templateName) {
		return templateName.startsWith(".") || templateName.startsWith("_");
	}

	/**
	 * A file was deleted so remove it from the destination.
	 */
	private void processFileDelete(Path file) {

		if (isIgnoreFile(file)) {
			return;
		}

		String templateName = sourceDirectory.relativize(file).toString();
		if (isIgnoreFile(templateName)) {
			return;
		}

		if (templateName.endsWith(".ftl")) {
			boolean underscore = templateName.startsWith("_");
			String sub = templateName.substring(0, templateName.length() - 3);
			sub += ".html";
			System.out.println("sub:" + sub);
		}

		try {
			File outFile = new File(destinationDirectory, templateName);
			if (outFile.exists()) {
				if (outFile.isDirectory()) {
					deleteDirectory(outFile.toPath());
					log.debug("... deleted directory {}", outFile.getAbsolutePath());

				} else {
					if (!outFile.delete()) {
						log.error("failed to delete file from destination: " + outFile.getAbsolutePath());
					} else {
						log.debug("... deleted file {}", outFile.getAbsolutePath());
					}
				}
			}
		} catch (IOException e) {
			log.error("Error trying to delete file or directory " + file, e);
		}
	}

	private void deleteDirectory(Path directory) throws IOException {

		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});

	}

	/**
	 * Process a file - copy or process html template.
	 */
	private void processFile(Path file) {

		if (isIgnoreFile(file)) {
			return;
		}

		String templateName = sourceDirectory.relativize(file).toString();
		if (isIgnoreFile(templateName)) {
			return;
		}

		boolean isTemplate = isTemplate(templateName);

		try {
			if (!isTemplate) {
				copyFile(templateName, file);
				return;
			}

			String outName = adjustAsHtml(templateName);

			File outFile = new File(destinationDirectory, outName);
			makeParentDirectories(outFile);

			log.debug("... render template: {}", templateName);

			FileWriter writer = new FileWriter(outFile);

			Map<String, Object> map = new HashMap<>();
			renderService.render(templateName, map, writer);
		} catch (IOException e) {
			log.error("Error processing file " + file, e);
		}
	}

	private void copyFile(String templateName, Path file) throws IOException {

		File outFile = new File(destinationDirectory, templateName);
		makeParentDirectories(outFile);

		// not a template so just copy the file
		if (file.toFile().exists()) {
			try {
				Files.copy(file, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				log.debug("... copy file: {}", templateName);
			} catch (NoSuchFileException e) {
				log.trace("... ignore file not found (IDE removed): {}", templateName);
			}
		}
	}

	private boolean isTemplate(String templateName) {
		String lower = templateName.toLowerCase();
		return lower.endsWith(".html") || lower.endsWith(".ftl");
	}

	private String adjustAsHtml(String templateName) {
		if (templateName.endsWith(".ftl")) {//&& !templateName.startsWith("_")) {
			return templateName.substring(0, templateName.length() - 3) + "html";
		}
		return templateName;
	}

	/**
	 * Make sure all the parent directories exist or created.
	 */
	private boolean makeParentDirectories(File outFile) {
		File parentDir = outFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			log.error("Failed to create parent directories for: " + outFile.getAbsolutePath());
			return false;
		}

		return true;
	}

	private final class DirectoryWalkProcessFile extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

			processFile(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes aAttrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

	}

}
