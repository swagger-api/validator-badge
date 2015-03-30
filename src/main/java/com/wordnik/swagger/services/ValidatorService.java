package com.wordnik.swagger.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.wordnik.swagger.models.SchemaValidationError;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator service class which is used to validate the swagger
 */
public class ValidatorService {
    private static final String SCHEMA_FILE = "schema.json";
    private static final String SCHEMA_URL = "http://swagger.io/v2/schema.json";
    private static ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = Logger.getLogger(ValidatorService.class);

    /**
     * This method validates the swagger
     *
     * @param request  http Request
     * @param response http response
     * @param url      url of the swagger to be validated
     */
    public void validateByUrl(HttpServletRequest request, HttpServletResponse response, String url) {
        if (url == null) {
            onFailure(response);
        } else {
            try {
                String inputDoc = getUrlContents(url);
                String schemaText = getSchema();
                JsonNode schemaObject = MAPPER.readTree(schemaText);
                JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
                JsonSchema schema = factory.getJsonSchema(schemaObject);
                ProcessingReport report = schema.validate(JsonLoader.fromString(inputDoc));
                if (report.isSuccess())
                    onSuccess(response);
                else
                    onFailure(response);
            } catch (Exception e) {
                onError(response);
            }
        }
    }

    /**
     * This method used to debug by URL
     *
     * @param url url of the swagger to be debug
     * @return validity of the swagger
     * @throws Exception
     */
    public List<SchemaValidationError> debugByUrl(String url) throws Exception {
        String inputDoc = getUrlContents(url);
        return debugByContent(inputDoc);
    }

    /**
     * This method used to debug by content
     *
     * @param content swagger content
     * @return validity of the swagger
     * @throws Exception
     */
    public List<SchemaValidationError> debugByContent(String content) throws Exception {
        String schemaText = getSchema();
        JsonNode schemaObject = MAPPER.readTree(schemaText);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);
        ProcessingReport report = schema.validate(JsonLoader.fromString(content));
        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();
        for (ProcessingMessage pm : lp) {
            output.add(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }

    /**
     * This method displays valid png
     *
     * @param response HttpResponse
     */
    private void onSuccess(HttpServletResponse response) {
        try {
            String name = "valid.png";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * This method displays error png
     *
     * @param response HttpResponse
     */
    private void onError(HttpServletResponse response) {
        try {
            String name = "error.png";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * This method displays invalid png
     *
     * @param response HttpResponse
     */
    private void onFailure(HttpServletResponse response) {
        try {
            String name = "invalid.png";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * This method used to get the schema
     *
     * @return schema
     * @throws Exception
     */
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

    /**
     * This method used to get URL contents
     * @param urlString url
     * @return url content
     * @throws Exception
     */
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
}