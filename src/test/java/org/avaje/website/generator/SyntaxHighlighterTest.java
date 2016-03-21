package org.avaje.website.generator;

import org.avaje.freemarker.util.IOUtil;
import org.junit.Test;

import java.io.IOException;


public class SyntaxHighlighterTest {

  SyntaxHighlighter highlighter = new SyntaxHighlighter();

  @Test
  public void testFilter() throws Exception {

    String code = "print \"Hello World\"";
    String out = highlighter.formatCode(code, "PythonLexer", "pygments.lexers", "python");

    System.out.println(out);
  }

  @Test
  public void test() throws IOException {

    String content = IOUtil.readUtf8(this.getClass().getResourceAsStream("/input/padded.html"));
    String out = highlighter.filter(content);
    System.out.println(out);
  }
}