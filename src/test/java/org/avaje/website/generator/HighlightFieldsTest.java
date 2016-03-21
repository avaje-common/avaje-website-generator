package org.avaje.website.generator;

import org.junit.Test;

public class HighlightFieldsTest {

  HighlightFields highlightFields = new HighlightFields();

  @Test
  public void testHighlight() throws Exception {

    String source = "<span class=\"o\">.</span><span class=\"na\">id</span><span class=\"o\">.</span><span class=\"na\">greaterThan</span><span class=\"o\">(</span><span class=\"mi\">12</span><span class=\"o\">)</span>";
    String val = highlightFields.highlight(source);
    System.out.println(val);
    System.out.println(source);
  }

  @Test
  public void testHighlight2() throws Exception {

    String source = "<span class=\"o\">.</span><span class=\"nb\">id</span><span class=\"o\">.</span><span class=\"na\">greaterThan</span><span class=\"o\">(</span><span class=\"mi\">12</span><span class=\"o\">)</span>";
    String val = highlightFields.highlight(source);
    System.out.println(val);
    System.out.println(source);
  }

  @Test
  public void testHighlight3() throws Exception {

    String source = "<span class=\"o\">.</span><span class=\"na\">id</span><span class=\"o\">.</span><span class=\"na\">greaterThan</span><span class=\"o\">.</span><span class=\"mi\">12</span><span class=\"o\">)</span>";
    String val = highlightFields.highlight(source);
    System.out.println(val);
    System.out.println(source);
  }


  @Test
  public void testHighlight4() throws Exception {

    String source = "foo<span class=\"o\">.</span><span class=\"na\">id</span><span class=\"o\">.</span><span class=\"na\">greaterThan</span><span class=\"o\">.</span><span class=\"mi\">12</span><span class=\"o\">)</span>";
    String val = highlightFields.highlight(source);
    System.out.println(val);
    System.out.println(source);
  }
}