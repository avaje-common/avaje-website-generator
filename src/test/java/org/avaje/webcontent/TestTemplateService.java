package org.avaje.webcontent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.avaje.website.generator.TemplateRender;
import org.junit.Assert;
import org.junit.Test;

public class TestTemplateService {

  @Test
  public void test() throws IOException {

    //FileWriter out = new FileWriter("./hello");
    
    StringWriter out = new StringWriter();
    
    File inputDir = new File("./src/test/resources/input");
    TemplateRender templateRender = new TemplateRender(inputDir);
    Map<String,Object> map = new HashMap<>();
    templateRender.render("hello", map, out);
    
    Assert.assertEquals("<b>hello world</b>", out.toString());
    
  }
  
  
  @Test
  public void testIndex() throws IOException {

    FileWriter out = new FileWriter("./index.html");
        
    File inputDir = new File("./src/test/resources/input");
    TemplateRender templateRender = new TemplateRender(inputDir);
    Map<String,Object> map = new HashMap<>();
    
    templateRender.render("index.html", map, out);
        
  }
  
}
