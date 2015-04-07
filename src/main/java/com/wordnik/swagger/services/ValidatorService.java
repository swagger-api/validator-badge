package com.wordnik.swagger.services;

import com.wordnik.swagger.util.Json;

import com.wordnik.swagger.models.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;

import com.github.fge.jsonschema.main.*;
import com.github.fge.jsonschema.core.report.*;
import com.github.fge.jackson.JsonLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.servlet.http.*;

public class ValidatorService {
    static String SCHEMA_FILE = "schema.json";
    static String SCHEMA_URL = "http://swagger.io/v2/schema.json";
    static ObjectMapper MAPPER = new ObjectMapper();
    Logger LOGGER = LoggerFactory.getLogger(ValidatorService.class);
    private JsonSchema schema;

    public void validateByUrl(HttpServletRequest request, HttpServletResponse response, String url) {
        System.out.println("validationUrl: " + url + ", forClient: " + getRemoteAddr(request) + ", method: get");
        if (url == null) {
            fail(response);
        } else {
            try {
                String inputDoc = getUrlContents(url);

                if (schema == null) {
                    String schemaText = getSchema();
                    JsonNode schemaObject = MAPPER.readTree(schemaText);
                    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
                    schema = factory.getJsonSchema(schemaObject);
                }
                ProcessingReport report = schema.validate(JsonLoader.fromString(inputDoc));
                if (report.isSuccess()) {
                    Swagger swagger = Json.mapper().readValue(inputDoc, Swagger.class);
                    if (swagger != null) {
                        System.out.println("swaggerHost: " + swagger.getHost() + ", forClient: " + getRemoteAddr(request));
                    }
                    success(response);
                } else
                    fail(response);
            } catch (Exception e) {
                error(response);
            }
        }
    }

    public List<SchemaValidationError> debugByUrl(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        System.out.println("validationUrl: " + url + ", forClient: " + getRemoteAddr(request) + ", method: get, debug: true");
        String content = getUrlContents(url);
        String schemaText = getSchema();
        JsonNode schemaObject = MAPPER.readTree(schemaText);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);
        ProcessingReport report = schema.validate(JsonLoader.fromString(content));
        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        if (report.isSuccess()) {
            Swagger swagger = Json.mapper().readValue(content, Swagger.class);
            if (swagger != null) {
                System.out.println("swaggerHost: " + swagger.getHost() + ", forClient: " + getRemoteAddr(request));
            }
        }

        List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();
        java.util.Iterator<ProcessingMessage> it = lp.iterator();
        while (it.hasNext()) {
            ProcessingMessage pm = it.next();
            output.add(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }

    public List<SchemaValidationError> debugByContent(HttpServletRequest request, HttpServletResponse response, String content) throws Exception {
        System.out.println("validationUrl: n/a, forClient: " + getRemoteAddr(request) + ", method: post, debug: true");
        String schemaText = getSchema();
        JsonNode schemaObject = MAPPER.readTree(schemaText);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);
        ProcessingReport report = schema.validate(JsonLoader.fromString(content));
        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        if (report.isSuccess()) {
            Swagger swagger = Json.mapper().readValue(content, Swagger.class);
            if (swagger != null) {
                System.out.println("swaggerHost: " + swagger.getHost() + ", forClient: " + getRemoteAddr(request));
            }
        }

        List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();
        java.util.Iterator<ProcessingMessage> it = lp.iterator();
        while (it.hasNext()) {
            ProcessingMessage pm = it.next();
            output.add(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }

    private void success(HttpServletResponse response) {
        try {
            String name = "valid.png";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void error(HttpServletResponse response) {
        try {
            String name = "error.png";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fail(HttpServletResponse response) {
        try {
            String name = "invalid.png";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSchema() throws Exception {
        try {
            return getUrlContents(SCHEMA_URL);
        } catch (Exception e) {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(is));

            String inputLine;
            StringBuilder contents = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                contents.append(inputLine);
            in.close();
            return contents.toString();
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

    protected String getRemoteAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }
}