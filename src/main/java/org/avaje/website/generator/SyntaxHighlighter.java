package org.avaje.website.generator;

import org.avaje.freemarker.layout.ContentFilter;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

public class SyntaxHighlighter implements ContentFilter {

  private PythonInterpreter interpreter = new PythonInterpreter();

  private final HighlightFields javaHighlightFields = new HighlightFields();
  private final HighlightFields kotlinHighlightFields = new HighlightFieldsKotlin();

  @Override
  public String filter(String content) {

    boolean usePre = true;
    int start = content.indexOf("<pre content");
    if (start == -1) {
      start = content.indexOf("```");
      usePre = false;
    }
    if (start == -1) {
      return content;
    }

    int endOfStartLine = content.indexOf('\n', start + 3);
    if (endOfStartLine == -1) {
      return content;
    }

    int endOfEnd;
    int end;
    if (usePre) {
      end = content.indexOf("</pre>", endOfStartLine);
      endOfEnd = end + 6;
    } else {
      end = content.indexOf("```", endOfStartLine);
      endOfEnd = end + 3;
    }
    if (end == -1) {
      return content;
    }

    String language = determineLanguage(usePre, content, start, endOfStartLine);

    boolean javaLang = "java".equalsIgnoreCase(language);
    boolean kotlinLang = "kotlin".equalsIgnoreCase(language);

    String rawSource = content.substring(endOfStartLine + 1, end);
    if (javaLang || kotlinLang) {
      rawSource = escapeLtGt(rawSource);
    }

    rawSource = trimRawSource(rawSource);
    String highlightedSource = formatSource(language, rawSource);

    if (javaLang) {
      highlightedSource = javaHighlightFields.highlight(highlightedSource);
    } else if (kotlinLang) {
      highlightedSource = kotlinHighlightFields.highlight(highlightedSource);
    }

    StringBuilder buffer = new StringBuilder(content.length() + 1000);
    buffer.append(content, 0, start);
    buffer.append(highlightedSource);

    String remainder = content.substring(endOfEnd);
    buffer.append(filter(remainder));
    return buffer.toString();
  }

  private String escapeLtGt(String content) {
    content = content.replace("<|","<");
    content = content.replace("|>",">");
    return content;
  }

  protected String trimRawSource(String rawSource) {

    int firstChar = findFirstChar(rawSource);
    if (firstChar == 0) {
      return rawSource;
    }

    try {

      rawSource = rawSource.trim();
      StringBuilder sb = new StringBuilder(rawSource.length());
      LineNumberReader lineReader = new LineNumberReader(new StringReader(rawSource));
      boolean firstLine = true;

      String line;
      while ((line = lineReader.readLine()) != null) {
        if (firstLine) {
          sb.append(line);
          firstLine = false;
        } else {
          sb.append("\n");
          if (line.length() > firstChar) {
            sb.append(line.substring(firstChar));
          } else {
            sb.append(line);
          }
        }
      }
      return sb.toString();

    } catch (IOException e) {
      return rawSource;
    }
  }

  private int findFirstChar(String content) {

    int start = 0;
    if (startsWithNewLine(content)) {
      start = 1;
    }
    for (int i = start; i < content.length(); i++) {
      if (!Character.isWhitespace(content.charAt(i))) {
        return i - start;
      }
    }
    return 0;
  }

  private boolean startsWithNewLine(String content) {
    char ch = content.charAt(0);
    return ch == '\n' || ch == '\r';
  }

  private String determineLanguage(boolean usePre, String content, int start, int endOfStartLine) {

    if (usePre) {
      //"<pre content='asd'>";
      int p0 = start + 14;
      int p1 = content.indexOf('>', p0) - 1;
      String restOfLine = content.substring(p0, p1);
      return restOfLine.trim().toLowerCase();

    } else {
      String restOfLine = content.substring(start + 3, endOfStartLine);
      return restOfLine.trim().toLowerCase();
    }
  }


  private String formatSource(String language, String source) {
    if (language.equals("java")) {
      return formatCode(source, "JavaLexer", "pygments.lexers.jvm", "java");

    } else if (language.equals("kotlin")) {
      return formatCode(source, "KotlinLexer", "pygments.lexers.jvm", "kotlin");

    } else if (language.equals("groovy")) {
      return formatCode(source, "GroovyLexer", "pygments.lexers.jvm", "groovy");

    } else if (language.equals("scala")) {
      return formatCode(source, "ScalaLexer", "pygments.lexers.jvm", "scala");

    } else if (language.equals("json")) {
      return formatCode(source, "JsonLexer", "pygments.lexers.data", "json");

    } else if (language.equals("javascript")) {
      return formatCode(source, "JavascriptLexer", "pygments.lexers.javascript", "javascript");

    } else if (language.equals("sql")) {
      return formatCode(source, "SqlLexer", "pygments.lexers.sql", "sql");

    } else if (language.equals("xml")) {
      return formatCode(source, "XmlLexer", "pygments.lexers.html", "xml");

    } else if (language.equals("properties")) {
      return formatCode(source, "PropertiesLexer", "pygments.lexers.configs", "properties");

    } else if (language.equals("sh")) {
      return formatCode(source, "BashLexer", "pygments.lexers.shell", "sh");

    } else if (language.equals("console")) {
      return formatCode(source, "PyPyLogLexer", "pygments.lexers.console", "console");

    } else if (language.equals("text")) {
      return formatCode(source, "TextLexer", "pygments.lexers.special", "text");

    } else if (language.equals("yml")) {
      return formatCode(source, "YamlLexer", "pygments.lexers.data", "yml");
    }


    return source;
  }

  protected String formatCode(String code, String lexer, String lexerPackage, String language) {

    // Set a variable with the content you want to work with
    interpreter.set("code", code);

    String fromClause = "from " + lexerPackage + " import " + lexer + "\n";

    String command =
      "from pygments import highlight\n"
        + fromClause
        + "from pygments.formatters import HtmlFormatter\n"
        + "\nresult = highlight(code, " + lexer + "(), HtmlFormatter())";

    interpreter.exec(command);

    // Get the result that has been set in a variable
    String codeHighlighted = interpreter.get("result", String.class);

    return "<div class=\"syntax " + language + "\">" + codeHighlighted + "</div>";
  }

}
