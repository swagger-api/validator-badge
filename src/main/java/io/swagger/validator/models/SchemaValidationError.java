package io.swagger.validator.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class SchemaValidationError {
    private String level, domain, keyword, message;
    private Schema schema;
    private Instance instance;
    private List<String> required;
    private List<String> missing;

    public SchemaValidationError() {
    }

    public SchemaValidationError(JsonNode node) {
        JsonNode prop = node.get("level");
        if (prop != null) {
            level = prop.asText();
        }

        prop = node.get("domain");
        if (prop != null) {
            domain = prop.asText();
        }

        prop = node.get("keyword");
        if (prop != null) {
            keyword = prop.asText();
        }

        prop = node.get("message");
        if (prop != null) {
            message = prop.asText();
        }

        prop = node.get("schema");
        if (prop != null) {
            schema = new Schema();
            JsonNode s = prop;
            prop = s.get("loadingURI");
            if (prop != null) {
                schema.setLoadingURI(prop.asText());
            }
            prop = s.get("pointer");
            if (prop != null) {
                schema.setPointer(prop.asText());
            }
        }

        prop = node.get("instance");
        if (prop != null) {
            instance = new Instance();
            JsonNode s = prop;
            prop = s.get("pointer");
            if (prop != null) {
                instance.setPointer(prop.asText());
            }
        }

        prop = node.get("required");
        if (prop != null) {
            ArrayNode an = (ArrayNode) prop;
        }

    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public List<String> getMissing() {
        return missing;
    }

    public void setMissing(List<String> missing) {
        this.missing = missing;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}