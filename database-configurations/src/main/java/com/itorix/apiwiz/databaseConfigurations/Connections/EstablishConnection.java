package com.itorix.apiwiz.databaseConfigurations.Connections;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import java.sql.SQLException;

public interface EstablishConnection {
    public ClientConnection getMySqlConnection(MySQLConfiguration sqlConfiguration) throws SQLException, ItorixException;

    public ClientConnection getPostgreSqlConnection(PostgreSQLConfiguration sqlConfiguration) throws ItorixException;

    public ClientConnection getMongoDbConnection(MongoDBConfiguration mongoDBConfiguration)
        throws Exception;
}
