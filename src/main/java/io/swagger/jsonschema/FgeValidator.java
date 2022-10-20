package io.swagger.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.swagger.Utils;
import io.swagger.models.SchemaValidationError;
import java.util.ArrayList;
import java.util.List;

public class FgeValidator extends Validator {

    static final String SCHEMA30_FILE_FGE = "schemas/30/schema3-fix-format-uri-reference.json";
    static final String SCHEMA20_FILE_FGE = "schemas/20/schema.json";

    public FgeValidator() {
        super();
        schemaLocations.get(Utils.VERSION.V30).path = SCHEMA30_FILE_FGE;
        schemaLocations.get(Utils.VERSION.V20).path = SCHEMA20_FILE_FGE;
        schemaLocations.get(Utils.VERSION.V30).url = null;
    }

    public boolean supports(Utils.VERSION version) {
        switch(version) {
            case V20:
            case V30:
                return true;
            default:
                return false;
        }
    }
    public JsonSchema buildSchema(JsonNode schemaNode, Utils.VERSION version) throws Exception{
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        return factory.getJsonSchema(schemaNode);
    }

    public JsonNode customizeSchema(JsonNode schemaNode, Utils.VERSION version) {
        ObjectNode oNode = (ObjectNode) schemaNode;
        if (Utils.VERSION.V30.equals(version)) {
            if (oNode.get("id") != null) {
                oNode.remove("id");
            }
            if (oNode.get("$schema") != null) {
                oNode.remove("$schema");
            }
            if (oNode.get("description") != null) {
                oNode.remove("description");
            }
        }
        return oNode;
    }

    public List<SchemaValidationError> validate(JsonNode spec, Utils.VERSION version, String location){
        List<SchemaValidationError> output = new ArrayList<>();

        try {
            JsonSchema schema = (JsonSchema) loadSchema(version);
            ProcessingReport report = schema.validate(spec);
            ListProcessingReport lp = new ListProcessingReport();
            lp.mergeWith(report);

            java.util.Iterator<ProcessingMessage> it = lp.iterator();
            while (it.hasNext()) {
                ProcessingMessage pm = it.next();
                output.add(new SchemaValidationError(pm.asJson()));
            }
        } catch (Exception e) {
            LOGGER.error("Error in FGE JSON Schema Validation", e);
        }
        return output;
    }
}
