package io.swagger.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import io.swagger.Utils;
import io.swagger.jsonschema.FgeValidator;
import io.swagger.jsonschema.NetworkntValidator;
import io.swagger.jsonschema.Validator;
import io.swagger.models.SchemaValidationError;
import io.swagger.models.Swagger;
import io.swagger.models.ValidationResponse;
import io.swagger.oas.inflector.models.RequestContext;
import io.swagger.oas.inflector.models.ResponseContext;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.InlineModelResolver;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidatorController {

    static final String INVALID_VERSION = "Deprecated Swagger version.  Please visit http://swagger.io for information on upgrading to Swagger/OpenAPI 2.0 or OpenAPI 3.x";
    static Logger LOGGER = LoggerFactory.getLogger(io.swagger.handler.ValidatorController.class);
    static boolean rejectLocal = StringUtils.isBlank(System.getProperty("rejectLocal")) ? true : Boolean.parseBoolean(System.getProperty("rejectLocal"));
    static boolean rejectRedirect = StringUtils.isBlank(System.getProperty("rejectRedirect")) ? true : Boolean.parseBoolean(System.getProperty("rejectRedirect"));

    private Validator fgeValidator = new FgeValidator();
    private Validator ntValidator = new NetworkntValidator();


    public ResponseContext validateByUrl(
            RequestContext request,
            String url,
            Boolean resolve,
            Boolean resolveFully,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation) {

        if(url == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByUrl(
                    request,
                    url,
                    resolve,
                    resolveFully,
                    validateInternalRefs,
                    validateExternalRefs,
                    resolveRequestBody,
                    resolveCombinators,
                    allowEmptyStrings,
                    legacyYamlDeserialization,
                    inferSchemaType,
                    jsonSchemaValidation,
                    legacyJsonSchemaValidation);
        }catch (Exception e){
            return handleFailure("Failed to process URL " + url, e);
        }

        return processValidationResponse(validationResponse);
    }

    public ResponseContext validateByContent(
            RequestContext request,
            Boolean resolve,
            Boolean resolveFully,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation,
            JsonNode inputSpec) {
        if(inputSpec == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }
        String inputAsString = Json.pretty(inputSpec);

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByContent(
                    request,
                    inputAsString,
                    null,
                    resolve,
                    resolveFully,
                    validateInternalRefs,
                    validateExternalRefs,
                    resolveRequestBody,
                    resolveCombinators,
                    allowEmptyStrings,
                    legacyYamlDeserialization,
                    inferSchemaType,
                    jsonSchemaValidation,
                    legacyJsonSchemaValidation);
        }catch (Exception e){
            return handleFailure("Failed to process", e);
        }

        return processValidationResponse(validationResponse);
    }


    private ResponseContext processValidationResponse(ValidationResponse validationResponse) {
        if (validationResponse == null){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process specification" );
        }

        boolean valid = true;
        boolean upgrade = false;
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
                    if (error.getLevel() != null && error.getLevel().toLowerCase().contains("error")) {
                        valid= false;
                    }
                    if (INVALID_VERSION.equals(error.getMessage())) {
                        upgrade = true;
                    }
                }
            }
        }

        if (upgrade == true ){
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("upgrade.png"));
        }else if (valid == true ){
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("valid.png"));
        } else{
            return new ResponseContext()
                    .contentType("image/png")
                    .entity(this.getClass().getClassLoader().getResourceAsStream("invalid.png"));
        }
    }
    public ResponseContext reviewByUrl(
            RequestContext request,
            String url,
            Boolean resolve,
            Boolean resolveFully,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation) {

        if(url == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByUrl(
                    request,
                    url,
                    resolve,
                    resolveFully,
                    validateInternalRefs,
                    validateExternalRefs,
                    resolveRequestBody,
                    resolveCombinators,
                    allowEmptyStrings,
                    legacyYamlDeserialization,
                    inferSchemaType,
                    jsonSchemaValidation,
                    legacyJsonSchemaValidation
            );
        }catch (Exception e){
            return handleFailure("Failed to process specification for " + url, e);
        }

        return new ResponseContext()
                .entity(validationResponse);
        //return processDebugValidationResponse(validationResponse);

    }


    public ResponseContext reviewByContent(
            RequestContext request,
            Boolean resolve,
            Boolean resolveFully,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation,
            JsonNode inputSpec) {
        if(inputSpec == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }
        String inputAsString = Json.pretty(inputSpec);

        ValidationResponse validationResponse = null;
        try {
            validationResponse = debugByContent(
                    request,
                    inputAsString,
                    null,
                    resolve,
                    resolveFully,
                    validateInternalRefs,
                    validateExternalRefs,
                    resolveRequestBody,
                    resolveCombinators,
                    allowEmptyStrings,
                    legacyYamlDeserialization,
                    inferSchemaType,
                    jsonSchemaValidation,
                    legacyJsonSchemaValidation
            );
        }catch (Exception e){
            return handleFailure("Failed to process specification", e);
        }

        return new ResponseContext()
                .entity(validationResponse);
        //return processDebugValidationResponse(validationResponse);
    }

    private ResponseContext processDebugValidationResponse(ValidationResponse validationResponse) {
        if (validationResponse == null){
            return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity( "Failed to process specification" );
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
                .entity(messages);
    }

    public ValidationResponse debugByUrl(
            RequestContext request,
            String url,
            Boolean resolve,
            Boolean resolveFully,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation) throws Exception {
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
            content = Utils.getUrlContents(url, io.swagger.handler.ValidatorController.rejectLocal, io.swagger.handler.ValidatorController.rejectRedirect);
        } catch (Exception e) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Can't read from file " + url);
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        return debugByContent(
                request,
                content,
                url,
                resolve,
                resolveFully,
                validateInternalRefs,
                validateExternalRefs,
                resolveRequestBody,
                resolveCombinators,
                allowEmptyStrings,
                legacyYamlDeserialization,
                inferSchemaType,
                jsonSchemaValidation,
                legacyJsonSchemaValidation);
    }

    public ValidationResponse debugByContent(
            RequestContext request,
            String content,
            String location,
            Boolean resolve,
            Boolean resolveFully,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation) throws Exception {

        ValidationResponse output = new ValidationResponse();

        // convert to a JsonNode

        JsonNode spec = Utils.readNode(content);
        if (spec == null) {
            ProcessingMessage pm = new ProcessingMessage();
            pm.setLogLevel(LogLevel.ERROR);
            pm.setMessage("Unable to read content.  It may be invalid JSON or YAML");
            output.addValidationMessage(new SchemaValidationError(pm.asJson()));
            return output;
        }

        // get the version, return deprecated if version 1.x
        Utils.VERSION version = Utils.getVersion(spec);
        switch(version) {
            case V20:
                SwaggerDeserializationResult result = null;
                try {
                    result = readSwagger(content, resolve, false);
                } catch (Exception e) {
                    LOGGER.debug("can't read Swagger contents", e);

                    ProcessingMessage pm = new ProcessingMessage();
                    pm.setLogLevel(LogLevel.ERROR);
                    pm.setMessage("unable to parse Swagger: " + e.getMessage());
                    output.addValidationMessage(new SchemaValidationError(pm.asJson()));
                    return output;
                }
                if (result != null) {
                    for (String message : result.getMessages()) {
                        output.addMessage(message);
                    }
                }
                break;
            case V30:
            case V31:
                SwaggerParseResult parseResult = null;
                try {
                    parseResult = readOpenApi(
                            content,
                            location,
                            resolve,
                            resolveFully,
                            false,
                            validateInternalRefs,
                            validateExternalRefs,
                            resolveRequestBody,
                            resolveCombinators,
                            allowEmptyStrings,
                            legacyYamlDeserialization,
                            inferSchemaType);
                } catch (Exception e) {
                    LOGGER.debug("can't read OpenAPI contents", e);

                    ProcessingMessage pm = new ProcessingMessage();
                    pm.setLogLevel(LogLevel.ERROR);
                    pm.setMessage("unable to parse OpenAPI: " + e.getMessage());
                    output.addValidationMessage(new SchemaValidationError(pm.asJson()));
                    return output;
                }
                if (parseResult != null) {
                    for (String message : parseResult.getMessages()) {
                        output.addMessage(message);
                    }
                }
                break;
            case NONE:
            case V1:
            default:
                ProcessingMessage pm = new ProcessingMessage();
                pm.setLogLevel(LogLevel.ERROR);
                pm.setMessage(INVALID_VERSION);
                output.addValidationMessage(new SchemaValidationError(pm.asJson()));
                return output;
        }

        // do actual JSON schema validation
        if (Boolean.FALSE.equals(jsonSchemaValidation)) {
            return output;
        }

        if (!Boolean.FALSE.equals(legacyJsonSchemaValidation) && fgeValidator.supports(version)) {
            output.addValidationMessages(fgeValidator.validate(spec, version, location));
        } else {
            if (ntValidator.supports(version)) {
                output.addValidationMessages(ntValidator.validate(spec, version, location));
            }
        }
        return output;
    }

    public ResponseContext parseByUrl(
            RequestContext request,
            String url,
            Boolean resolve,
            Boolean resolveFully,
            Boolean flatten,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation,
            Boolean returnFullParseResult) throws Exception {

        String content;
        if(StringUtils.isBlank(url)) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }

        // read the spec contents, bail if it fails
        try {
            content = Utils.getUrlContents(url, io.swagger.handler.ValidatorController.rejectLocal, io.swagger.handler.ValidatorController.rejectRedirect);
        } catch (Exception e) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "Can't read from " + url );
        }

        try {
            return parseContent(
                    request,
                    content,
                    url,
                    resolve,
                    resolveFully,
                    flatten,
                    validateInternalRefs,
                    validateExternalRefs,
                    resolveRequestBody,
                    resolveCombinators,
                    allowEmptyStrings,
                    legacyYamlDeserialization,
                    inferSchemaType,
                    jsonSchemaValidation,
                    legacyJsonSchemaValidation,
                    returnFullParseResult);
        }catch (Exception e){
            return handleFailure("Failed to process specification for " + url, e);
        }
    }

    public ResponseContext parseByContent(
            RequestContext request,
            Boolean resolve,
            Boolean resolveFully,
            Boolean flatten,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation,
            Boolean returnFullParseResult,
            JsonNode inputSpec) {
        if(inputSpec == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "No specification supplied in either the url or request body.  Try again?" );
        }
        String inputAsString = Json.pretty(inputSpec);

        try {
            return parseContent(
                    request,
                    inputAsString,
                    null,
                    resolve,
                    resolveFully,
                    flatten,
                    validateInternalRefs,
                    validateExternalRefs,
                    resolveRequestBody,
                    resolveCombinators,
                    allowEmptyStrings,
                    legacyYamlDeserialization,
                    inferSchemaType,
                    jsonSchemaValidation,
                    legacyJsonSchemaValidation,
                    returnFullParseResult
            );
        }catch (Exception e){
            return handleFailure("Failed to process specification", e);
        }
    }

    public ResponseContext parseContent(
            RequestContext request,
            String content,
            String location,
            Boolean resolve,
            Boolean resolveFully,
            Boolean flatten,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType,
            Boolean jsonSchemaValidation,
            Boolean legacyJsonSchemaValidation,
            Boolean returnFullParseResult) throws Exception {

        JsonNode spec = Utils.readNode(content);
        if (spec == null) {
            return new ResponseContext()
                    .status(Response.Status.BAD_REQUEST)
                    .entity( "Unable to read content.  It may be invalid JSON or YAML" );
        }

        // get the version, return deprecated if version 1.x
        Utils.VERSION version = Utils.getVersion(spec);
        SwaggerDeserializationResult resultV2 = null;
        SwaggerParseResult resultV3 = null;
        switch(version) {
            case V20:
                try {
                    resultV2 = readSwagger(content, resolve, flatten);
                } catch (Exception e) {
                    LOGGER.debug("can't read Swagger contents", e);

                    return new ResponseContext()
                            .status(Response.Status.BAD_REQUEST)
                            .entity( "unable to parse Swagger: " + e.getMessage() );
                }
                if (resultV2 != null) {
                    for (String message : resultV2.getMessages()) {
                        LOGGER.debug(message);
                    }

                }
                break;
            case V30:
            case V31:
                try {
                    resultV3 = readOpenApi(
                            content,
                            location,
                            resolve,
                            resolveFully,
                            flatten,
                            validateInternalRefs,
                            validateExternalRefs,
                            resolveRequestBody,
                            resolveCombinators,
                            allowEmptyStrings,
                            legacyYamlDeserialization,
                            inferSchemaType);
                } catch (Exception e) {
                    LOGGER.debug("can't read OpenAPI contents", e);
                    return new ResponseContext()
                            .status(Response.Status.BAD_REQUEST)
                            .entity( "unable to parse OpenAPI: " + e.getMessage() );
                }
                if (resultV3 != null) {
                    for (String message : resultV3.getMessages()) {
                        LOGGER.debug(message);
                    }
                }
                break;
            case NONE:
            case V1:
            default:
                return new ResponseContext()
                        .status(Response.Status.BAD_REQUEST)
                        .entity( INVALID_VERSION );
        }

        // do actual JSON schema validation
        if (!Boolean.FALSE.equals(jsonSchemaValidation)) {
            List<SchemaValidationError> errors = null;
            if (!Boolean.FALSE.equals(legacyJsonSchemaValidation) && fgeValidator.supports(version)) {
                errors = fgeValidator.validate(spec, version, location);
            } else {
                errors = ntValidator.validate(spec, version, location);
            }
            if (errors != null) {
                for (SchemaValidationError e : errors) {
                    if (Utils.VERSION.V20.equals(version)) {
                        resultV2.message(e.getMessage());
                    } else {
                        resultV3.message(e.getMessage());
                    }
                }
            }

        }

        String mediaType = "application/json";
        if (request.getAcceptableMediaTypes() != null && request.getAcceptableMediaTypes().contains(new MediaType("application", "yaml"))) {
            mediaType = "application/yaml";
        } else if (request.getAcceptableMediaTypes() != null && request.getAcceptableMediaTypes().contains(new MediaType("application", "octet-stream"))){
            mediaType = "application/octet-stream";
        } else if (request.getAcceptableMediaTypes() != null && request.getAcceptableMediaTypes().contains(new MediaType("text", "plain"))){
            mediaType = "text/plain";
        }
        String specFingerprint = content.trim().substring(0, 1);
        boolean isJson = specFingerprint.startsWith("{") || specFingerprint.startsWith("[");
        String name = "result";
        if (!StringUtils.isBlank(location)) {
            String ext = location.split("\\.")[location.split("\\.").length - 1];
            name = location.split("/")[location.split("/").length - 1];
            name = name.substring(0, name.length() - ext.length() - 1);
        }
        byte[] bytes = null;
        JsonNode resultAsNode = null;
        switch(version) {
            case V20:
                if (resultV2 == null) {
                    return new ResponseContext()
                            .status(Response.Status.NO_CONTENT)
                            .entity( "No result" );
                }
                switch (mediaType) {
                    case "application/yaml":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            resultAsNode = io.swagger.util.Json.mapper().convertValue(resultV2, JsonNode.class);
                        } else {
                            resultAsNode = io.swagger.util.Json.mapper().convertValue(resultV2.getSwagger(), JsonNode.class);
                        }
                        return new ResponseContext()
                                .contentType("application/yaml")
                                .entity(resultAsNode);
                    case "application/octet-stream":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            if (isJson) {
                                bytes = io.swagger.util.Json.pretty(resultV2).getBytes("UTF-8");
                            } else {
                                bytes = io.swagger.util.Yaml.pretty().writeValueAsString(resultV2).getBytes("UTF-8");
                            }
                        } else {
                            if (isJson) {
                                bytes = io.swagger.util.Json.pretty(resultV2.getSwagger()).getBytes("UTF-8");
                            } else {
                                bytes = io.swagger.util.Yaml.pretty().writeValueAsString(resultV2.getSwagger()).getBytes("UTF-8");
                            }
                        }
                        return new ResponseContext().status(200)
                                .entity(bytes)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                                .header("Content-Disposition", String.format("attachment; filename=\"%s-parsed" + (isJson ? ".json" : ".yaml") + "\"", name))
                                .header("Accept-Range", "bytes")
                                .header("Content-Length", String.valueOf(bytes.length));
                    case "text/plain":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            if (isJson) {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(io.swagger.util.Json.pretty(resultV2));
                            } else {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(io.swagger.util.Yaml.pretty().writeValueAsString(resultV2));
                            }
                        } else {
                            if (isJson) {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(io.swagger.util.Json.pretty(resultV2.getSwagger()));
                            } else {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(io.swagger.util.Yaml.pretty().writeValueAsString(resultV2.getSwagger()));
                            }
                        }
                    default:
                        return new ResponseContext()
                                .contentType("application/json")
                                .entity(io.swagger.util.Json.mapper().convertValue(resultV2.getSwagger(), JsonNode.class));
                }
            case V30:
                if (resultV3 == null) {
                    return new ResponseContext()
                            .status(Response.Status.NO_CONTENT)
                            .entity( "No result" );
                }
                switch (mediaType) {
                    case "application/yaml":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            resultAsNode = Json.mapper().convertValue(resultV3, JsonNode.class);
                        } else {
                            resultAsNode = Json.mapper().convertValue(resultV3.getOpenAPI(), JsonNode.class);
                        }
                        return new ResponseContext()
                                .contentType("application/yaml")
                                .entity(resultAsNode);
                    case "application/octet-stream":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            if (isJson) {
                                bytes = Json.pretty(resultV3).getBytes("UTF-8");
                            } else {
                                bytes = Yaml.pretty().writeValueAsString(resultV3).getBytes("UTF-8");
                            }
                        } else {
                            if (isJson) {
                                bytes = Json.pretty(resultV3.getOpenAPI()).getBytes("UTF-8");
                            } else {
                                bytes = Yaml.pretty().writeValueAsString(resultV3.getOpenAPI()).getBytes("UTF-8");
                            }
                        }
                        return new ResponseContext().status(200)
                                .entity(bytes)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                                .header("Content-Disposition", String.format("attachment; filename=\"%s-parsed" + (isJson ? ".json" : ".yaml") + "\"", name))
                                .header("Accept-Range", "bytes")
                                .header("Content-Length", String.valueOf(bytes.length));
                    case "text/plain":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            if (isJson) {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Json.pretty(resultV3));
                            } else {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Yaml.pretty().writeValueAsString(resultV3));
                            }
                        } else {
                            if (isJson) {
                                return new ResponseContext()
                                        .contentType("atext/plain")
                                        .entity(Json.pretty(resultV3.getOpenAPI()));
                            } else {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Yaml.pretty().writeValueAsString(resultV3.getOpenAPI()));
                            }
                        }
                    default:
                        return new ResponseContext()
                                .contentType("application/json")
                                .entity(Json.mapper().convertValue(resultV3.getOpenAPI(), JsonNode.class));
                }
            case V31:
                if (resultV3 == null) {
                    return new ResponseContext()
                            .status(Response.Status.NO_CONTENT)
                            .entity( "No result" );
                }
                switch (mediaType) {
                    case "application/yaml":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            resultAsNode = Json31.mapper().convertValue(resultV3, JsonNode.class);
                        } else {
                            resultAsNode = Json31.mapper().convertValue(resultV3.getOpenAPI(), JsonNode.class);
                        }
                        return new ResponseContext()
                                .contentType("application/yaml")
                                .entity(resultAsNode);
                    case "application/octet-stream":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            if (isJson) {
                                bytes = Json31.pretty(resultV3).getBytes("UTF-8");
                            } else {
                                bytes = Yaml31.pretty().writeValueAsString(resultV3).getBytes("UTF-8");
                            }
                        } else {
                            if (isJson) {
                                bytes = Json31.pretty(resultV3.getOpenAPI()).getBytes("UTF-8");
                            } else {
                                bytes = Yaml31.pretty().writeValueAsString(resultV3.getOpenAPI()).getBytes("UTF-8");
                            }
                        }
                        return new ResponseContext().status(200)
                                .entity(bytes)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                                .header("Content-Disposition", String.format("attachment; filename=\"%s-parsed" + (isJson ? ".json" : ".yaml") + "\"", name))
                                .header("Accept-Range", "bytes")
                                .header("Content-Length", String.valueOf(bytes.length));
                    case "text/plain":
                        if (Boolean.TRUE.equals(returnFullParseResult)) {
                            if (isJson) {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Json31.pretty(resultV3));
                            } else {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Yaml31.pretty().writeValueAsString(resultV3));
                            }
                        } else {
                            if (isJson) {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Json31.pretty(resultV3.getOpenAPI()));
                            } else {
                                return new ResponseContext()
                                        .contentType("text/plain")
                                        .entity(Yaml31.pretty().writeValueAsString(resultV3.getOpenAPI()));
                            }
                        }
                    default:
                        return new ResponseContext()
                                .contentType("application/json")
                                .entity(Json31.mapper().convertValue(resultV3.getOpenAPI(), JsonNode.class));
                }
            default:
                return new ResponseContext()
                        .status(Response.Status.BAD_REQUEST)
                        .entity( INVALID_VERSION );
        }
    }

    private SwaggerParseResult readOpenApi(
            String content,
            String location,
            Boolean resolve,
            Boolean resolveFully,
            Boolean flatten,
            Boolean validateInternalRefs,
            Boolean validateExternalRefs,
            Boolean resolveRequestBody,
            Boolean resolveCombinators,
            Boolean allowEmptyStrings,
            Boolean legacyYamlDeserialization,
            Boolean inferSchemaType) throws IllegalArgumentException {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        ParseOptions p = new ParseOptions();
        p.setResolve(Boolean.TRUE.equals(resolve));
        p.setResolveFully(Boolean.TRUE.equals(resolveFully));
        p.setFlatten(Boolean.TRUE.equals(flatten));
        if (Boolean.FALSE.equals(validateInternalRefs)) p.setValidateInternalRefs(validateInternalRefs);
        p.setValidateExternalRefs(Boolean.TRUE.equals(validateExternalRefs));
        p.setResolveRequestBody(Boolean.TRUE.equals(resolveRequestBody));
        if (Boolean.FALSE.equals(resolveCombinators)) p.setResolveCombinators(resolveCombinators);
        p.setAllowEmptyString(Boolean.TRUE.equals(allowEmptyStrings));
        if (Boolean.FALSE.equals(allowEmptyStrings)) p.setAllowEmptyString(allowEmptyStrings);
        p.setLegacyYamlDeserialization(Boolean.TRUE.equals(legacyYamlDeserialization));
        if (Boolean.FALSE.equals(inferSchemaType)) p.setInferSchemaType(inferSchemaType);
        SwaggerParseResult result = parser.readContents(content, null, p, location);
        clean(result.getOpenAPI());
        return result;

    }
    private SwaggerDeserializationResult readSwagger(String content, Boolean resolve, Boolean flatten) throws IllegalArgumentException {
        SwaggerParser parser = new SwaggerParser();
        SwaggerDeserializationResult output = parser.readWithInfo(content, Boolean.FALSE.equals(resolve) ? false : true);
        if (Boolean.TRUE.equals(flatten)) {
            InlineModelResolver inlineModelResolver = new InlineModelResolver();
            inlineModelResolver.flatten(output.getSwagger());
        }
        return output;
    }


    private void clean(OpenAPI openAPI) {
        if (openAPI.getComponents() != null) {
            if(openAPI.getComponents().getCallbacks() != null && openAPI.getComponents().getCallbacks().isEmpty()) {
                openAPI.getComponents().callbacks(null);
            }
            if(openAPI.getComponents().getRequestBodies() != null && openAPI.getComponents().getRequestBodies().isEmpty()) {
                openAPI.getComponents().requestBodies(null);
            }
            if(openAPI.getComponents().getSchemas() != null && openAPI.getComponents().getSchemas().isEmpty()) {
                openAPI.getComponents().schemas(null);
            }
            if(openAPI.getComponents().getHeaders() != null && openAPI.getComponents().getHeaders().isEmpty()) {
                openAPI.getComponents().headers(null);
            }
            if(openAPI.getComponents().getLinks() != null && openAPI.getComponents().getLinks().isEmpty()) {
                openAPI.getComponents().links(null);
            }
            if(openAPI.getComponents().getExamples() != null && openAPI.getComponents().getExamples().isEmpty()) {
                openAPI.getComponents().examples(null);
            }
            if(openAPI.getComponents().getParameters() != null && openAPI.getComponents().getParameters().isEmpty()) {
                openAPI.getComponents().parameters(null);
            }
            if(openAPI.getComponents().getPathItems() != null && openAPI.getComponents().getPathItems().isEmpty()) {
                openAPI.getComponents().pathItems(null);
            }
            if(openAPI.getComponents().getResponses() != null && openAPI.getComponents().getResponses().isEmpty()) {
                openAPI.getComponents().responses(null);
            }
            if(openAPI.getComponents().getSecuritySchemes() != null && openAPI.getComponents().getSecuritySchemes().isEmpty()) {
                openAPI.getComponents().securitySchemes(null);
            }
            if(openAPI.getComponents().getExtensions() != null && openAPI.getComponents().getExtensions().isEmpty()) {
                openAPI.getComponents().extensions(null);
            }
        }
    }

    private static ResponseContext handleFailure(final String message, final Throwable throwable) {
        LOGGER.error(message, throwable);
        return new ResponseContext().status(Response.Status.INTERNAL_SERVER_ERROR).entity(convertThrowableToJsonString(message, throwable));
    }

    private static String convertThrowableToJsonString(final String message, final Throwable throwable) {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("stacktrace", getStackTrace(throwable));
        return convertToJson(map);
    }

    private static String convertToJson(Object object) {
        StringWriter stringWriter = new StringWriter();
        try {
            io.swagger.v3.core.util.Json.mapper().writeValue(stringWriter, object);
        } catch (IOException e) {
            LOGGER.error("Could not convert object data to json.", e);
        }
        return stringWriter.toString();
    }

    private static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
