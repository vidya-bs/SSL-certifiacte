package com.itorix.apiwiz.datadictionary.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.mongodb.client.*;
import org.bson.BsonRegularExpression;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static com.itorix.apiwiz.datadictionary.Utils.SchemaConversionStaicFields.*;

@Component
public class MongoDbSchemaConverter {
    private static final Logger logger = LoggerFactory.getLogger(MongoDbSchemaConverter.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${itorix.schema.import.normalSearchScanCount:10}")
    private int normalSearchScanCount;

    @Value("${itorix.schema.import.deepSearchScanCount:100}")
    private int deepSearchScanCount;

    @Value("${itorix.schema.import.keySearchScanCount:10}")
    private int keySearchScanCount;

    @Autowired
    private DataTypeConverter dataTypeConverter;

    private static JsonWriterSettings settings = JsonWriterSettings.builder()
            .outputMode(JsonMode.STRICT)
            .objectIdConverter((value, writer) -> writer.writeString(value.toHexString()))
            .indent(true)
            .int64Converter((value, writer) -> writer.writeNumber(String.valueOf(value.longValue())))
            .doubleConverter((value, writer) -> writer.writeNumber(String.valueOf(value.doubleValue())))
            .decimal128Converter((value, writer) -> writer.writeNumber(String.valueOf(value.bigDecimalValue())))
            .build();

    public List<ObjectNode> generateSchema(MongoClient client, String databaseName, Set<String> collections, boolean deepSearch) {
        long start = System.currentTimeMillis();
        MongoDatabase database = client.getDatabase(databaseName);
        List<ObjectNode> schemas = new ArrayList<>();
        logger.info("Time took for getting database connection - {}", (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        collections.parallelStream().forEach(collection -> {
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
        });
        logger.info("Time took for conversion - {}", (System.currentTimeMillis() - start));
        return schemas;
    }



    public ObjectNode generateSchemaFromDatabase(MongoDatabase database, String collectionName, boolean deepSearch)
            throws JSONException, IOException {
        long start = System.currentTimeMillis();
        logger.debug("Generating schema for collection - {}", collectionName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        MongoCursor<Document> cursor = null;
        if(deepSearch) {
            cursor = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(deepSearchScanCount)
                    .iterator();
        } else {
            cursor = collection.find()
                    .sort(new Document("_id", -1))
                    .limit(normalSearchScanCount)
                    .iterator();
        }
        ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            try {
                linearDataType(doc, jsonNode, collectionName);
            } catch (Exception ex) {
                logger.error("Error while fetching schema from document ", ex);
            }
        }
        cursor.close();
        logger.debug("Generated schema for collection - {} in {}ms", collectionName, System.currentTimeMillis() - start);
        return jsonNode;
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
        collections.parallelStream().forEach( collectionName -> {
            long localStart = System.currentTimeMillis();
            logger.debug("Searching for key in collection - {}", collectionName);
            try {
                if(checkCollectionHasKey(database, collectionName, searchKey)){
                    collectionNamesWithKey.add(collectionName);
                }
            } catch (CodecConfigurationException exception){
                logger.error("UnSupported Codec datatype for {} collection - {}", collectionName, exception.getMessage());
            } catch (Exception ex) {
                logger.error("Error occurred while converting {} to schema!, because of - ", collectionName, ex);
            } finally {
                logger.debug("Search over for key in collection - {} is  {}ms", collectionName, System.currentTimeMillis() - localStart);
            }
        });
        logger.info("Time took for searching in mongodb - {}", (System.currentTimeMillis() - start));
        return collectionNamesWithKey;
    }

    private boolean checkCollectionHasKey(MongoDatabase database, String collectionName, String searchKey) throws IOException {
        MongoCollection<Document> mongoCollection = database.getCollection(collectionName);
        MongoCursor<Document> cursor = mongoCollection.find()
                .sort(new Document("_id", -1))
                .limit(keySearchScanCount)
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

    public void linearDataType(Object obj, ObjectNode jsonNode, String key){
        if(obj instanceof Long){
            jsonNode.set(key, dataTypeConverter.getDataType("long"));
        }
        else if(obj instanceof Integer){
            jsonNode.set(key, dataTypeConverter.getDataType("integer"));
        }
        else if(obj instanceof String){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof ObjectId){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof Date){
            jsonNode.set(key, dataTypeConverter.getDataType("dateTime"));
        }
        else if(obj instanceof BsonTimestamp){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof MinKey){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof MaxKey){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj == null){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof Symbol){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof BsonRegularExpression){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof Double){
            jsonNode.set(key, dataTypeConverter.getDataType("double"));
        }
        else if(obj instanceof Decimal128){
            jsonNode.set(key, dataTypeConverter.getDataType("decimal"));
        }
        else if(obj instanceof CodeWithScope){
            jsonNode.set(key, dataTypeConverter.getDataType("string"));
        }
        else if(obj instanceof Binary){
            jsonNode.set(key, dataTypeConverter.getDataType("binary"));
        }
        else if(obj instanceof Boolean){
            jsonNode.set(key, dataTypeConverter.getDataType("boolean"));
        }
        else if(obj instanceof ArrayList){
            if(jsonNode.get(key) != null){
                ObjectNode innerJsonNode = (ObjectNode) jsonNode.get(key);
                ArrayList<?> objArray = (ArrayList<?>) obj;
                if (objArray != null && objArray.size() > 0) {
                    linearDataType(((ArrayList<?>) obj).get(0), innerJsonNode, "items");
                }
                jsonNode.set(key, innerJsonNode);
            }else {
                ObjectNode innerJsonNode = OBJECT_MAPPER.createObjectNode();
                innerJsonNode.put("type", "array");  // check for the properties already exists
                ArrayList<?> objArray = (ArrayList<?>) obj;
                if (objArray != null && objArray.size() > 0) {
                    linearDataType(((ArrayList<?>) obj).get(0), innerJsonNode, "items");
                }
                jsonNode.set(key, innerJsonNode);
            }

        } else if( obj instanceof Document){
            ObjectNode childNode = OBJECT_MAPPER.createObjectNode();
            if(jsonNode.get(key) != null) {
                if(jsonNode.get(key).get("properties") != null)
                    childNode = (ObjectNode) jsonNode.get(key).get("properties");
            }
            for(String objKey: ((Document) obj).keySet()){
                Object object = ((Document) obj).get(objKey);
                if( object instanceof Document){
                    linearDataType(object, childNode, objKey);
                } else {
                    linearDataType(object, childNode, objKey);
                }
            }
            ObjectNode parentNode = OBJECT_MAPPER.createObjectNode()
                    .put("type", "object")
                    .set("properties", childNode);
            jsonNode.set(key, parentNode);
        } else{
            logger.debug("Unsupported datatype - {}", obj);
        }
        // DBref not supported for now due to cyclic dependency and dependency tracking overhead
    }

}

