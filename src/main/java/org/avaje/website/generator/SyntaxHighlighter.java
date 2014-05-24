package org.avaje.website.generator;

import org.avaje.freemarker.layout.ContentFilter;
import org.python.util.PythonInterpreter;

public class SyntaxHighlighter implements ContentFilter {

  private PythonInterpreter interpreter = new PythonInterpreter();

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
    String highlightedSource = formatSource(language, rawSource);

    buffer.append(content.substring(0, start));
    buffer.append(highlightedSource);

    String remainder = content.substring(end + 3);
    buffer.append(filter(remainder));
    return buffer.toString();
  }


  private String determineLanguage(String content, int start, int endOfStartLine) {
    
    String restOfLine = content.substring(start + 3, endOfStartLine);
    return restOfLine.trim().toLowerCase();
  }

  
  private String formatSource(String language, String source) {
    if (language.equals("java")) {
      return formatCode(source, "JavaLexer", "pygments.lexers.jvm", "java");
    
    } else if (language.equals("groovy")) {
      return formatCode(source, "GroovyLexer", "pygments.lexers.jvm", "groovy");
    
    } else if (language.equals("scala")) {
      return formatCode(source, "ScalaLexer", "pygments.lexers.jvm", "scala");
    
    } else if (language.equals("sql")) {
      return formatCode(source, "SqlLexer", "pygments.lexers.sql", "sql");
    
    } else if (language.equals("xml")) {
      return formatCode(source, "XmlLexer", "pygments.lexers.web", "xml");
    
    } else if (language.equals("properties")) {
      return formatCode(source, "PropertiesLexer", "pygments.lexers.text", "properties");
    
    } else if (language.equals("sh")) {
      return formatCode(source, "BashLexer", "pygments.lexers.shell", "sh");
    
    } else if (language.equals("javascript")) {
      return formatCode(source, "JavascriptLexer", "pygments.lexers.web", "javascript");
    }
    
//    if (language.equals("json")) {
//      return formatCode(source, "JsonLexer", "pygments.lexers.web", "json");
//    }

    return source;
  }
  
  private String formatCode(String code, String lexer, String lexerPackage, String language) {

    // Set a variable with the content you want to work with
    interpreter.set("code", code);

    String fromClause = "from "+lexerPackage+" import "+lexer+"\n";
    
    interpreter.exec("from pygments import highlight\n" + fromClause
        //+ "from pygments.lexers.jvm import JavaLexer\n"
        + "from pygments.formatters import HtmlFormatter\n"
        + "\nresult = highlight(code, "+lexer+"(), HtmlFormatter())");

    // Get the result that has been set in a variable
    String codeHighlighted = interpreter.get("result", String.class);
    
    return "<div class=\"syntax "+language+"\">"+codeHighlighted+"</div>";
  }

}
