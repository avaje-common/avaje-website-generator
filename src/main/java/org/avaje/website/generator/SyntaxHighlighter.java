package org.avaje.website.generator;

import org.avaje.freemarker.layout.ContentFilter;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

public class SyntaxHighlighter implements ContentFilter {

  private PythonInterpreter interpreter = new PythonInterpreter();

  private final HighlightFields highlightFields = new HighlightFields();

  @Override
  public String filter(String content) {
    
    int start = content.indexOf("```");
    if (start == -1) {
      return content;
    }

    int endOfStartLine = content.indexOf('\n', start + 3);
    if (endOfStartLine == -1) {
      return content;
    }
    
    int end = content.indexOf("```", endOfStartLine);
    if (end == -1) {
      return content;
    }
    
    String language = determineLanguage(content, start, endOfStartLine);

    StringBuilder buffer = new StringBuilder(content.length() + 1000);

    String rawSource = content.substring(endOfStartLine + 1, end);
    rawSource = trimRawSource(rawSource);
    String highlightedSource = formatSource(language, rawSource);
    if ("java".equalsIgnoreCase(language)) {
      highlightedSource = highlightFields.highlight(highlightedSource);
    }

    buffer.append(content.substring(0, start));
    buffer.append(highlightedSource);

    String remainder = content.substring(end + 3);
    buffer.append(filter(remainder));
    return buffer.toString();
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
    return  ch == '\n' || ch == '\r';
  }

  private String determineLanguage(String content, int start, int endOfStartLine) {
    
    String restOfLine = content.substring(start + 3, endOfStartLine);
    return restOfLine.trim().toLowerCase();
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
    
    }

    return source;
  }
  
  protected String formatCode(String code, String lexer, String lexerPackage, String language) {

    // Set a variable with the content you want to work with
    interpreter.set("code", code);

    String fromClause = "from "+lexerPackage+" import "+lexer+"\n";

    String command =
        "from pygments import highlight\n"
        + fromClause
        + "from pygments.formatters import HtmlFormatter\n"
        + "\nresult = highlight(code, "+lexer+"(), HtmlFormatter())";

    interpreter.exec(command);

    // Get the result that has been set in a variable
    String codeHighlighted = interpreter.get("result", String.class);
    
    return "<div class=\"syntax "+language+"\">"+codeHighlighted+"</div>";
  }

}
