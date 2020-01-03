package org.avaje.website.generator;

public class HighlightFieldsKotlin extends HighlightFields {

  public HighlightFieldsKotlin() {
    super(true);
  }

  @Override
  String process(String line) {
    line = line.replace("<span class=\"n\">@", "<span class=\"nd\">@");
    return super.process(line);
  }

}