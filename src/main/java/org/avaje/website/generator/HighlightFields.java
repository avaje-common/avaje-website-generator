package org.avaje.website.generator;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * Used to highlight fields by adding a css class 'field' to fields.
 */
public class HighlightFields {

  private final String withField;
  private final String withEnd;

  private final String startMatch;
  private final String endMatch;

  public HighlightFields() {
    withField = "<span class=\"o\">.</span><span class=\"na field\">";
    withEnd = "</span>";

    startMatch = "<span class=\"o\">.</span><span class=\"na\">";
    endMatch = "</span><span class=\"o\">.</span>";
  }

  public HighlightFields(boolean kotlin) {
    withField = "<span class=\"p\">.</span><span class=\"n field\">";
    withEnd = "</span>";

    startMatch = "<span class=\"p\">.</span><span class=\"n\">";
    endMatch = "</span><span class=\"p\">.</span>";
  }

  /**
   * Add css class 'field' to Pygments html markup.
   */
  public String highlight(String fullHtml) {

    StringReader sr = new StringReader(fullHtml);
    LineNumberReader reader = new LineNumberReader(sr);

    StringBuilder sb = new StringBuilder(fullHtml.length());

    try {
      boolean firstLine = true;
      String line;
      while ((line = reader.readLine()) != null) {
        if (firstLine) {
          firstLine = false;
        } else {
          sb.append('\n');
        }
        sb.append(process(line));
      }

      return sb.toString();

    } catch (IOException e) {
      return fullHtml;
    }
  }

  String process(String line) {

    line = line.replace("<span class=\"n\">var</span>", "<span class=\"k\">var</span>");
    line = line.replace("<span class=\"n\">val</span>", "<span class=\"k\">val</span>");

    if (line.contains("<span class=\"nd\">@")) {
      line = line.replace("<span class=\"n\">", "<span class=\"nx\">");
    }

    StringBuilder sb = new StringBuilder(line.length());
    process(0, line, sb);
    return sb.toString();
  }

  private void process(int at, String line, StringBuilder sb) {

    int pos = line.indexOf(startMatch, at);
    if (pos == -1) {
      sb.append(line.substring(at));
      return;
    }

    int next = pos + startMatch.length() + 1;
    int pos2 = line.indexOf("<", next);
    if (pos2 == -1) {
      sb.append(line.substring(at));
      return;
    }

    int pos3 = line.indexOf(endMatch, next);
    if (pos3 == -1) {
      sb.append(line.substring(at));
      return;
    }

    if (pos2 != pos3) {
      sb.append(line.substring(at, pos2));
      process(pos2, line, sb);

    } else {
      // match
      // append initial from at TO pos
      String val = line.substring(at, pos);
      sb.append(val);
      sb.append(withField);
      int litStart = pos + startMatch.length();
      String literal = line.substring(litStart, pos2);
      sb.append(literal);
      sb.append(withEnd);
      int end = pos3 + withEnd.length();
      process(end, line, sb);
    }
  }
}
