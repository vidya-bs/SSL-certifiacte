package com.itorix.apiwiz.databaseConfigurations.Connections;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import java.sql.SQLException;

public interface GetConnection {

    public ClientConnection getMySqlDataBaseConnection(MySQLConfiguration mySQLConfiguration) throws SQLException, ItorixException;

    public ClientConnection getPostgreSqlDataBaseConnection(PostgreSQLConfiguration postgreSQLConfiguration) throws SQLException, ItorixException;

    public ClientConnection getMongoDbDataBaseConnection(MongoDBConfiguration mongoDBConfiguration)
        throws Exception;
}
