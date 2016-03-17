package org.avaje.webcontent;

import java.io.File;
import java.io.IOException;

import org.avaje.website.generator.SiteWatchRender;
import org.junit.Test;

public class TestSiteRender {

  @Test
  public void test() throws IOException {
    
    SiteWatchRender site = new SiteWatchRender(new File("src/test/resources/input"), new File("target/site"));
    site.render();
  }
  
  @Test
  public void testWatcher() throws Exception {

    File source = new File("/home/rob/github/avaje-ebeanorm-website");
    File dest = new File("/home/rob/github/ebean-orm.github.io");

    SiteWatchRender site = new SiteWatchRender(source, dest);
    site.render();
    site.run();
  }
  
}
