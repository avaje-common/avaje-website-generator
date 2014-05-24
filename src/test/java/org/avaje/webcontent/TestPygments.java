package org.avaje.webcontent;

import org.junit.Test;
import org.python.util.PythonInterpreter;

public class TestPygments {

  @Test
  public void test() {

    String code = "public class Foo { \n  }";

    PythonInterpreter interpreter = new PythonInterpreter();

    // Set a variable with the content you want to work with
    interpreter.set("code", code);

    // Simple use Pygments as you would in Python
    
    //pygments.lexers.jvm.JavaLexer
    
    interpreter.exec("from pygments import highlight\n" 
        + "from pygments.lexers.jvm import JavaLexer\n"
        + "from pygments.formatters import HtmlFormatter\n"
        + "\nresult = highlight(code, JavaLexer(), HtmlFormatter())");

    // Get the result that has been set in a variable
    String codeHighlighted = interpreter.get("result", String.class);
    System.out.println(codeHighlighted);

  }
}
