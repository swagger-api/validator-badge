package io.swagger.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.Utils;
import io.swagger.models.SchemaValidationError;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Validator {

    static final String SCHEMA20_FILE = "schemas/20/official.json";
    static final String SCHEMA20_URL = "http://swagger.io/v2/schema.json";


    static final String SCHEMA30_FILE = "schemas/30/official.json";
    // static final String SCHEMA30_FILE = "schemas/30/schema3.json";
    static final String SCHEMA30_URL = "https://spec.openapis.org/oas/3.0/schema/2021-09-28";

    static final String SCHEMA31_FILE = "schemas/31/schema-base.json";
    static final String SCHEMA31_URL = "https://spec.openapis.org/oas/3.1/schema-base/2022-10-07";

    static Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    static class SchemaLocation {
        public SchemaLocation(String path, String url) {
            this.path = path;
            this.url = url;
        }
        public String path;
        public String url;
        public String content;
        public JsonNode node;
        public Object schemaObject;
        public long lastFetch;
    }
    protected Map<Utils.VERSION, SchemaLocation> schemaLocations = new ConcurrentHashMap<>();

    public Validator() {
        schemaLocations.put(Utils.VERSION.V20, new SchemaLocation(SCHEMA20_FILE, SCHEMA20_URL));
        schemaLocations.put(Utils.VERSION.V30, new SchemaLocation(SCHEMA30_FILE, SCHEMA30_URL));
        schemaLocations.put(Utils.VERSION.V31, new SchemaLocation(SCHEMA31_FILE, SCHEMA31_URL));
    }

    public abstract boolean supports(Utils.VERSION version);
    public abstract List<SchemaValidationError> validate(JsonNode spec, Utils.VERSION version, String location);

    public abstract Object buildSchema(JsonNode schemaNode, Utils.VERSION version) throws Exception;

    public JsonNode customizeSchema(JsonNode schemaNode, Utils.VERSION version) {
        return schemaNode;
    }
    public Object loadSchema(Utils.VERSION version) {

        SchemaLocation loc = schemaLocations.get(version);

        if (loc != null && (System.currentTimeMillis() - loc.lastFetch) < 600000) {
            return loc.schemaObject;
        }

        try {
            return loadSchemaFromNetwork(loc, version);
        } catch (Exception e) {
            LOGGER.warn("error fetching schema from network, using local copy");
            try {
                return loadSchemaFromFile(loc, version);
            } catch (Exception ex) {
                LOGGER.warn("error fetching schema from file, aborting");
                return null;
            }
        }
    }
    public Object loadSchemaFromNetwork(SchemaLocation loc, Utils.VERSION version) throws Exception {
        if (StringUtils.isBlank(loc.url)) {
            throw new Exception("empty URL");
        }
        LOGGER.debug("returning online schema");
        loc.content = Utils.getUrlContents(loc.url);
        loc.node = customizeSchema(Utils.readNode(loc.content), version);
        loc.schemaObject = buildSchema(loc.node, version);
        loc.lastFetch = System.currentTimeMillis();
        return loc.schemaObject;
    }
    public Object loadSchemaFromFile(SchemaLocation loc, Utils.VERSION version) throws Exception {
        LOGGER.debug("returning local schema");
        loc.content = Utils.getResourceFileAsString(loc.path);
        loc.node = customizeSchema(Utils.readNode(loc.content), version);
        loc.schemaObject = buildSchema(loc.node, version);
        loc.lastFetch = System.currentTimeMillis();
        return loc.schemaObject;

    }
}
