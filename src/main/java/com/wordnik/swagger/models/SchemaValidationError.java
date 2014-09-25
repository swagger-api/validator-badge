package com.wordnik.swagger.models;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*; 
import com.fasterxml.jackson.databind.node.*;

import java.util.*;

public class SchemaValidationError {
  private String level, domain, keyword, message;
  private Schema schema;
  private Instance instance;
  private List<String> required;
  private List<String> missing;

  public SchemaValidationError() {}
  public SchemaValidationError(JsonNode node) {
    JsonNode prop = node.get("level");
    if(prop != null)
      level = (String) ((TextNode) prop).asText();

    prop = node.get("domain");
    if(prop != null)
      domain = (String) ((TextNode) prop).asText();

    prop = node.get("keyword");
    if(prop != null)
      keyword = (String) ((TextNode) prop).asText();

    prop = node.get("message");
    if(prop != null)
      message = (String) ((TextNode) prop).asText();

    prop = node.get("schema");
    if(prop != null) {
      schema = new Schema();
      JsonNode s = (JsonNode) prop;
      prop = s.get("loadingURI");
      if(prop != null)
        schema.setLoadingURI((String) ((TextNode) prop).asText());
      prop = s.get("pointer");
      if(prop != null)
        schema.setPointer((String) ((TextNode) prop).asText());
    }

    prop = node.get("instance");
    if(prop != null) {
      instance = new Instance();
      JsonNode s = (JsonNode) prop;
      prop = s.get("pointer");
      if(prop != null)
        instance.setPointer((String) ((TextNode) prop).asText());
    }
    
    prop = node.get("required");
    if(prop != null) {
      ArrayNode an = (ArrayNode) prop;
    }

  }

  public String getLevel() {
    return level;
  }
  public void setLevel(String level) {
    this.level = level;
  }

  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getKeyword() {
    return keyword;
  }
  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  public List<String> getRequired() {
    return required;
  }
  public void setRequired(List<String> required) {
    this.required = required;
  }

  public List<String> getMissing() {
    return missing;
  }
  public void setMissing(List<String> missing) {
    this.missing = missing;
  }

  public Schema getSchema() {
    return schema;
  }
  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public Instance getInstance() {
    return instance;
  }
  public void setInstance(Instance instance) {
    this.instance = instance;
  }
}