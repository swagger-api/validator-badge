package io.swagger.validator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.validator.models.SchemaValidationError;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

public class ValidatorService {
    static Logger LOGGER = LoggerFactory.getLogger(ValidatorService.class);
    static long LAST_FETCH = 0;
    static String CACHED_SCHEMA = null;
    static String SCHEMA_FILE = "schema.json";
    static String SCHEMA_URL = "http://swagger.io/v2/schema.json";
    static ObjectMapper JsonMapper = Json.mapper();
    static ObjectMapper YamlMapper = Yaml.mapper();
    private JsonSchema schema;

    public void validateByUrl(HttpServletRequest request, HttpServletResponse response, String url) {
        LOGGER.info("validationUrl: " + url + ", forClient: " + getRemoteAddr(request));

        if (url == null) {
            LOGGER.debug("no URL passed");
            fail(response);
        } else {
            try {
                String inputDoc = getUrlContents(url);

                if (schema == null) {
                    JsonNode schemaObject = JsonMapper.readTree(getSchema());
                    JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
                    schema = factory.getJsonSchema(schemaObject);
                }

                JsonNode spec = readNode(inputDoc);
                String version = getVersion(spec);
                if (version != null && version.startsWith("\"1.")) {
                    upgrade(response);
                    return;
                }

                ProcessingReport report = schema.validate(spec);
                if (report.isSuccess()) {
                    Swagger swagger = readSwagger(inputDoc);
                    if (swagger != null) {
                        success(response);
                    } else {
                        fail(response);
                    }
                } else {
                    fail(response);
                }
            } catch (Exception e) {
                LOGGER.trace("failed to verify by URL: " + url, e);
                error(response);
            }
        }
    }

    private String getVersion(JsonNode node) {
        if (node == null) {
            return null;
        }
        JsonNode version = node.get("swagger");
        if (version != null) {
            return version.toString();
        }
        version = node.get("swaggerVersion");
        if (version != null) {
            return version.toString();
        }
        LOGGER.debug("version not found!");
        return null;
    }

    public List<SchemaValidationError> debugByUrl(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();

        String content;

        try {
            content = getUrlContents(url);
        } catch (IOException e) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Can't read from file " + url);
            output.add(new SchemaValidationError(pm.asJson()));
            return output;
        }

        JsonNode schemaObject = JsonMapper.readTree(getSchema());

        JsonNode spec = readNode(content);
        if (spec == null) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Unable to read content.  It may be invalid JSON or YAML");
            output.add(new SchemaValidationError(pm.asJson()));
            return output;
        }

        String version = getVersion(spec);

        if (version != null && version.startsWith("\"1.")) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Deprecated Swagger version.  Please visit http://swagger.io for information on upgrading to Swagger 2.0");
            output.add(new SchemaValidationError(pm.asJson()));
            return output;
        }

        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);
        ProcessingReport report = schema.validate(spec);
        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        if (report.isSuccess()) {
            try {
                SwaggerParser parser = new SwaggerParser();
                readSwagger(content);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("can't read swagger contents", e);

                ProcessingMessage pm = new ProcessingMessage();
                pm.setLogLevel(LogLevel.ERROR);
                pm.setMessage("unable to parse swagger from " + url);
                output.add(new SchemaValidationError(pm.asJson()));
                return output;
            }
        }

        java.util.Iterator<ProcessingMessage> it = lp.iterator();
        while (it.hasNext()) {
            ProcessingMessage pm = it.next();
            output.add(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }

    public List<SchemaValidationError> debugByContent(HttpServletRequest request, HttpServletResponse response, String content) throws Exception {
        JsonNode schemaObject = JsonMapper.readTree(getSchema());
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaObject);

        List<SchemaValidationError> output = new ArrayList<SchemaValidationError>();

        JsonNode spec = readNode(content);

        if (spec == null) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Unable to read content.  It may be invalid JSON or YAML");
            output.add(new SchemaValidationError(pm.asJson()));
            return output;
        }

        ProcessingReport report = schema.validate(spec);

        ListProcessingReport lp = new ListProcessingReport();
        lp.mergeWith(report);

        if (report.isSuccess()) {
            Swagger swagger = readSwagger(content);
        }
        java.util.Iterator<ProcessingMessage> it = lp.iterator();
        while (it.hasNext()) {
            ProcessingMessage pm = it.next();
            output.add(new SchemaValidationError(pm.asJson()));
        }
        return output;
    }

    private void success(HttpServletResponse response) {
        writeToResponse(response, "valid.png");
    }

    private void error(HttpServletResponse response) {
        writeToResponse(response, "error.png");
    }

    private void fail(HttpServletResponse response) {
        writeToResponse(response, "invalid.png");
    }

    private void upgrade(HttpServletResponse response) {
        writeToResponse(response, "upgrade.png");
    }

    private void writeToResponse(HttpServletResponse response, String name) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
            if (is != null) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } catch (IOException e) {
            LOGGER.error("can't send response image", e);
        }
    }

    private String getSchema() throws Exception {
        if (CACHED_SCHEMA != null && (System.currentTimeMillis() - LAST_FETCH) < 600000) {
            return CACHED_SCHEMA;
        }
        try {
            LOGGER.debug("returning cached schema");
            LAST_FETCH = System.currentTimeMillis();
            CACHED_SCHEMA = getUrlContents(SCHEMA_URL);
            return CACHED_SCHEMA;
        } catch (Exception e) {
            LOGGER.warn("fetching schema from GitHub");
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE);
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
        // System.setProperty("jsse.enableSNIExtension", "false");
        // System.setProperty("javax.net.debug", "all");

        HttpGet getMethod = new HttpGet(urlString);
        getMethod.setHeader("Accept", "application/json, */*");

        final CloseableHttpClient httpClient = getCarelessHttpClient();

        if (httpClient != null) {
            final CloseableHttpResponse response = httpClient.execute(getMethod);

            try {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            } finally {
                response.close();
                httpClient.close();
            }
        } else {
            throw new IOException("CloseableHttpClient could not be initialized");
        }
    }

    protected String getRemoteAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private Swagger readSwagger(String content) throws IllegalArgumentException {
        SwaggerParser parser = new SwaggerParser();
        SwaggerDeserializationResult output = parser.readWithInfo(content);
        return output.getSwagger();
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