package com.itorix.apiwiz.databaseConfigurations.Connections;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDbAuthType;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySqlConfigType;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgresAuthType;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.databaseConfigurations.MongoDb.MongoDbConnector;
import com.itorix.apiwiz.databaseConfigurations.MySql.MySqlConnector;
import com.itorix.apiwiz.databaseConfigurations.PostgreSql.PostgreSqlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EstablishConnectionImpl implements EstablishConnection {

    private static final Logger logger = LoggerFactory.getLogger(EstablishConnectionImpl.class);

    @Autowired
    private MySqlConnector mySqlTcpConnector;

    @Autowired
    private PostgreSqlConnector postgreSqlConnector;

    @Autowired
    private MongoDbConnector mongoDbConnector;

    @Override
    public ClientConnection getMySqlConnection(MySQLConfiguration sqlConfiguration) throws ItorixException {
        if(sqlConfiguration == null || sqlConfiguration.getMySqlConfigType() == null) {
            logger.error("Invalid Database configuration - {}", sqlConfiguration);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MYSQL auth type is necessary but missing"), "DatabaseConfiguration-1000");
        }

        MySqlConfigType connectionType = sqlConfiguration.getMySqlConfigType();
        if(connectionType == MySqlConfigType.TCPIP) {
            return mySqlTcpConnector.getTcpConnection(sqlConfiguration);
        } else if (connectionType == MySqlConfigType.LDAP) {
            return  mySqlTcpConnector.getLdapConnection(sqlConfiguration);
        }
//        else if (connectionType == MySqlConfigType.LDAPSASLKERBEROS) {
            //TODO
//        }
        else if (connectionType == MySqlConfigType.NATIVEKERBEROS) {
            return mySqlTcpConnector.getNativeKerberosConnection(sqlConfiguration);
        } else {
            logger.error("Invalid Database configuration Type - {}", connectionType.getValue());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Invalid mysql auth type"), "DatabaseConfiguration-1000");
        }
    }

    @Override
    public ClientConnection getPostgreSqlConnection(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
        if(postgreSQLConfiguration == null) {
            logger.error("Invalid Database configuration");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"Invalid postgres configuration"), "DatabaseConfiguration-1001");
        }
        PostgresAuthType postgresAuthType = postgreSQLConfiguration.getPostgresAuthType();
        if(postgresAuthType == PostgresAuthType.USERNAMEPASSWORD) {
            return postgreSqlConnector.getTcpConnection(postgreSQLConfiguration);
        } else if(postgresAuthType == PostgresAuthType.LDAP) {
            return postgreSqlConnector.getTcpConnection(postgreSQLConfiguration);
        } else if(postgresAuthType == PostgresAuthType.RADIUS) {
            return postgreSqlConnector.getTcpConnection(postgreSQLConfiguration);
        }  else if(postgresAuthType == PostgresAuthType.GSSAPI) {
            return postgreSqlConnector.getGssApiConnection(postgreSQLConfiguration);
        }
//        else if(postgresAuthType == PostgresAuthType.PAM) {
            //TODO
//        }
        else{
            logger.error("Invalid Database configuration Type - {}", postgresAuthType.getValue());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Invalid postgresql auth type"), "DatabaseConfiguration-1000");
        }
    }

    @Override
    public ClientConnection getMongoDbConnection(MongoDBConfiguration mongoDBConfiguration)
        throws ItorixException {
        if(mongoDBConfiguration == null) {
            logger.error("Invalid Database configuration");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"Invalid MongoDb configuration"), "DatabaseConfiguration-1001");
        }
        if(mongoDBConfiguration.getAuthentication() == null) {
            logger.error("Invalid Mongodb Authentication configuration");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"Invalid MongoDb Authentication configuration"), "DatabaseConfiguration-1001");
        }
        MongoDbAuthType mongoDbAuthType = mongoDBConfiguration.getAuthentication().getMongoDbAuthType();
        if(mongoDbAuthType == MongoDbAuthType.none){
            return mongoDbConnector.getConnection(mongoDBConfiguration);
        } else if(mongoDbAuthType == MongoDbAuthType.userNamePassword){
            return mongoDbConnector.getConnectionByUsernamePassword(mongoDBConfiguration);
        } else if(mongoDbAuthType == MongoDbAuthType.x509){
            return mongoDbConnector.getX509Connection(mongoDBConfiguration);
        } else if(mongoDbAuthType == MongoDbAuthType.ldap){
            return mongoDbConnector.getLdapConnection(mongoDBConfiguration);
        } else if(mongoDbAuthType == MongoDbAuthType.kerberos){
            return mongoDbConnector.getKerberosConnection(mongoDBConfiguration);
        } else if(mongoDbAuthType == MongoDbAuthType.awsIAM){
            return mongoDbConnector.getConnectionByAwsIamAuth(mongoDBConfiguration);
        } else {
            logger.error("Invalid Database configuration Type - {}", mongoDbAuthType.getValue());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Invalid mongodb auth type"), "DatabaseConfiguration-1000");
        }
    }
}
