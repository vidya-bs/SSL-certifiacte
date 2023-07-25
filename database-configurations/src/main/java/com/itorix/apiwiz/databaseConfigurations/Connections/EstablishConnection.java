package com.itorix.apiwiz.databaseConfigurations.Connections;

import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.mongodb.client.MongoClient;

import java.sql.Connection;
import java.sql.SQLException;

public interface EstablishConnection {
    public Connection getMySqlConnection(MySQLConfiguration sqlConfiguration) throws SQLException, ItorixException;

    public Connection getPostgreSqlConnection(PostgreSQLConfiguration sqlConfiguration) throws ItorixException;

    public MongoClient getMongoDbConnection(MongoDBConfiguration mongoDBConfiguration)
        throws Exception;
}
