package com.itorix.apiwiz.databaseConfigurations.Connections;

import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.mongodb.client.MongoClient;

import java.sql.Connection;
import java.sql.SQLException;

public interface GetConnection {

    public Connection getMySqlDataBaseConnection(MySQLConfiguration mySQLConfiguration) throws SQLException, ItorixException;

    public Connection getPostgreSqlDataBaseConnection(PostgreSQLConfiguration postgreSQLConfiguration) throws SQLException, ItorixException;

    public MongoClient getMongoDbDataBaseConnection(MongoDBConfiguration mongoDBConfiguration)
        throws Exception;
}
