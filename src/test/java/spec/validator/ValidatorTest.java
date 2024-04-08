package spec.validator;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.handler.ValidatorController;
import io.swagger.models.ValidationResponse;
import io.swagger.oas.inflector.models.RequestContext;
import io.swagger.oas.inflector.models.ResponseContext;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;


public class ValidatorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    private static final String IMAGE = "image";
    private static final String PNG = "png";
    private static final String VALID_30_YAML = "/valid_oas3.yaml";
    private static final String INVALID_30_YAML ="/invalid_oas3.yaml";
    private static final String VALID_20_YAML = "/valid_swagger2.yaml";
    private static final String INVALID_20_YAML ="/invalid_swagger2.yaml";

    private static final String INVALID_31_YAML ="/invalid_oas31.yaml";
    private static final String INVALID_31_YAML_SIMPLE ="/invalid_oas31_simple.yaml";
    private static final String VALID_31_YAML ="/valid_oas31.yaml";
    private static final String VALID_31_YAML_SIMPLE ="/valid_oas31_simple.yaml";
    private static final String VALID_IMAGE = "valid.png";
    private static final String INVALID_IMAGE = "invalid.png";
    private static final String APPLICATION = "application";
    private static final String JSON = "json";
    private static final String INFO_MISSING = "attribute info is missing";
    private static final String INFO_MISSING_SCHEMA = "object has missing required properties ([\"info\"])";

    protected int serverPort = getDynamicPort();

    private static Logger LOGGER = LoggerFactory.getLogger(ValidatorTest.class);

    @Before
    public void setUpWireMockServer() throws IOException {
        System.setProperty("rejectLocal", "false");
        this.serverPort = wireMockRule.port();
        String pathFile = FileUtils.readFileToString(new File("src/test/resources/valid_oas3.yaml"));

        stubFor(get(urlPathMatching("/valid/yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/invalid_oas3.yaml"));

        stubFor(get(urlPathMatching("/invalid/yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/valid_swagger2.yaml"));

        stubFor(get(urlPathMatching("/validswagger/yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/invalid_swagger2.yaml"));

        stubFor(get(urlPathMatching("/invalidswagger/yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));


        pathFile = FileUtils.readFileToString(new File("src/test/resources/valid_oas31.yaml"));
        stubFor(get(urlPathMatching("/valid/yaml31"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/invalid_oas31.yaml"));

        stubFor(get(urlPathMatching("/invalid/yaml31"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/oas31local/petstore31.yaml"));
        pathFile = pathFile.replace("1337", String.valueOf(this.serverPort));

        stubFor(get(urlPathMatching("/petstore31.yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/oas31local/petstore31ext1.yaml"));
        pathFile = pathFile.replace("1337", String.valueOf(this.serverPort));

        stubFor(get(urlPathMatching("/petstore31ext1.yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/oas31local/petstore31ext2.yaml"));
        pathFile = pathFile.replace("1337", String.valueOf(this.serverPort));

        stubFor(get(urlPathMatching("/petstore31ext2.yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/oas30local/petstore30.yaml"));
        pathFile = pathFile.replace("1337", String.valueOf(this.serverPort));

        stubFor(get(urlPathMatching("/petstore30.yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/oas30local/petstore30ext1.yaml"));
        pathFile = pathFile.replace("1337", String.valueOf(this.serverPort));

        stubFor(get(urlPathMatching("/petstore30ext1.yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));

        pathFile = FileUtils.readFileToString(new File("src/test/resources/oas30local/petstore30ext2.yaml"));
        pathFile = pathFile.replace("1337", String.valueOf(this.serverPort));

        stubFor(get(urlPathMatching("/petstore30ext2.yaml"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-type", "application/yaml")
                        .withBody(pathFile
                                .getBytes(StandardCharsets.UTF_8))));
    }

    private boolean validateEquals(InputStream image1, InputStream image2) throws IOException {

        int i =  image1.read();
        while (-1 != i)
        {
            int y = image2.read();
            if (i != y)
            {
                return false;
            }
            i = image1.read();
        }

        int y = image2.read();
        return(y == -1);
    }

    private static int getDynamicPort() {
        return new Random().ints(10000, 20000).findFirst().getAsInt();
    }


    @Test
    public void testValidateValid20SpecByUrl() throws Exception {

        String url = "http://localhost:${dynamicPort}/validswagger/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(VALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();

    }

    @Test
    public void testValidateValid30SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/valid/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));

        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(VALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();

    }

    @Test
    public void testValidateInvalid30SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/invalid/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));

        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(INVALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();

    }

    @Test
    public void testValidateInvalid20SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/invalidswagger/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));

        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(INVALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();

    }

    @Test
    public void testValidateInvalid30SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(INVALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();


    }

    @Test
    public void testValidateInvalid20SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(INVALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();


    }


    @Test
    public void testValidateValid30SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(VALID_IMAGE);


        assertThat( validateEquals(entity,valid)).isTrue();
    }

    @Test
    public void testValidateValid20SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.validateByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        assertThat(response.getContentType().getType()).isEqualTo(IMAGE);
        assertThat(response.getContentType().getSubtype()).isEqualTo(PNG);
        InputStream entity = (InputStream)response.getEntity();
        InputStream valid = this.getClass().getClassLoader().getResourceAsStream(VALID_IMAGE);

        assertThat( validateEquals(entity,valid)).isTrue();
    }



    @Test
    public void testDebugValid30SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/valid/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));

        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null || validationResponse.getMessages().size() == 0).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages() == null || validationResponse.getSchemaValidationMessages().size() == 0).isTrue();
    }

    @Test
    public void testDebugValid20SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/validswagger/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));


        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null || validationResponse.getMessages().size() == 0).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages() == null || validationResponse.getSchemaValidationMessages().size() == 0).isTrue();
    }

    @Test
    public void testDebugInvalid30SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/invalid/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));

        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals(INFO_MISSING_SCHEMA)).isTrue();

    }

    @Test
    public void testDebugInvalid20SpecByUrl() throws Exception {
        String url = "http://localhost:${dynamicPort}/invalidswagger/yaml";
        url = url.replace("${dynamicPort}", String.valueOf(this.serverPort));

        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByUrl(new RequestContext(), url,                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals(INFO_MISSING_SCHEMA)).isTrue();

    }

    @Test
    public void testDebugInvalid30SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals(INFO_MISSING_SCHEMA)).isTrue();
    }

    @Test
    public void testDebugInvalid20SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals(INFO_MISSING_SCHEMA)).isTrue();
    }

    @Test
    public void testDebugValid20SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null || validationResponse.getMessages().size() == 0).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages() == null || validationResponse.getSchemaValidationMessages().size() == 0).isTrue();
    }

    @Test
    public void testDebugValid30SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        // since inflector 2.0.3 content type is managed by inflector according to request headers and spec
/*
        assertThat(response.getContentType().getType()).isEqualTo(APPLICATION);
        assertThat(response.getContentType().getSubtype()).isEqualTo(JSON);
*/
        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null || validationResponse.getMessages().size() == 0).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages() == null || validationResponse.getSchemaValidationMessages().size() == 0).isTrue();
    }

    @Test
    public void testDebugInvalid31SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_31_YAML_SIMPLE).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                false, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        // assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals("$.info: is missing but it is required")).isTrue();
    }

    @Test
    public void testDebugValid31SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_31_YAML_SIMPLE).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true,
                rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null).isTrue();
    }

    @Test
    public void testParseValid31SpecByContent() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_31_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.parseByContent(new RequestContext(),
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true,
                true,rootNode);

        assertThat("3.1.0").isEqualTo(((JsonNode)response.getEntity()).get("openapi").asText());
    }

    @Test
    public void testDebugInvalid30SpecByContentFge() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals(INFO_MISSING_SCHEMA)).isTrue();
    }

    @Test
    public void testDebugInvalid30SpecByContentNt() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                false, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat("$: required property 'info' not found").isEqualTo(validationResponse.getSchemaValidationMessages().get(0).getMessage());
    }

    @Test
    public void testDebugValid30SpecByContentNt() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                false, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null).isTrue();
        assertThat(0).isEqualTo(validationResponse.getSchemaValidationMessages().size());
    }

    @Test
    public void testDebugValid30SpecByContentFge() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_30_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null).isTrue();
        assertThat(0).isEqualTo(validationResponse.getSchemaValidationMessages().size());
    }

    @Test
    public void testDebugInvalid20SpecByContentFge() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat(validationResponse.getSchemaValidationMessages().get(0).getMessage().equals(INFO_MISSING_SCHEMA)).isTrue();
    }

    @Test
    public void testDebugInvalid20SpecByContentNt() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(INVALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),                 false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                false, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages().contains(INFO_MISSING)).isTrue();
        assertThat("$: required property 'info' not found").isEqualTo(validationResponse.getSchemaValidationMessages().get(0).getMessage());
    }

    @Test
    public void testDebugValid20SpecByContentNt() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                false, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null).isTrue();
        assertThat(0).isEqualTo(validationResponse.getSchemaValidationMessages().size());
    }

    @Test
    public void testDebugValid20SpecByContentFge() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonNode rootNode = mapper.readTree(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(VALID_20_YAML).toURI())));
        ValidatorController validator = new ValidatorController();
        ResponseContext response = validator.reviewByContent(new RequestContext(),
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true, rootNode);

        ValidationResponse validationResponse = (ValidationResponse) response.getEntity();
        assertThat(validationResponse.getMessages() == null).isTrue();
        assertThat(0).isEqualTo(validationResponse.getSchemaValidationMessages().size());
    }

}
