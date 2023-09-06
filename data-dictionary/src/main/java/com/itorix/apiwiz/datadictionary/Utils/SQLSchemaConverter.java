package com.itorix.apiwiz.datadictionary.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

import static com.itorix.apiwiz.datadictionary.Utils.SchemaConversionStaicFields.*;

@Component
public class SQLSchemaConverter {

    Set<String> ignorableSchemaNames = new HashSet<>(Arrays.asList("information_schema", "pg_catalog"));
    private static final Logger logger = LoggerFactory.getLogger(SQLSchemaConverter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private DataTypeConverter dataTypeConverter;

    public Object convertMysqlTabletoSchema(Connection connection, String dataBaseName, Set<String> tables) throws ItorixException {
        try {
            if (dataBaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "Mysql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            List<ObjectNode> schemas = new ArrayList<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            tables.parallelStream().forEach( tableName -> {
                try {
                    ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
                    ResultSet columns = metaData.getColumns(dataBaseName, null, tableName, null);
                    if (!columns.next()) {
                        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1005"), "Mysql", tableName), "DatabaseConfiguration-1005");
                    }
                    ObjectNode obj = OBJECT_MAPPER.createObjectNode();
                    ObjectNode json = OBJECT_MAPPER.createObjectNode();
                    obj.put("type", "object");
                    do {
                        String columnName = columns.getString(COLUMN_NAME);
                        String columnType = columns.getString(TYPE_NAME);
                        ObjectNode relevantDataType = dataTypeConverter.getDataType(columnType);

                        json.set(columnName, relevantDataType);
                    } while (columns.next());
                    obj.set("properties", json);
                    jsonNode.set(tableName, obj);
                    columns.close();
                    schemas.add(jsonNode);
                } catch (Exception ex){
                    logger.error("Exception while converting {} - ", tableName, ex);
                }
            });
            dbs.close();
            connection.close();
            return schemas;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "Mysql"), "DatabaseConfiguration-1003");
        }
    }

    public Object convertPostgresSqlTabletoSchema(Connection connection, String dataBaseName, String schemaName, Set<String> tables) throws ItorixException {
        try {
            if (dataBaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "postgreSql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            List<ObjectNode> schemas = new ArrayList<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            tables.parallelStream().forEach( tableName -> {
                try {
                    ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
                    ResultSet columns = metaData.getColumns(dataBaseName, schemaName, tableName, null);
                    if (!columns.next()) {
                        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1005"), "Mysql", tableName), "DatabaseConfiguration-1005");
                    }
                    ObjectNode obj = OBJECT_MAPPER.createObjectNode();
                    ObjectNode json = OBJECT_MAPPER.createObjectNode();
                    obj.put("type", "object");
                    do {
                        String columnName = columns.getString(COLUMN_NAME);
                        String columnType = columns.getString(TYPE_NAME);
                        ObjectNode relevantDataType = dataTypeConverter.getDataType(columnType);
                        json.set(columnName, relevantDataType);
                    } while (columns.next());

                    obj.set("properties", json);
                    jsonNode.set(tableName, obj);
                    columns.close();
                    schemas.add(jsonNode);
                } catch (Exception ex){
                    logger.error("Exception while converting {} - ", tableName, ex);
                }
            });
            dbs.close();
            connection.close();
            return schemas;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "postgreSql"), "DatabaseConfiguration-1003");
        }
    }

    public List<String> getPostgresSchemas(Connection connection, String dataBaseName) throws ItorixException {
        List<String> schemaNames = new ArrayList<>();
        try {
            if (dataBaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "PostgreSql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            try {
                ResultSet schemas = metaData.getSchemas(dataBaseName, "%");
                while (schemas.next()) {
                    String tableName = schemas.getString(TABLE_SCHEM);
                    if(!ignorableSchemaNames.contains(tableName)) {
                        schemaNames.add(tableName);
                    }
                }
                schemas.close();
            } catch (Exception ex) {
                String errorMessage = String.format("While fetching data from database - %s", dataBaseName);
                logger.error("Error occurred while fetching data from database - ", ex);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "PostgreSql", errorMessage), "DatabaseConfiguration-1004");
            }
            dbs.close();
            connection.close();
            return schemaNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "PostgreSql"), "DatabaseConfiguration-1003");
        }
    }

    public List<String> getMySqlTableNames(Connection connection, String dataBaseName) throws ItorixException {
        List<String> tableNames = new ArrayList<>();
        try {
            if (dataBaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "MySql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            try {
                String[] types = {TABLE};
                ResultSet tables = metaData.getTables(dataBaseName, null, "%", types);
                while (tables.next()) {
                    String tableName = tables.getString(TABLE_NAME);
                    tableNames.add(tableName);
                }
                tables.close();
            } catch (Exception ex) {
                String errorMessage = String.format("While fetching data from database - %s", dataBaseName);
                logger.error("Error occurred while fetching data from database - ", ex);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "MySql", errorMessage), "DatabaseConfiguration-1004");
            }
            dbs.close();
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "MySql"), "DatabaseConfiguration-1003");
        }
    }

    public List<String> getPostgresSqlTableNames(Connection connection, String databaseName, String schemaName) throws ItorixException {
        List<String> tableNames = new ArrayList<>();
        try {
            if (databaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "MySql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            try {
                String[] types = {TABLE};
                ResultSet tables = metaData.getTables(databaseName, schemaName, null, types);
                while (tables.next()) {
                    String tableName = tables.getString(TABLE_NAME);
                    tableNames.add(tableName);
                }
                tables.close();
            } catch (Exception ex) {
                String errorMessage = String.format("While fetching data from database - %s", databaseName);
                logger.error("Error occurred while fetching data from database - ", ex);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "PostgreSql", errorMessage), "DatabaseConfiguration-1004");
            }
            dbs.close();
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "PostgreSql"), "DatabaseConfiguration-1003");
        }
    }

    public List<String> searchInMySqlDatabase(Connection connection, String databaseName, String searchKey) throws ItorixException {
        List<String> tableNames = new ArrayList<>();
        try {
            if (databaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "MySql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            try {
                String[] types = {TABLE};
                ResultSet tables = metaData.getTables(databaseName, null, "%", types);
                while (tables.next()) {
                    try {
                        String tableName = tables.getString(TABLE_NAME);
                        ResultSet column = metaData.getColumns(databaseName, null, tableName, searchKey);
                        if(column.next()){
                            tableNames.add(tableName);
                        }
                    } catch (Exception ex) {
                        logger.error("Error while converting the table {} to schema", ex.getMessage());
                    }
                }
                tables.close();
            } catch (Exception ex) {
                String errorMessage = String.format("While fetching data from database - %s", databaseName);
                logger.error("Error occurred while fetching data from database - ", ex);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "MySql", errorMessage), "DatabaseConfiguration-1004");
            }
            dbs.close();
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "MySql"), "DatabaseConfiguration-1003");
        }
    }


    public List<String> searchInPostgresDatabase(Connection connection, String databaseName, String sschemaName, String searchKey) throws ItorixException {
        List<String> tableNames = new ArrayList<>();
        try {
            if (databaseName == null) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "PostgreSql", "Invalid database name"), "DatabaseConfiguration-1004");
            }
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet dbs = metaData.getCatalogs();
            try {
                String[] types = {TABLE};
                ResultSet tables = metaData.getTables(databaseName, sschemaName, "%", types);
                while (tables.next()) {
                    try {
                        String tableName = tables.getString(TABLE_NAME);
                        ResultSet column = metaData.getColumns(databaseName, sschemaName, tableName, searchKey);
                        if(column.next()){
                            tableNames.add(tableName);
                        }
                    } catch (Exception ex) {
                        logger.error("Error while converting the table {} to schema", ex.getMessage());
                    }
                }
                tables.close();
            } catch (Exception ex) {
                String errorMessage = String.format("While fetching data from database - %s", databaseName);
                logger.error("Error occurred while fetching data from database - ", ex);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1004"), "PostgreSql", errorMessage), "DatabaseConfiguration-1004");
            }
            dbs.close();
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1003"), "PostgreSql"), "DatabaseConfiguration-1003");
        }
    }
}
