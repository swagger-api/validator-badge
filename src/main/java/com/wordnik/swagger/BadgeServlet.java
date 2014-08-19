package com.wordnik.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.*;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jackson.JsonLoader;

import java.net.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class BadgeServlet extends HttpServlet {
  static String SCHEMA_FILE = "https://raw.githubusercontent.com/reverb/swagger-spec/master/schemas/v2.0/schema.json";
  static ObjectMapper MAPPER = new ObjectMapper();
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse resp) {
    String url = request.getParameter("url");
    try {
      if(url == null) {
        fail(resp);
      }
      else {
        String inputDoc = getUrlContents(url);
        String schemaText = getUrlContents(SCHEMA_FILE);
        JsonNode schemaObject = MAPPER.readTree(schemaText);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);
        ProcessingReport report = schema.validate(JsonLoader.fromString(inputDoc));
        System.out.println("success: " + report.isSuccess() + " " + url);
        if(report.isSuccess())
          success(resp);
        else
          fail(resp);
      }
    }
    catch (Exception e) {
      try{
        error(resp);
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  private String getUrlContents(String urlString) throws Exception {
    URL url = new URL(urlString);
    BufferedReader in = new BufferedReader(
    new InputStreamReader(url.openStream()));

    String inputLine;
    StringBuilder contents = new StringBuilder();
    while ((inputLine = in.readLine()) != null)
      contents.append(inputLine);
    in.close();
    return contents.toString();
  }

  private void success(HttpServletResponse resp) throws IOException {
    resp.sendRedirect("valid.png");
  }

  private void error(HttpServletResponse resp) throws IOException {
    resp.sendRedirect("error.png");
  }

  private void fail(HttpServletResponse resp) throws IOException {
    resp.sendRedirect("invalid.png");
  }
}