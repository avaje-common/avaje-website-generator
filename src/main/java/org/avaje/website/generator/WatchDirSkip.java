package org.avaje.website.generator;

import java.nio.file.Path;

/**
 * Provides support for skipping directory structures.
 */
public interface WatchDirSkip {

  /**
   * Return true if this path should be skipped/ignored.
   */
  public boolean skip(Path path);
}
