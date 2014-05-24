package org.avaje.website.generator;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import org.avaje.freemarker.ConfigurationBuilder;
import org.avaje.freemarker.layout.InheritLayoutTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Service that provides freemarker template rendering.
 */
public class TemplateRender {

  private static final Logger log = LoggerFactory.getLogger(TemplateRender.class);

  private final Configuration configuration;

  /**
   * Construct with devMode so using template exception handler.
   */
  public TemplateRender(File inputDir) {
    this(inputDir, true, 0);
  }
  
  /**
   * Construct specifying dev mode and updateDelay.
   */
  public TemplateRender(File inputDir, boolean devMode, int updateDelay) {

    ConfigurationBuilder builder = new ConfigurationBuilder();
 
    builder.setUseExceptionHandler(devMode);
    if (!devMode) {
      builder.setTemplateUpdateDelay(updateDelay);
    }

    try {
      FileTemplateLoader fileTemplate = new FileTemplateLoader(inputDir);

      SyntaxHighlighter filter = new SyntaxHighlighter();
      InheritLayoutTemplateLoader templateLoader = new InheritLayoutTemplateLoader(fileTemplate, filter);

      builder.setTemplateLoader(templateLoader);

      this.configuration = builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Render the template with the given model to the writer.
   */
  public void render(String templateName, Map<?, ?> model, Writer writer) throws IOException {

    Template template;
    try {

      template = configuration.getTemplate(templateName);

    } catch (IOException e) {
      throw new IOException("Error loading template: " + templateName, e);
    }

    try {
      
      if (model == null) {
        model = Collections.emptyMap();
      }
      
      SimpleHash wrappedModel = new SimpleHash(model);
      template.process(wrappedModel, writer);

    } catch (TemplateException e) {
      log.error("Error processing template: " + templateName, e);
    }
  }

}
