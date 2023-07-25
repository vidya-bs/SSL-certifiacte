package com.itorix.apiwiz.datadictionary.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static com.itorix.apiwiz.datadictionary.Utils.SchemaConversionStaicFields.*;

@Component
public class MongoDbSchemaConverter {
    private static final Logger logger = LoggerFactory.getLogger(MongoDbSchemaConverter.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static JsonWriterSettings settings = JsonWriterSettings.builder()
            .outputMode(JsonMode.STRICT)
            .objectIdConverter((value, writer) -> writer.writeString(value.toHexString()))
            .indent(true)
            .int64Converter((value, writer) -> writer.writeNumber(String.valueOf(value.longValue())))
            .doubleConverter((value, writer) -> writer.writeNumber(String.valueOf(value.doubleValue())))
            .decimal128Converter((value, writer) -> writer.writeNumber(String.valueOf(value.bigDecimalValue())))
            .build();

    public Map<String, Map<String, Set<ObjectNode>>> generateSchema(MongoClient client, String databaseName, List<String> collections, boolean deepSearch) {
        long start = System.currentTimeMillis();
        MongoDatabase database = client.getDatabase(databaseName);
        Map<String, Map<String, Set<ObjectNode>>> map = new HashMap<>();
        Map<String, Set<ObjectNode>> collectionSchemas = new HashMap<>();
        if (collections == null) {
            MongoIterable<String> collectionNames = database.listCollectionNames();
            collections = collectionNames.into(new ArrayList<>());
        }
        Collections.sort(collections);
        logger.info("Time took for getting database connection - {}", (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (String collection : collections) {
            try {
                Set<ObjectNode> obj = generateSchemaFromDatabase(database, collection, deepSearch);
                if (!obj.isEmpty()) {
                    collectionSchemas.put(collection, obj);
                }
            } catch (CodecConfigurationException exception){
                logger.error("UnSupported Codec datatype for {} collection - {}", collection, exception.getMessage());
            } catch (Exception ex) {
                logger.error("Error occurred while converting {} to schema!, because of - ", collection, ex);
            }
        }
        logger.info("Time took for conversion - {}", (System.currentTimeMillis() - start));
        if (!collectionSchemas.isEmpty()) {
            map.put(databaseName, collectionSchemas);
        }
        return map;
    }



    public Set<ObjectNode> generateSchemaFromDatabase(MongoDatabase database, String collectionName,boolean deepSearch)
            throws JSONException, IOException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        MongoCursor<Document> cursor = null;
        if(deepSearch) {
            cursor = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(10)
                    .iterator();
        }else{
            cursor = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(1)
                    .iterator();
        }
        List<Document> documents = new ArrayList<>();
        while (cursor.hasNext()) {
            documents.add(cursor.next());
        }
        cursor.close();
        Set<ObjectNode> nodeSet = new HashSet<>();

        for (Document doc : documents) {
            try {
                String result = outputAsString(doc.toJson(settings));
                ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
                JsonNode propertyNode = OBJECT_MAPPER.readTree(result);
                jsonNode.set(collectionName, propertyNode);
                nodeSet.add(jsonNode);
            } catch (Exception ex){
                logger.error("Error while fetching schema from document ", ex);
            }
        }
        return nodeSet;
    }

    public static String outputAsString(String json) throws IOException {
        return cleanup(outputAsString(json, null));
    }

    private static String outputAsString(String json, JsonNodeType type) throws IOException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
        StringBuilder output = new StringBuilder();
        output.append("{");

        if (type == null) output.append(TYPE_OBJECT_PROPERTY + "{");

        for (Iterator<String> iterator = jsonNode.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();

            if((jsonNode.get(fieldName) instanceof ArrayNode && (jsonNode.get(fieldName).isNull() || jsonNode.get(fieldName).isEmpty())) ||
                    jsonNode.get(fieldName) instanceof NullNode) {
                continue;
            }else{
                JsonNodeType nodeType = jsonNode.get(fieldName).getNodeType();
                output.append(convertNodeToStringSchemaNode(jsonNode, nodeType, fieldName));
            }
        }

        if (type == null) output.append("}");

        output.append("}");

        return output.toString();
    }

    private static String convertNodeToStringSchemaNode(
            JsonNode jsonNode, JsonNodeType nodeType, String key) throws IOException {

        StringBuilder result = new StringBuilder("\"" + key + "\": { \"type\": \"");

        JsonNode node;
        switch (nodeType) {
            case ARRAY:
                node = jsonNode.get(key).get(0);
                result.append(ARRAY_TYPE);
                JsonNodeType type ;
                try{
                    type = jsonNode.get(key).get(0).getNodeType();
                }catch(Exception ex){
                    type = JsonNodeType.STRING;
                }
                if (type == JsonNodeType.OBJECT) {
                    result.append("{ "+ TYPE_OBJECT_PROPERTY);
                    result.append(outputAsString(node.toString(), type));
                } else if (type == JsonNodeType.ARRAY) {
                    result.append(outputAsString(node.toString(), type));
                } else {
                    result.append(String.format(CUSTOM_TYPE, type.toString().toLowerCase()));
                }
                if (type == JsonNodeType.OBJECT) {
                    result.append("}},");
                } else {
                    result.append("},");
                }
                break;
            case BOOLEAN:
                result.append(TYPE_BOOLEAN);
                break;
            case NUMBER:
                if(jsonNode.get(key) instanceof IntNode){
                    result.append(INTEGER_TYPE);
                }else {
                    result.append(NUMBER_TYPE);
                }
                break;
            case OBJECT:
                node = jsonNode.get(key);
                result.append(OBJECT_PROPERTY);
                result.append(outputAsString(node.toString(), JsonNodeType.OBJECT));
                result.append("},");
                break;
            case STRING:
                result.append(STRING_TYPE);
                break;
        }
        return result.toString();
    }

    private static String cleanup(String dirty) {
        JSONObject rawSchema = new JSONObject(new JSONTokener(dirty));
        Schema schema = SchemaLoader.load(rawSchema);
        return schema.toString();
    }

    public List<String> getCollectionNames(MongoClient client, String databaseName) throws ItorixException {
        try {
            MongoDatabase database = client.getDatabase(databaseName);
            MongoIterable<String> collections = database.listCollectionNames();
            List<String> collectionNames = collections.into(new ArrayList<>());
            client.close();
            return collectionNames;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Error occurred while connecting to database"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> getDatabaseNames(MongoClient client) throws ItorixException {
        try {
            MongoIterable<String> dbNames = client.listDatabaseNames();
            List<String> databases = dbNames.into(new ArrayList<>());
            client.close();
            return databases;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Error occurred while connecting to database"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> searchForKey(MongoClient client, String databaseName, String searchKey) {
        long start = System.currentTimeMillis();
        MongoDatabase database = client.getDatabase(databaseName);
        List<String> collectionNamesWithKey = new ArrayList<>();

        MongoIterable<String> collectionNames = database.listCollectionNames();
        List<String> collections = collectionNames.into(new ArrayList<>());
        for (String collectionName : collections) {
            try {
                if(checkCollectionHasKey(database, collectionName, searchKey)){
                    collectionNamesWithKey.add(collectionName);
                }
            }
            catch (CodecConfigurationException exception){
                logger.error("UnSupported Codec datatype for {} collection - {}", collectionName, exception.getMessage());
            } catch (Exception ex) {
                logger.error("Error occurred while converting {} to schema!, because of - ", collectionName, ex);
            }
        }
        logger.info("Time took for searching in mongodb - {}", (System.currentTimeMillis() - start));
        return collectionNamesWithKey;
    }

    private boolean checkCollectionHasKey(MongoDatabase database, String collectionName, String searchKey) throws IOException {
        MongoCollection<Document> mongoCollection = database.getCollection(collectionName);
        MongoCursor<Document> cursor = mongoCollection.find()
                .sort(new Document("_id", -1))
                .limit(10)
                .iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            String result = document.toJson(settings);
            JSONObject jsonObject = new JSONObject(result);
            if(keyExists(jsonObject, searchKey)){
                return true;
            }
        }
        return false;
    }

    public boolean keyExists(JSONObject  object, String searchedKey) {
        boolean exists = object.has(searchedKey);
        if(!exists) {
            Iterator<?> keys = object.keys();
            while( keys.hasNext() ) {
                String key = (String)keys.next();
                if ( object.get(key) instanceof JSONObject ) {
                    exists = keyExists((JSONObject) object.get(key), searchedKey);
                    if(exists){
                        return true;
                    }
                } else if ( object.get(key) instanceof JSONArray ) {
                    exists = keyExists(((JSONArray) object.get(key)).getJSONObject(0), searchedKey);
                    if(exists){
                        return true;
                    }
                }
            }
        }
        return exists;
    }
}

