package io.swagger.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.swagger.oas.inflector.models.RequestContext;
import io.swagger.oas.inflector.models.ResponseContext;

import io.swagger.parser.SwaggerParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.models.SchemaValidationError;
import io.swagger.models.ValidationResponse;
import org.apache.commons.lang3.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


public class ValidatorController{
    static final String INVALID_VERSION = "Deprecated Swagger version.  Please visit http://swagger.io for information on upgrading to Swagger 2.0\"";
    static final String SCHEMA_FILE = "schema3.json";
    static final String SCHEMA_URL = "https://raw.githubusercontent.com/swagger-api/validator-badge/validator-rc2/src/main/resources/schema3.json";
    static final String SCHEMA2_FILE = "schema.json";
    static final String SCHEMA2_URL = "http://swagger.io/v2/schema.json";

    static Logger LOGGER = LoggerFactory.getLogger(ValidatorController.class);
    static long LAST_FETCH = 0;
    static String CACHED_SCHEMA = null;
    static ObjectMapper JsonMapper = Json.mapper();
    static ObjectMapper YamlMapper = Yaml.mapper();
    private JsonSchema schema;
    static String specVersion = "";


    public ResponseContext validateByUrl(RequestContext request , String url) {

        if(url == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }

        //TODO
        // 1. Get Version of the URL OR CONTENT
        // 2. Call SwaggerParser for 2.x or OpenAPIV3Parser for 3.0 and validate messages of parser
        // 3. Validate JSON SCHEMA accordingly
        // 4. Return answer valid(Image) if messages in parser and JsonSchemaValidator are null or 0
        // 5. Return Invalid(Image) if messages in parser and JsonSchemaValidator are not null and > 0
        // 6. Do the same fot debugByUrl and debugByContent but returning a Json with error messages instead of an image

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByUrl(request, url);
        }catch (Exception e){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }

        if (validationResponse == null){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }

        boolean valid = true;
        List messages = new ArrayList<>();

        if (validationResponse.getMessages() != null) {
            for (String message : validationResponse.getMessages()) {
                if (message != null) {
                    messages.add(message);
                    if(message.endsWith("is unsupported")) {
                        valid = true;
                    }else{
                        valid = false;
                    }
                }
            }
        }
        if (validationResponse.getSchemaValidationMessages() != null) {
            for (SchemaValidationError error : validationResponse.getSchemaValidationMessages()) {
                if (error != null) {
                    messages.add(error.getMessage());
                    valid= false;
                }
            }
        }


        if (valid == true ){
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("valid.png"));
        }else{
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("invalid.png"));
        }

    }

    public ResponseContext reviewByUrl(RequestContext request , String url) {

        if(url == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByUrl(request, url);
        }catch (Exception e){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }

        if (validationResponse == null){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }


        List messages = new ArrayList<>();

        if (validationResponse.getMessages() != null) {
            for (String message : validationResponse.getMessages()) {
                if (message != null) {
                    messages.add(message);
                }
            }
        }
        if (validationResponse.getSchemaValidationMessages() != null) {
            for (SchemaValidationError error : validationResponse.getSchemaValidationMessages()) {
                if (error != null) {
                    messages.add(error.getMessage());
                }
            }
        }

        return new ResponseContext()
                .contentType("application/json")
                .entity(messages);
    }

    public ResponseContext validateByContent(RequestContext request, JsonNode inputSpec) {
        if(inputSpec == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }
        String inputAsString = Json.pretty(inputSpec);

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByContent(request ,inputAsString);
        }catch (Exception e){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }

        if (validationResponse == null){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }


        boolean valid = true;
        List messages = new ArrayList<>();

        if (validationResponse.getMessages() != null) {
            for (String message : validationResponse.getMessages()) {
                if (message != null) {
                    messages.add(message);
                    if(message.endsWith("is unsupported")) {
                        valid = true;
                    }else{
                        valid = false;
                    }
                }
            }
        }
        if (validationResponse.getSchemaValidationMessages() != null) {
            for (SchemaValidationError error : validationResponse.getSchemaValidationMessages()) {
                if (error != null) {
                    messages.add(error.getMessage());
                    valid= false;
                }
            }
        }


        if (valid == true ){
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("valid.png"));
        }else{
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("invalid.png"));
        }
    }

    public ResponseContext reviewByContent(RequestContext request, JsonNode inputSpec) {
        if(inputSpec == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }
        String inputAsString = Json.pretty(inputSpec);

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByContent(request ,inputAsString);
        }catch (Exception e){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }

        if (validationResponse == null){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process URL" );
        }

        List messages = new ArrayList<>();
        if (validationResponse.getMessages() != null) {
            for (String message : validationResponse.getMessages()) {
                if (message != null) {
                    messages.add(message);
                }

            }
        }
        if (validationResponse.getSchemaValidationMessages() != null) {
            for (SchemaValidationError error : validationResponse.getSchemaValidationMessages()) {
                if (error != null) {
                    messages.add(error.getMessage());
                }
            }
        }


        return new ResponseContext()
                .contentType("application/json")
                .entity(messages);


    }




    private String getVersion(JsonNode node) {
        if (node == null) {
            return null;
        }
        JsonNode version = node.get("swagger");
        if (version != null) {
            specVersion = "2.0";
            return version.toString();
        }
        version = node.get("swaggerVersion");
        if (version != null) {
            specVersion = "2.0";
            return version.toString();
        }
        version = node.get("openapi");
        if (version != null) {
            specVersion = "3.0";
            return version.toString();
        }
        LOGGER.debug("version not found!");
        return null;
    }

    public ValidationResponse debugByUrl( RequestContext request, String url) throws Exception {
        ValidationResponse output = new ValidationResponse();
        String content;

        if(StringUtils.isBlank(url)) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("No valid URL specified");
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        // read the spec contents, bail if it fails
        try {
            content = getUrlContents(url);
        } catch (Exception e) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Can't read from file " + url);
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        // convert to a JsonNode

        JsonNode spec = readNode(content);
        if (spec == null) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Unable to read content.  It may be invalid JSON or YAML");
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        // get the version, return deprecated if version 1.x
        String version = getVersion(spec);
        if (version != null && version.startsWith("\"1.")) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage(INVALID_VERSION);
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        // use the swagger deserializer to get human-friendly messages
        if (specVersion.equals("3.0")){
            SwaggerParseResult result = readOpenApi(content);
            if(result != null) {
                for(String message : result.getMessages()) {
                    output.addMessage(message);
                }
            }
        }else if (specVersion.equals("2.0")) {
           SwaggerDeserializationResult result = readSwagger(content);
            if (result != null) {
                for (String message : result.getMessages()) {
                    output.addMessage(message);
                }
            }
        }
        // do actual JSON schema validation
        JsonNode schemaObject = JsonMapper.readTree(getSchema());
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);
        ProcessingReport report = schema.validate(spec);
        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        if (report.isSuccess()) {
            try {
                //readSwagger(content);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("can't read swagger contents", e);

                ProcessingMessage pm = new ProcessingMessage();
                pm.setLogLevel(LogLevel.ERROR);
                pm.setMessage("unable to parse swagger from " + url);
                output.addValidationMessage(new SchemaValidationError(pm.asJson()));
                return output;
            }
        }

        java.util.Iterator<ProcessingMessage> it = lp.iterator();
        while (it.hasNext()) {
            ProcessingMessage pm = it.next();
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }

    public ValidationResponse debugByContent(RequestContext request, String content) throws Exception {

        ValidationResponse output = new ValidationResponse();

        JsonNode spec = readNode(content);

        if (spec == null) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Unable to read content.  It may be invalid JSON or YAML");
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        String version = getVersion(spec);
        if (version != null && version.startsWith("\"1.")) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage(INVALID_VERSION);
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        JsonNode schemaObject = JsonMapper.readTree(getSchema());
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);



        // use the swagger deserializer to get human-friendly messages
        if (specVersion.equals("3.0")){
            SwaggerParseResult result = readOpenApi(content);
            if(result != null) {
                for(String message : result.getMessages()) {
                    output.addMessage(message);
                }
            }
        }else if (specVersion.equals("2.0")) {
            SwaggerDeserializationResult result = readSwagger(content);
            if (result != null) {
                for (String message : result.getMessages()) {
                    output.addMessage(message);
                }
            }
        }

        // do actual JSON schema validation
        ProcessingReport report = schema.validate(spec);
        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        if (report.isSuccess()) {
            try {
                //readSwagger(content);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("can't read swagger contents", e);

                ProcessingMessage pm = new ProcessingMessage();
                pm.setLogLevel(LogLevel.ERROR);
                pm.setMessage("unable to parse swagger from contents");
                output.addValidationMessage(new SchemaValidationError(pm.asJson()));
                return output;
            }
        }

        java.util.Iterator<ProcessingMessage> it = lp.iterator();
        while (it.hasNext()) {
            ProcessingMessage pm = it.next();
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }


    private String getSchema() throws Exception {
        /*if (CACHED_SCHEMA != null && (System.currentTimeMillis() - LAST_FETCH) < 600000) {
            return CACHED_SCHEMA;
        }*/
        try {
            LOGGER.debug("returning cached schema");
            LAST_FETCH = System.currentTimeMillis();
            if (specVersion.equals("3.0")) {
                CACHED_SCHEMA = getUrlContents(SCHEMA_URL);
            }else if (specVersion.equals("2.0")) {
                CACHED_SCHEMA = getUrlContents(SCHEMA2_URL);
            }
            return CACHED_SCHEMA;
        } catch (Exception e) {
            LOGGER.warn("fetching schema from GitHub");
            InputStream is = null;
            if (specVersion.equals("3.0")) {
                is = this.getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE);
            }else if (specVersion.equals("2.0")) {
                is = this.getClass().getClassLoader().getResourceAsStream(SCHEMA2_FILE);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(is));

            String inputLine;
            StringBuilder contents = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                contents.append(inputLine);
            }
            in.close();
            LAST_FETCH = System.currentTimeMillis();
            CACHED_SCHEMA = contents.toString();
            return CACHED_SCHEMA;
        }
    }

    private CloseableHttpClient getCarelessHttpClient() {
        CloseableHttpClient httpClient = null;

        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClients
                    .custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            LOGGER.error("can't disable SSL verification", e);
        }

        return httpClient;
    }

    private String getUrlContents(String urlString) throws IOException {
        LOGGER.trace("fetching URL contents");

        final CloseableHttpClient httpClient = getCarelessHttpClient();

        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder
                .setConnectTimeout(5000)
                .setSocketTimeout(5000);

        HttpGet getMethod = new HttpGet(urlString);
        getMethod.setConfig(requestBuilder.build());
        getMethod.setHeader("Accept", "application/json, */*");


        if (httpClient != null) {
            final CloseableHttpResponse response = httpClient.execute(getMethod);

            try {

                HttpEntity entity = response.getEntity();
                StatusLine line = response.getStatusLine();
                if(line.getStatusCode() > 299 || line.getStatusCode() < 200) {
                    throw new IOException("failed to read swagger with code " + line.getStatusCode());
                }
                return EntityUtils.toString(entity, "UTF-8");
            } finally {
                response.close();
                httpClient.close();
            }
        } else {
            throw new IOException("CloseableHttpClient could not be initialized");
        }
    }

    private SwaggerParseResult readOpenApi(String content) throws IllegalArgumentException {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        return parser.readContents(content, null, null);

    }

    private SwaggerDeserializationResult readSwagger(String content) throws IllegalArgumentException {
        SwaggerParser parser = new SwaggerParser();
        return parser.readWithInfo(content);
    }

    private JsonNode readNode(String text) {
        try {
            if (text.trim().startsWith("{")) {
                return JsonMapper.readTree(text);
            } else {
                return YamlMapper.readTree(text);
            }
        } catch (IOException e) {
            return null;
        }
    }
}
