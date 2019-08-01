/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util.xml;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.camunda.bpm.engine.BpmnParseException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;
import org.camunda.bpm.engine.impl.util.io.InputStreamSource;
import org.camunda.bpm.engine.impl.util.io.ResourceStreamSource;
import org.camunda.bpm.engine.impl.util.io.StreamSource;
import org.camunda.bpm.engine.impl.util.io.StringStreamSource;
import org.camunda.bpm.engine.impl.util.io.UrlStreamSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Tom Baeyens
 */
public class Parse extends DefaultHandler {

  private static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  private static final String XXE_PROCESSING = "http://xml.org/sax/features/external-general-entities";

  private static final String JAXP_ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
  private static final String JAXP_ACCESS_EXTERNAL_SCHEMA_SYSTEM_PROPERTY = "javax.xml.accessExternalSchema";
  private static final String JAXP_ACCESS_EXTERNAL_SCHEMA_ALL = "all";

  protected Parser parser;
  protected String name;
  protected StreamSource streamSource;
  protected Element rootElement = null;
  protected List<Problem> errors = new ArrayList<>();
  protected List<Problem> warnings = new ArrayList<>();
  protected String schemaResource;
  protected boolean enableXxeProcessing = true;

  public Parse(Parser parser) {
    this.parser = parser;
  }

  public Parse name(String name) {
    this.name = name;
    return this;
  }

  public Parse sourceInputStream(InputStream inputStream) {
    if (name == null) {
      name("inputStream");
    }
    setStreamSource(new InputStreamSource(inputStream));
    return this;
  }

  public Parse sourceResource(String resource) {
    return sourceResource(resource, null);
  }

  public Parse sourceUrl(URL url) {
    if (name == null) {
      name(url.toString());
    }
    setStreamSource(new UrlStreamSource(url));
    return this;
  }

  public Parse sourceUrl(String url) {
    try {
      return sourceUrl(new URL(url));
    } catch (MalformedURLException e) {
      throw LOG.malformedUrlException(url, e);
    }
  }

  public Parse sourceResource(String resource, ClassLoader classLoader) {
    if (name == null) {
      name(resource);
    }
    setStreamSource(new ResourceStreamSource(resource, classLoader));
    return this;
  }

  public Parse sourceString(String string) {
    if (name == null) {
      name("string");
    }
    setStreamSource(new StringStreamSource(string));
    return this;
  }

  public Parse xxeProcessing(boolean enable) {
    setEnableXxeProcessing(enable);
    return this;
  }

  protected void setStreamSource(StreamSource streamSource) {
    if (this.streamSource != null) {
      throw LOG.multipleSourcesException(this.streamSource, streamSource);
    }
    this.streamSource = streamSource;
  }

  public void setEnableXxeProcessing(boolean enableXxeProcessing) {
    this.enableXxeProcessing = enableXxeProcessing;
  }

  public Parse execute() {
    try {
      InputStream inputStream = streamSource.getInputStream();

      SAXParserFactory saxParserFactory = parser.getSaxParserFactory();
      saxParserFactory.setFeature(XXE_PROCESSING, enableXxeProcessing);
      saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      if (schemaResource == null) { // must be done before parser is created
        saxParserFactory.setNamespaceAware(false);
        saxParserFactory.setValidating(false);
      }

      SAXParser saxParser = parser.getSaxParser();
      try {
        saxParser.setProperty(JAXP_ACCESS_EXTERNAL_SCHEMA, resolveAccessExternalSchemaProperty());
      } catch (Exception e) {
        // ignore unavailable option
        LOG.logAccessExternalSchemaNotSupported(e);
      }
      if (schemaResource != null) {
        saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        saxParser.setProperty(JAXP_SCHEMA_SOURCE, schemaResource);
      }
      saxParser.parse(inputStream, new ParseHandler(this));
    } catch (Exception e) {
      throw LOG.parsingFailureException(name, e);
    }

    return this;
  }

  /*
   * JAXP allows users to override the default value via system properties and a central properties
   * file (see https://docs.oracle.com/javase/tutorial/jaxp/properties/scope.html). However, both
   * are overridden by an explicit configuration in code, as we apply it. Since we want users to
   * customize the value, we take the system property into account. The properties file is not
   * supported at the moment.
   */
  protected String resolveAccessExternalSchemaProperty() {
    String systemProperty = System.getProperty(JAXP_ACCESS_EXTERNAL_SCHEMA_SYSTEM_PROPERTY);

    if (systemProperty != null) {
      return systemProperty;
    } else {
      return JAXP_ACCESS_EXTERNAL_SCHEMA_ALL;
    }
  }

  public Element getRootElement() {
    return rootElement;
  }

  public List<Problem> getProblems() {
    return errors;
  }

  public void addError(SAXParseException e) {
    errors.add(new Problem(e, name));
  }

  public void addError(String errorMessage, Element element) {
    errors.add(new Problem(errorMessage, name, element));
  }

  public void addError(BpmnParseException e) {
    errors.add(new Problem(e, name));
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

  public void addWarning(SAXParseException e) {
    warnings.add(new Problem(e, name));
  }

  public void addWarning(String errorMessage, Element element) {
    warnings.add(new Problem(errorMessage, name, element));
  }

  public boolean hasWarnings() {
    return warnings != null && !warnings.isEmpty();
  }

  public void logWarnings() {
    StringBuilder builder = new StringBuilder();
    for (Problem warning : warnings) {
      builder.append("\n* ");
      builder.append(warning.toString());
    }
    LOG.logParseWarnings(builder.toString());
  }

  public void throwExceptionForErrors() {
    StringBuilder strb = new StringBuilder();
    for (Problem error : errors) {
      strb.append("\n* ");
      strb.append(error.toString());
    }
    throw LOG.exceptionDuringParsing(strb.toString());
  }

  public void setSchemaResource(String schemaResource) {
    SAXParserFactory saxParserFactory = parser.getSaxParserFactory();
    saxParserFactory.setNamespaceAware(true);
    saxParserFactory.setValidating(true);
    try {
      saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    } catch (Exception e) {
      LOG.unableToSetSchemaResource(e);
    }
    this.schemaResource = schemaResource;
  }
}
