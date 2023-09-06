package com.itorix.apiwiz.datadictionary.Utils;

public class SchemaConversionStaicFields {

//    NOSQL Conversion
    public static String OBJECT_PROPERTY = "object\", \"properties\": ";
    public static String TYPE_OBJECT_PROPERTY = "\"type\": \"" + OBJECT_PROPERTY;
    public static String TYPE_BOOLEAN = "boolean\" },";
    public static String CUSTOM_TYPE = "{\"type\": \"%s\"}";
    public static String ARRAY_TYPE = "array\", \"items\":";
    public static String INTEGER_TYPE = "integer\" },";
    public static String NUMBER_TYPE = "number\" },";
    public static String STRING_TYPE = "string\" },";

//    SQL Conversion
    public static String TABLE_CAT = "TABLE_CAT";
    public static String TABLE = "TABLE";
    public static String TABLE_NAME = "TABLE_NAME";
    public static String TABLE_SCHEM = "TABLE_SCHEM";
    public static String COLUMN_NAME = "COLUMN_NAME";
    public static String TYPE_NAME = "TYPE_NAME";



}
