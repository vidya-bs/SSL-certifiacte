package com.itorix.apiwiz.datadictionary.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.mongodb.client.*;
import org.bson.BsonRegularExpression;
import org.bson.BsonSymbol;
import org.bson.Document;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.*;
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
import java.sql.Timestamp;
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

    public List<ObjectNode> generateSchema(MongoClient client, String databaseName, List<String> collections, boolean deepSearch) {
        long start = System.currentTimeMillis();
        MongoDatabase database = client.getDatabase(databaseName);
        List<ObjectNode> schemas = new ArrayList<>();
        if (collections == null) {
            MongoIterable<String> collectionNames = database.listCollectionNames();
            collections = collectionNames.into(new ArrayList<>());
        }
        Collections.sort(collections);
        logger.info("Time took for getting database connection - {}", (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (String collection : collections) {
            try {
                ObjectNode obj = generateSchemaFromDatabase(database, collection, deepSearch);
                if (!obj.isEmpty()) {
                    schemas.add(obj);
                }
            } catch (CodecConfigurationException exception){
                logger.error("UnSupported Codec datatype for {} collection - {}", collection, exception.getMessage());
            } catch (Exception ex) {
                logger.error("Error occurred while converting {} to schema!, because of - ", collection, ex);
            }
        }
        logger.info("Time took for conversion - {}", (System.currentTimeMillis() - start));
        return schemas;
    }



    public ObjectNode generateSchemaFromDatabase(MongoDatabase database, String collectionName, boolean deepSearch)
            throws JSONException, IOException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        MongoCursor<Document> cursor = null;
        if(deepSearch) {
            cursor = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(100)
                    .iterator();
        } else {
            cursor = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(10)
                    .iterator();
        }
        List<Document> documents = new ArrayList<>();
        while (cursor.hasNext()) {
            documents.add(cursor.next());
        }
        cursor.close();
        ObjectNode parentNode = OBJECT_MAPPER.createObjectNode();
        ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
        for (Document doc : documents) {
            try {
                jsonNode = linearDataType(doc);
            } catch (Exception ex){
                logger.error("Error while fetching schema from document ", ex);
            }
        }
        parentNode.set(collectionName, jsonNode);
        return parentNode;
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
        try {
            if (!exists) {
                Iterator<?> keys = object.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Object obj = object.get(key);
                    if (object.get(key) instanceof JSONObject) {
                        exists = keyExists((JSONObject) obj, searchedKey);
                        if (exists) {
                            return true;
                        }
                    } else if (object.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) obj;
                        if (!jsonArray.isNull(0) && jsonArray.get(0) instanceof JSONObject) {
                            exists = keyExists((JSONObject) jsonArray.get(0) , searchedKey);
                            if (exists) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex){
            logger.error("Exception while parsing the json document - ", ex);
        }
        return exists;
    }

    private ObjectNode parseV2(Document doc) {
        ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
        for(String key: doc.keySet()){
            System.out.println(key);
            Object obj = doc.get(key);
            if( obj instanceof Document){
                ObjectNode childNode = parseV2((Document) obj);
                jsonNode.set(key, OBJECT_MAPPER.createObjectNode().put("type", "object").set("properties", childNode));
            } else {
                jsonNode.set(key, linearDataType(obj));
            }
        }
        return jsonNode;
    }

    public ObjectNode linearDataType(Object obj){
        if(obj instanceof Long){
            return OBJECT_MAPPER.createObjectNode().put("type", "long");
        }
        else if(obj instanceof Integer){
            return OBJECT_MAPPER.createObjectNode().put("type", "integer");
        }
        else if(obj instanceof String){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof ObjectId){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof Date){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof Timestamp){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof MinKey){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof MaxKey){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj == null){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof Symbol){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof BsonRegularExpression){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof Double){
            return OBJECT_MAPPER.createObjectNode().put("type", "double");
        }
        else if(obj instanceof Decimal128){
            return OBJECT_MAPPER.createObjectNode().put("type", "decimal");
        }
        else if(obj instanceof CodeWithScope){
            return OBJECT_MAPPER.createObjectNode().put("type", "string");
        }
        else if(obj instanceof ArrayList){
            ObjectNode childNode = linearDataType(((ArrayList<?>) obj).get(0));
            return  OBJECT_MAPPER.createObjectNode().put("type", "array").set("items", childNode);
        } else if( obj instanceof Document){
            ObjectNode childNode = OBJECT_MAPPER.createObjectNode();
            for(String key: ((Document) obj).keySet()){
                System.out.println(key);
                Object object = ((Document) obj).get(key);
                if( object instanceof Document){
                    ObjectNode innerChildNode = linearDataType(object);
                    childNode.set(key, innerChildNode);
                } else {
                    childNode.set(key, linearDataType(object));
                }
            }
            return OBJECT_MAPPER.createObjectNode().put("type", "object").set("properties", childNode);
        }
        return OBJECT_MAPPER.createObjectNode().put("type", "string");
    }

}

