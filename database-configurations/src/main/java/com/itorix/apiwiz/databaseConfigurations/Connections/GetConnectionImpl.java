package com.itorix.apiwiz.databaseConfigurations.Connections;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetConnectionImpl implements GetConnection {

    private static final Logger logger = LoggerFactory.getLogger(GetConnectionImpl.class);

    @Autowired
    private EstablishConnectionImpl establishConnection;

    @Override
    public ClientConnection getMySqlDataBaseConnection(MySQLConfiguration mySQLConfiguration) throws SQLException, ItorixException {
        if(mySQLConfiguration == null) {
            logger.error("Invalid Database configuration - {}", mySQLConfiguration);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"database Type is missing"), "DatabaseConfiguration-1000");
        }
        return establishConnection.getMySqlConnection(mySQLConfiguration);
    }

    @Override
    public ClientConnection getPostgreSqlDataBaseConnection(PostgreSQLConfiguration postgreSQLConfiguration) throws SQLException, ItorixException {
        if(postgreSQLConfiguration == null) {
            logger.error("Invalid Database configuration - {}", postgreSQLConfiguration);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"database Type is missing"), "DatabaseConfiguration-1000");
        }
        return establishConnection.getPostgreSqlConnection(postgreSQLConfiguration);
    }

    @Override
    public ClientConnection getMongoDbDataBaseConnection(MongoDBConfiguration mongoDBConfiguration)
        throws ItorixException {
        if(mongoDBConfiguration == null) {
            logger.error("Invalid Database configuration - {}", mongoDBConfiguration);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"database Type is missing"), "DatabaseConfiguration-1000");
        }
        return establishConnection.getMongoDbConnection(mongoDBConfiguration);
    }
}
