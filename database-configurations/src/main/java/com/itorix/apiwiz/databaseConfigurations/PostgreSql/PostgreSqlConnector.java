package com.itorix.apiwiz.databaseConfigurations.PostgreSql;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.DriverManager;
import java.util.Properties;

@Component
public class PostgreSqlConnector {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlConnector.class);

    @Autowired
    private PostgreSqlSSLConnection sslConnection;

    @Autowired
    private PostgreSqlSSHConnection sshconnection;

    @Autowired
    private PostgresSqlKerberos postgresSqlKerberos;

    @Autowired
    private RSAEncryption rsaEncryption;

    public ClientConnection getTcpConnection(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
        validateData(postgreSQLConfiguration);
        String hostName = postgreSQLConfiguration.getPostgresqlHostname();
        String hostport = postgreSQLConfiguration.getPostgresqlPort();
        String username = postgreSQLConfiguration.getPostgresqlUsername();
        String password = postgreSQLConfiguration.getPostgresqlPassword();
        String databaseName = postgreSQLConfiguration.getPostgresqlDatabase();

        try {
            password = rsaEncryption.decryptText(password);
        } catch (Exception ex){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }

        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        properties.put("characterEncoding", "UTF-8");
        properties.put("useSSL", "false");

        ClientConnection postgresConnection = new ClientConnection();
        postgresConnection.setHost(hostName);
        postgresConnection.setPort(Integer.parseInt(hostport));
        try {
            if(postgreSQLConfiguration.getSsh() != null){
                sshconnection.prepareSshTunnel(postgreSQLConfiguration,postgresConnection );
            }
            if(postgreSQLConfiguration.getSsl() != null){
                sslConnection.buildProperties(properties, postgreSQLConfiguration.getSsl(), postgresConnection);
            }
            String hostUrl =String.format("jdbc:postgresql://%s:%s/%s", postgresConnection.getHost(), postgresConnection.getPort(), databaseName);
            logger.info("postgresql connection url - {}", hostUrl);
            postgresConnection.setConnection(DriverManager.getConnection(hostUrl, properties));
            return postgresConnection;
        } catch (ItorixException ex){
            postgresConnection.close();
            throw ex;
        } catch (Exception ex){
            postgresConnection.close();
            logger.error("Error Occured while connecting to postgres - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"PostgreSql"), "DatabaseConfiguration-1002");
        }
    }

    public ClientConnection getGssApiConnection(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
        validateData(postgreSQLConfiguration);
        validateKerberosData(postgreSQLConfiguration);
        String hostName = postgreSQLConfiguration.getPostgresqlHostname();
        String hostport = postgreSQLConfiguration.getPostgresqlPort();
        String username = postgreSQLConfiguration.getPostgresqlUsername();
        String password = postgreSQLConfiguration.getPostgresqlPassword();
        String databaseName = postgreSQLConfiguration.getPostgresqlDatabase();
        String kerberosRelam = postgreSQLConfiguration.getPostgresKerberosRelam();
        String kerberosKdcServer = postgreSQLConfiguration.getPostgresKerberosKdcServer();

        try {
            password = rsaEncryption.decryptText(password);
        } catch (Exception ex){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        properties.put("characterEncoding", "UTF-8");
        properties.put("useSSL", "false");

        ClientConnection postgresConnection = new ClientConnection();
        postgresConnection.setHost(hostName);
        postgresConnection.setPort(Integer.parseInt(hostport));
        try {
            if(postgreSQLConfiguration.getSsh() != null){
                sshconnection.prepareSshTunnel(postgreSQLConfiguration,postgresConnection);
            }
            if(postgreSQLConfiguration.getSsl() != null){
                sslConnection.buildProperties(properties, postgreSQLConfiguration.getSsl(), postgresConnection);
            }
            String hostUrl =String.format("jdbc:postgresql://%s:%s/%s", postgresConnection.getHost(), postgresConnection.getPort(), databaseName);
            logger.info("postgresql connection url - {}", hostUrl);
            postgresConnection.setConnection(postgresSqlKerberos.createConnection(properties, hostUrl, username, kerberosRelam, kerberosKdcServer));
            return postgresConnection;
        } catch (ItorixException ex){
            postgresConnection.close();
            throw ex;
        } catch (Exception ex){
            postgresConnection.close();
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"PostgreSql"), "DatabaseConfiguration-1002");
        }
    }

    private void validateData(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
        if(postgreSQLConfiguration.getPostgresqlHostname() == null || postgreSQLConfiguration.getPostgresqlHostname().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres hostname is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(postgreSQLConfiguration.getPostgresqlPort() == null || postgreSQLConfiguration.getPostgresqlPort().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres port is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(postgreSQLConfiguration.getPostgresqlUsername() == null || postgreSQLConfiguration.getPostgresqlUsername().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres username is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(postgreSQLConfiguration.getPostgresqlPassword() == null || postgreSQLConfiguration.getPostgresqlPassword().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres password is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(postgreSQLConfiguration.getPostgresqlDatabase() == null || postgreSQLConfiguration.getPostgresqlDatabase().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres databse is mandatory parameter"), "DatabaseConfiguration-1001");
        }
    }

    private void validateKerberosData(PostgreSQLConfiguration postgreSQLConfiguration) throws ItorixException {
        if(postgreSQLConfiguration.getPostgresKerberosRelam() == null || postgreSQLConfiguration.getPostgresKerberosRelam().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres kerberos Relam is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(postgreSQLConfiguration.getPostgresKerberosKdcServer() == null || postgreSQLConfiguration.getPostgresKerberosKdcServer().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"postgres kerberos kdc server is mandatory parameter"), "DatabaseConfiguration-1001");
        }
    }

}
