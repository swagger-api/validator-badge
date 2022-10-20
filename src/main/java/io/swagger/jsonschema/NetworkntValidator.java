package io.swagger.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import io.swagger.Utils;
import io.swagger.models.SchemaValidationError;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetworkntValidator extends Validator{


    public boolean supports(Utils.VERSION version) {
        switch(version) {
            case V20:
            case V30:
                return true;
            case V31:
            default:
                return false;
        }
    }
    public JsonSchema buildSchema(JsonNode schemaNode, Utils.VERSION version) throws Exception{
        SpecVersion.VersionFlag v = SpecVersionDetector.detect(schemaNode);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(v);
        return factory.getSchema(schemaNode);
    }

    public List<SchemaValidationError> validate(JsonNode spec, Utils.VERSION version, String location){
        List<SchemaValidationError> output = new ArrayList<>();

        try {
            JsonSchema schema = (JsonSchema) loadSchema(version);
            Set<ValidationMessage> errors = schema.validate(spec);
            for (ValidationMessage m: errors) {
                output.add(new SchemaValidationError(m.getMessage(), "error"));
            }
        } catch (Exception e) {
            LOGGER.error("Error in NetworkNt JSON Schema Validation", e);
        }
        return output;
    }
}
