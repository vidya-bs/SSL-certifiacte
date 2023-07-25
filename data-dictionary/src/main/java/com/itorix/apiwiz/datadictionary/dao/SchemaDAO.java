package com.itorix.apiwiz.datadictionary.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.databaseconfigs.DatabaseType;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.databaseConfigurations.Connections.GetConnectionImpl;
import com.itorix.apiwiz.datadictionary.Utils.MongoDbSchemaConverter;
import com.itorix.apiwiz.datadictionary.Utils.SQLSchemaConverter;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SchemaDAO {

    private static final Logger logger = LoggerFactory.getLogger(SchemaDAO.class);
    @Autowired
    private GetConnectionImpl getConnection;

    @Autowired
    private SQLSchemaConverter SQLSchemaConverter;

    @Autowired
    private MongoDbSchemaConverter mongoDbSchemaConverter;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Object getSchemas(String databaseType, String connectionId, String databaseName, String schemaName, List<String> collections, List<String> tables, boolean deepSearch) throws ItorixException {
        if (databaseType == null) {
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "DataBase Type is required!"), "DatabaseConfiguration-1001");
        }
        try {
            if (databaseType.equalsIgnoreCase(DatabaseType.MONGODB.getDatabaseType())) {
                if (collections == null || collections.isEmpty()) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "collections names is required"), "DatabaseConfiguration-1000");
                }
                MongoDBConfiguration databaseConfiguration = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(connectionId)), MongoDBConfiguration.class);
                return getMongoSchemas(databaseName, collections, databaseConfiguration, deepSearch);
            } else if (databaseType.equalsIgnoreCase(DatabaseType.MYSQL.getDatabaseType())) {
                if (tables == null || tables.isEmpty()) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "tables names is required"), "DatabaseConfiguration-1000");
                }
                MySQLConfiguration mySQLConfiguration = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(connectionId)), MySQLConfiguration.class);
                return getMySqlSchemas(mySQLConfiguration, tables);
            } else if (databaseType.equalsIgnoreCase(DatabaseType.POSTGRESQL.getDatabaseType())) {
                if (tables == null || tables.isEmpty()) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "tables names is required"), "DatabaseConfiguration-1000");
                }
                PostgreSQLConfiguration postgreSQLConfiguration = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(connectionId)), PostgreSQLConfiguration.class);
                return getPostgreSQLSchemas(postgreSQLConfiguration, schemaName, tables);
            } else {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "Invalid database Type"), "DatabaseConfiguration-1000");
            }
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Error occurred while connecting to database"), "DatabaseConfiguration-1002");
        }
    }

    public Object getMongoSchemas(String databaseName, List<String> collections, MongoDBConfiguration mon, boolean deepSearch) throws ItorixException {
        try {
            long start = System.currentTimeMillis();
            MongoClient client = getConnection.getMongoDbDataBaseConnection(mon);
            logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
            Map<String, Map<String, Set<ObjectNode>>> schema = mongoDbSchemaConverter.generateSchema(client, databaseName, collections, deepSearch);
            client.close();
            return schema;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred while pulling schemas from database- ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Error Occurred while pulling schemas from database"), "DatabaseConfiguration-1002");
        }
    }

    public Object getMySqlSchemas(MySQLConfiguration mySQLConfiguration, List<String> tables)
            throws ItorixException {
        try {
            Connection connection = getConnection.getMySqlDataBaseConnection(mySQLConfiguration);
            String dataBaseName = mySQLConfiguration.getMysqlDatabaseName();
            Object obj = SQLSchemaConverter.convertMysqlTabletoSchema(connection, dataBaseName, tables);
            connection.close();
            return obj;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "MySql"), "DatabaseConfiguration-1002");
        }
    }

    public Object getPostgreSQLSchemas(PostgreSQLConfiguration postgreSQLConfiguration, String schemaName, List<String> tables)
            throws ItorixException {
        try {
            Connection connection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);
            String databaseName = postgreSQLConfiguration.getPostgresqlDatabase();
            Object obj = SQLSchemaConverter.convertPostgresSqlTabletoSchema(connection, databaseName, schemaName, tables);
            connection.close();
            return obj;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Postgres"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> getDatabases(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
        long start = System.currentTimeMillis();
        MongoClient client = getConnection.getMongoDbDataBaseConnection(mongoDBConfiguration);
        logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
        List<String> databaseNames = mongoDbSchemaConverter.getDatabaseNames(client);
        return databaseNames;
    }

    public List<String> getCollectionNames(MongoDBConfiguration mongoDBConfiguration, String databaseName) throws ItorixException {
        long start = System.currentTimeMillis();
        MongoClient client = getConnection.getMongoDbDataBaseConnection(mongoDBConfiguration);
        logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
        List<String> collectionNames = mongoDbSchemaConverter.getCollectionNames(client, databaseName);
        return collectionNames;
    }

    public List<String> getMysqlTableNames(MySQLConfiguration mySQLConfiguration) throws ItorixException {
        try {
            Connection connection = getConnection.getMySqlDataBaseConnection(mySQLConfiguration);
            String databaseName = mySQLConfiguration.getMysqlDatabaseName();
            List<String> tableNames = SQLSchemaConverter.getMySqlTableNames(connection, databaseName);
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "MySql"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> getPostgresTableNames(PostgreSQLConfiguration postgreSQLConfiguration, String schemaName) throws ItorixException {
        try {
            Connection connection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);
            String databaseName = postgreSQLConfiguration.getPostgresqlDatabase();
            List<String> tableNames = SQLSchemaConverter.getPostgresSqlTableNames(connection, databaseName, schemaName);
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Postgres"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> getPostgresSchemas(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
        try {
            Connection connection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);
            String databaseName = postgreSQLConfiguration.getPostgresqlDatabase();
            List<String> schemaNames = SQLSchemaConverter.getPostgresSchemas(connection, databaseName);
            connection.close();
            return schemaNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Postgres"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> searchInMySqlDB(MySQLConfiguration mySQLConfiguration, String searchKey) throws ItorixException {
        try {
            Connection connection = getConnection.getMySqlDataBaseConnection(mySQLConfiguration);
            String databaseName = mySQLConfiguration.getMysqlDatabaseName();
            List<String> tableNames = SQLSchemaConverter.searchInMySqlDatabase(connection, databaseName, searchKey);
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "MySql"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> searchInPostgresDB(PostgreSQLConfiguration postgreSQLConfiguration, String schemaName, String searchKey) throws ItorixException {
        try {
            Connection connection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);
            String databaseName = postgreSQLConfiguration.getPostgresqlDatabase();
            List<String> tableNames = SQLSchemaConverter.searchInPostgresDatabase(connection, databaseName, schemaName, searchKey);
            connection.close();
            return tableNames;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception occurred while pulling schemas from mysql DB - {}", ex.getMessage());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Postgres"), "DatabaseConfiguration-1002");
        }
    }

    public List<String> searchInMongoDB(MongoDBConfiguration mongoDBConfiguration, String databaseName, String searchKey) throws ItorixException {
        long start = System.currentTimeMillis();
        MongoClient client = getConnection.getMongoDbDataBaseConnection(mongoDBConfiguration);
        logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
        List<String> collections = mongoDbSchemaConverter.searchForKey(client, databaseName, searchKey);
        return collections;
    }

}
