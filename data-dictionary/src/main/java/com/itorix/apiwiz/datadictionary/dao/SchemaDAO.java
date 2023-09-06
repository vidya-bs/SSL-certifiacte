package com.itorix.apiwiz.datadictionary.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${itorix.schema.max.collections:10}")
    private int maxSchemaCollection;

    public Object getSchemas(String databaseType, String connectionId, String databaseName, String schemaName, Set<String> collections, Set<String> tables, boolean deepSearch) throws ItorixException {
        if (databaseType == null) {
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "DataBase Type is required!"), "DatabaseConfiguration-1001");
        }
        try {
            if (databaseType.equalsIgnoreCase(DatabaseType.MONGODB.getDatabaseType())) {
                if(databaseName == null) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "database or tenant name is required"), "DatabaseConfiguration-1001");
                }
                if (collections == null || collections.isEmpty()) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "collections names is required"), "DatabaseConfiguration-1001");
                }
                if (deepSearch && collections.size() > 1) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "when the deep search is enabled User not allowed to import more than one schema(collection) from db "), "DatabaseConfiguration-1001");
                }
                if (collections.size() > maxSchemaCollection) {
                    String error = String.format("User not allowed to import more than %s schema(collection) from db at single request", maxSchemaCollection);
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), error), "DatabaseConfiguration-1001");
                }
                MongoDBConfiguration databaseConfiguration = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(connectionId)), MongoDBConfiguration.class);
                return getMongoSchemas(databaseName, collections, databaseConfiguration, deepSearch);
            } else if (databaseType.equalsIgnoreCase(DatabaseType.MYSQL.getDatabaseType())) {
                if (tables == null || tables.isEmpty()) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "tables names is required"), "DatabaseConfiguration-1001");
                }
                if (tables.size() > maxSchemaCollection) {
                    String error = String.format("User not allowed to import more than %s schema(table) from db at single request", maxSchemaCollection);
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), error), "DatabaseConfiguration-1001");
                }
                MySQLConfiguration mySQLConfiguration = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(connectionId)), MySQLConfiguration.class);
                return getMySqlSchemas(mySQLConfiguration, tables);
            } else if (databaseType.equalsIgnoreCase(DatabaseType.POSTGRESQL.getDatabaseType())) {
                if (tables == null || tables.isEmpty()) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "tables names is required"), "DatabaseConfiguration-1001");
                }
                if (tables.size() > maxSchemaCollection) {
                    String error = String.format("User not allowed to import more than %s schema(table) from db at single request", maxSchemaCollection);
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), error), "DatabaseConfiguration-1001");
                }
                PostgreSQLConfiguration postgreSQLConfiguration = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(connectionId)), PostgreSQLConfiguration.class);
                return getPostgreSQLSchemas(postgreSQLConfiguration, schemaName, tables);
            } else {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"), "Invalid database Type"), "DatabaseConfiguration-1001");
            }
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Error occurred while connecting to database"), "DatabaseConfiguration-1002");
        }
    }

    public Object getMongoSchemas(String databaseName, Set<String> collections, MongoDBConfiguration mongoDBConf, boolean deepSearch) throws ItorixException {
        long start = System.currentTimeMillis();
        try (ClientConnection clientConnection = getConnection.getMongoDbDataBaseConnection(mongoDBConf);){
            MongoClient client = clientConnection.getMongoClient();
            logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
            List<ObjectNode> schema = mongoDbSchemaConverter.generateSchema(client, databaseName, collections, deepSearch);
            client.close();
            return schema;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred while pulling schemas from database- ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Error Occurred while pulling schemas from database"), "DatabaseConfiguration-1002");
        }
    }

    public Object getMySqlSchemas(MySQLConfiguration mySQLConfiguration, Set<String> tables)
            throws ItorixException {
        try (ClientConnection clientConnection = getConnection.getMySqlDataBaseConnection(mySQLConfiguration);){
            Connection connection = clientConnection.getConnection();
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

    public Object getPostgreSQLSchemas(PostgreSQLConfiguration postgreSQLConfiguration, String schemaName, Set<String> tables)
            throws ItorixException {
        try (ClientConnection clientConnection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);){
            Connection connection = clientConnection.getConnection();
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
        try (ClientConnection  clientConnection = getConnection.getMongoDbDataBaseConnection(mongoDBConfiguration);){
            MongoClient client = clientConnection.getMongoClient();
            logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
            List<String> databaseNames = mongoDbSchemaConverter.getDatabaseNames(client);
            return databaseNames;
        } catch (ItorixException ex) {
            throw ex;
        }
    }

    public List<String> getCollectionNames(MongoDBConfiguration mongoDBConfiguration, String databaseName) throws ItorixException {
        long start = System.currentTimeMillis();
        try (ClientConnection clientConnection = getConnection.getMongoDbDataBaseConnection(mongoDBConfiguration);){
            MongoClient client = clientConnection.getMongoClient();
            logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
            List<String> collectionNames = mongoDbSchemaConverter.getCollectionNames(client, databaseName);
            return collectionNames;
        } catch (ItorixException ex) {
            throw ex;
        }
    }

    public List<String> getMysqlTableNames(MySQLConfiguration mySQLConfiguration) throws ItorixException {
        try (ClientConnection clientConnection = getConnection.getMySqlDataBaseConnection(mySQLConfiguration);){
            Connection connection = clientConnection.getConnection();
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
        try (ClientConnection clientConnection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);){
            Connection connection = clientConnection.getConnection();
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
        try (ClientConnection clientConnection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);){
            Connection connection = clientConnection.getConnection();
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
        try (ClientConnection clientConnection = getConnection.getMySqlDataBaseConnection(mySQLConfiguration);) {
            Connection connection = clientConnection.getConnection();
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
        try (ClientConnection clientConnection = getConnection.getPostgreSqlDataBaseConnection(postgreSQLConfiguration);) {
            Connection connection = clientConnection.getConnection();
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
        try(ClientConnection clientConnection = getConnection.getMongoDbDataBaseConnection(mongoDBConfiguration)) {
            MongoClient client = clientConnection.getMongoClient();
            logger.info("Time took for establishing connection - {}", (System.currentTimeMillis() - start));
            List<String> collections = mongoDbSchemaConverter.searchForKey(client, databaseName, searchKey);
            return collections;
        } catch (ItorixException ex) {
            throw ex;
        }
    }

}
