package com.wordnik.swagger.services;

import com.wordnik.swagger.util.Json;
import com.wordnik.swagger.util.Yaml;

import com.wordnik.swagger.models.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.commons.io.IOUtils;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.*;
import com.github.fge.jsonschema.core.report.*;
import com.github.fge.jackson.JsonLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.util.*;

import java.security.*;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ValidatorService {
  static long LAST_FETCH = 0;
  static String CACHED_SCHEMA = null;
  static String SCHEMA_FILE = "schema.json";
  static String SCHEMA_URL = "http://swagger.io/v2/schema.json";
  static ObjectMapper JsonMapper = Json.mapper();
  static ObjectMapper YamlMapper = Yaml.mapper();
  Logger LOGGER = LoggerFactory.getLogger(ValidatorService.class);
  private JsonSchema schema;

  static {
    disableSslVerification();

  }

  private static void disableSslVerification() {
    try {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }
          public void checkClientTrusted(X509Certificate[] certs, String authType) { }
          public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        }
      };

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session) {
              return true;
          }
      };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }
  }

  public void validateByUrl(HttpServletRequest request, HttpServletResponse response, String url) {
    if(url == null) {
      fail(response);
    }
    else {
      try {
        String inputDoc = getUrlContents(url);

        if (schema == null) {
          JsonNode schemaObject = JsonMapper.readTree(getSchema());
          JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
          schema = factory.getJsonSchema(schemaObject);
        }

        JsonNode spec = readNode(inputDoc);
        String version = getVersion(spec);
        if(version != null && version.startsWith("\"1.")) {
          upgrade(response);
          return;
        }

        ProcessingReport report = schema.validate(spec);
        if(report.isSuccess()) {
          Swagger swagger = readSwagger(inputDoc);
          if(swagger != null) {
            success(response);
          }
          else fail(response);
        }
        else {
          fail(response);
        }
      }
      catch (Exception e) {
        error(response);
      }
    }
  }

  private String getVersion(JsonNode node) {
    JsonNode version = node.get("swagger");
    if(version != null) {
      return version.toString();
    }
    version = node.get("swaggerVersion");
    if(version != null) {
      return version.toString();
    }
    return null;
  }

  public List<SchemaValidationError> debugByUrl(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
    String content = getUrlContents(url);
    JsonNode schemaObject = JsonMapper.readTree(getSchema());

    List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();

    JsonNode spec = readNode(content);
    String version = getVersion(spec);

    if(version != null && version.startsWith("\"1.")) {
      ProcessingMessage pm = new ProcessingMessage();
      pm.setLogLevel(LogLevel.ERROR);
      pm.setMessage("Deprecated Swagger version.  Please visit http://swagger.io for information on upgrading to Swagger 2.0");
      output.add(new SchemaValidationError(pm.asJson()));
      return output;
    }

    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    JsonSchema schema = factory.getJsonSchema(schemaObject);
    ProcessingReport report = schema.validate(readNode(content));
    ListProcessingReport lp = new ListProcessingReport();
    lp.mergeWith(report);

    if(report.isSuccess()) {
      Swagger swagger = readSwagger(content);
    }

    java.util.Iterator<ProcessingMessage> it = lp.iterator();
    while(it.hasNext()) {
      ProcessingMessage pm = it.next();
      output.add(new SchemaValidationError(pm.asJson()));
    }
    return output;
  }

  public List<SchemaValidationError> debugByContent(HttpServletRequest request, HttpServletResponse response, String content) throws Exception {
    JsonNode schemaObject = JsonMapper.readTree(getSchema());
    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    JsonSchema schema = factory.getJsonSchema(schemaObject);
    ProcessingReport report = schema.validate(readNode(content));
    ListProcessingReport lp = new ListProcessingReport();
    lp.mergeWith(report);

    if(report.isSuccess()) {
      Swagger swagger = readSwagger(content);
      if(swagger != null) {
      }
    }

    List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();
    java.util.Iterator<ProcessingMessage> it = lp.iterator();
    while(it.hasNext()) {
      ProcessingMessage pm = it.next();
      output.add(new SchemaValidationError(pm.asJson()));
    }
    return output;
  }

  private void success(HttpServletResponse response) {
    try {
      String name = "valid.png";
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
      if(is != null) {
        IOUtils.copy(is, response.getOutputStream());
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }

  private void error(HttpServletResponse response) {
    try {
      String name = "error.png";
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
      if(is != null) {
        IOUtils.copy(is, response.getOutputStream());
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }

  private void fail(HttpServletResponse response) {
    try {
      String name = "invalid.png";
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
      if(is != null) {
        IOUtils.copy(is, response.getOutputStream());
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }

  private void upgrade(HttpServletResponse response) {
    try {
      String name = "upgrade.png";
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
      if(is != null) {
        IOUtils.copy(is, response.getOutputStream());
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }

  private String getSchema() throws Exception {
    if(CACHED_SCHEMA != null && (System.currentTimeMillis() - LAST_FETCH) < 600000) {
      return CACHED_SCHEMA;
    }
    try{
      LAST_FETCH = System.currentTimeMillis();
      CACHED_SCHEMA = getUrlContents(SCHEMA_URL);
      return CACHED_SCHEMA;
    }
    catch (Exception e) {
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE);
      BufferedReader in = new BufferedReader(
        new InputStreamReader(is));

      String inputLine;
      StringBuilder contents = new StringBuilder();
      while ((inputLine = in.readLine()) != null)
        contents.append(inputLine);
      in.close();
      LAST_FETCH = System.currentTimeMillis();
      CACHED_SCHEMA = contents.toString();
      return CACHED_SCHEMA;
    }
  }

  private String getUrlContents(String urlString) throws Exception {
    System.setProperty ("jsse.enableSNIExtension", "false");

    URL url = new URL(urlString);

    URLConnection urlc = url.openConnection();
    urlc.setRequestProperty("Accept", "application/json, */*");
    urlc.connect();

    StringBuilder contents = new StringBuilder();
    InputStream in = urlc.getInputStream();
    for(int i = 0;i!= -1;i= in.read()){
      char c = (char)i;
      if(!Character.isISOControl(c))
        contents.append((char)i);
      if(c == '\n') {
        contents.append('\n');
      }
    }
    in.close();

    return contents.toString();
  }

  protected String getRemoteAddr(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-FORWARDED-FOR");  
    if (ipAddress == null) {  
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  private Swagger readSwagger(String text) {
    if(text.trim().startsWith("{")) {
      return JsonMapper.convertValue(readNode(text), Swagger.class);
    }
    else {
      return YamlMapper.convertValue(readNode(text), Swagger.class);
    }
  }

  private JsonNode readNode(String text) {
    try{
      if(text.trim().startsWith("{")) {
        return JsonMapper.readTree(text);
      }
      else {
        return YamlMapper.readTree(text);
      }
    }
    catch (IOException e) {
      return null;
    }    
  }
}