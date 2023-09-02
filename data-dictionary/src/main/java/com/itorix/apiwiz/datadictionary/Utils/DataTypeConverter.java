package com.itorix.apiwiz.datadictionary.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DataTypeConverter {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final Map<String, ObjectNode> map = new HashMap<String, ObjectNode>() {
    private static final long serialVersionUID = 1L;
    {
      put("BIT", OBJECT_MAPPER.createObjectNode().put("type","boolean") );
      put("TINYINT", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("TINYINT UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("SMALLINT", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("SMALLINT UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("MEDIUMINT", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("MEDIUMINT UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("INT", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("INT UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("INTEGER", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("INTEGER UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("BIGINT", OBJECT_MAPPER.createObjectNode().put("type","integer").put("format", "int64") );
      put("BIGINT UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("FLOAT", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "float") );
      put("FLOAT UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("DOUBLE", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "double") );
      put("DOUBLE UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("DECIMAL", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "float") );
      put("DECIMAL UNSIGNED", OBJECT_MAPPER.createObjectNode().put("type","integer") );
      put("NUMERIC", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "double") );
      put("DATE", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date") );
      put("TIME", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date") );
      put("YEAR", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date") );
      put("TIMESTAMP", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date") );
      put("DATETIME", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date-time") );
      put("CHAR", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("VARCHAR", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("varchar", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("VARCHAR (255)", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("TINYTEXT", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("TEXT", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("MEDIUMTEXT", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("LONGTEXT", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("SET", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("ENUM", OBJECT_MAPPER.createObjectNode().put("type", "string") );
      put("BINARY", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","binary"));
      put("VARBINARY", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("TINYBLOB", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("BLOB", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("MEDIUMBLOB", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("LONGBLOB", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("GEOMETRY", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("POINT", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("LINESTRING", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("POLYGON", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("MULTIPOINT", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("MULTILINESTRING", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("MULTIPOLYGON", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("GEOMETRYCOLLECTION", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("JSON", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("BOOL", OBJECT_MAPPER.createObjectNode().put("type","boolean") );
      put("DOUBLE PRECISION", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "double") );
      put("REAL", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "double") );



      //postgresql

      put("bigint", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("bigserial", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("bit", OBJECT_MAPPER.createObjectNode().put("type", "boolean"));
      put("boolean", OBJECT_MAPPER.createObjectNode().put("type", "boolean"));
      put("bit varying", OBJECT_MAPPER.createObjectNode().put("type", "boolean"));
      put("bytea", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","byte"));
      put("char", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("character", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("character varying", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("date", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","date"));
      put("decimal", OBJECT_MAPPER.createObjectNode().put("type", "number").put("format","float"));
      put("double precision", OBJECT_MAPPER.createObjectNode().put("type", "number").put("format","double"));
      put("json", OBJECT_MAPPER.createObjectNode().put("type", "object"));
      put("jsonb", OBJECT_MAPPER.createObjectNode().put("type", "object"));
      put("numeric", OBJECT_MAPPER.createObjectNode().put("type", "number").put("format","double"));
      put("real", OBJECT_MAPPER.createObjectNode().put("type", "number").put("format","float"));
      put("serial", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("smallserial", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("smallint", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("text", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("time", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date-time"));
      put("timetz", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date-time"));
      put("timestamp", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date-time"));
      put("timestamptz", OBJECT_MAPPER.createObjectNode().put("type","string").put("format", "date-time"));
      put("uuid", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("xml", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("pg_lsn", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("pg_snapshot", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("money", OBJECT_MAPPER.createObjectNode().put("type", "number").put("format","double"));
      put("interval", OBJECT_MAPPER.createObjectNode().put("type", "number").put("format","double"));

      put("point", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("line", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("lseg", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("box", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("path", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("polygon", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("circle", OBJECT_MAPPER.createObjectNode().put("type", "string"));

      put("cidr", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("inet", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("macaddr", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("macaddr8", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("tsquery", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("tsvector", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("txid_snapshot", OBJECT_MAPPER.createObjectNode().put("type", "string"));

      put("int", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("int2", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("int4", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("int8", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("serial8", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("serial2", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("serial4", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
      put("bpchar", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("float4", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "float") );
      put("float8", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "float") );
      put("bool", OBJECT_MAPPER.createObjectNode().put("type","boolean") );
      put("double", OBJECT_MAPPER.createObjectNode().put("type","number").put("format", "double") );


      //MongoDb
      put("long", OBJECT_MAPPER.createObjectNode().put("type","integer").put("format", "int64") );
      put("integer", OBJECT_MAPPER.createObjectNode().put("type","integer").put("format", "int32") );
      put("string", OBJECT_MAPPER.createObjectNode().put("type", "string"));
      put("dateTime", OBJECT_MAPPER.createObjectNode().put("type", "string").put("format","date-time"));

    }
  };
  public ObjectNode getDataType(String type){
    if(!type.isEmpty() && type.charAt(0) == '_'){
      return OBJECT_MAPPER.createObjectNode().put("type","array").set("items",map.get(type.substring(1)));
    }
    ObjectNode objType = map.get(type);
    if (objType == null){
      log.debug("Custom datatype is not available, Falling back to default datatype string");
      objType = map.get("string");
    }
    return objType;
  }
}
