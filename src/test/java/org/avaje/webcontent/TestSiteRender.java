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
    
    SiteWatchRender site = new SiteWatchRender(new File("/home/rob/work-avaje/avaje-ebeanorm-website"), new File("/home/rob/work-avaje/site"));
    site.render();
    site.run();
    
  }
  
}
