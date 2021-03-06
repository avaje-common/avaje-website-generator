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
  public void testFilterKotlin() throws Exception {

    String code = "@Entity\n" +
      "class Customer(name : String) {\n" +
      "\n" +
      "  @Id\n" +
      "  var id: Long = 0\n" +
      "\n" +
      "  @Length(100)\n" +
      "  var name: String = name\n" +
      "\n" +
      "}";
    String out = highlighter.formatCode(code, "KotlinLexer", "pygments.lexers.jvm", "kotlin");

    System.out.println(out);
  }

  @Test
  public void test() throws IOException {

    String content = IOUtil.readUtf8(this.getClass().getResourceAsStream("/input/padded.html"));
    String out = highlighter.filter(content);
    System.out.println(out);
  }

  @Test
  public void testUsePre() throws IOException {

    String content = IOUtil.readUtf8(this.getClass().getResourceAsStream("/input/usePre.html"));
    String out = highlighter.filter(content);
    System.out.println(out);
  }
}