package com.itorix.apiwiz.databaseConfigurations.MySql;

import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.SslAuthType;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.databaseConfigurations.Utils.KerberosConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class MySqlConnector {

    private static final Logger logger = LoggerFactory.getLogger(MySqlConnector.class);

    @Autowired
    private MySqlSSLConnection sslConnection;

    @Autowired
    private MySqlSSHConnection sshconnection;

    @Autowired
    private KerberosConnector kerberosConnector;

    @Autowired
    private RSAEncryption rsaEncryption;

    public Connection getTcpConnection( MySQLConfiguration sqlConfiguration) throws  ItorixException {
        validateData(sqlConfiguration);
        String hostName = sqlConfiguration.getMysqlHostname();
        String hostport = sqlConfiguration.getMysqlPort();
        String username = sqlConfiguration.getMysqlUserName();
        String password = sqlConfiguration.getMysqlPassword();
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

        try {
            int allocatedPort = Integer.parseInt(hostport);
            if(sqlConfiguration.getSsh() != null) {
                allocatedPort = sshconnection.prepareSshTunnel(sqlConfiguration);
                hostName = "localhost";
            }
            if(sqlConfiguration.getSsl() != null && sqlConfiguration.getSsl().getSslMode() != SslAuthType.DISABLED ){
                sslConnection.buildProperties(properties, sqlConfiguration.getSsl());
            }
            String hostUrl =String.format("jdbc:mysql://%s:%s", hostName, allocatedPort);
            logger.info("postgresql connection url - {}", hostUrl);
            return DriverManager.getConnection(hostUrl, properties);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            logger.error("Exception Occurred while establishing mysql connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql"), "DatabaseConfiguration-1002");
        }
    }

    public Connection getLdapConnection( MySQLConfiguration sqlConfiguration) throws ItorixException {
        validateData(sqlConfiguration);
        String hostName = sqlConfiguration.getMysqlHostname();
        String hostport = sqlConfiguration.getMysqlPort();
        String username = sqlConfiguration.getMysqlUserName();
        String password = sqlConfiguration.getMysqlPassword();

        try {
            password = rsaEncryption.decryptText(password);
        } catch (Exception ex){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }

        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        properties.put("characterEncoding", "UTF-8");
        properties.setProperty("enableClearTextPlugin", "true");

        try {
            int allocatedPort = Integer.parseInt(hostport);
            if(sqlConfiguration.getSsh() != null ){
                allocatedPort = sshconnection.prepareSshTunnel(sqlConfiguration);
                hostName = "localhost";
            }
            if(sqlConfiguration.getSsl() != null && sqlConfiguration.getSsl().getSslMode() != SslAuthType.DISABLED) {
                sslConnection.buildProperties(properties, sqlConfiguration.getSsl());
            }
            String hostUrl =String.format("jdbc:mysql://%s:%s", hostName, allocatedPort);
            logger.info("mysql connection url - {}", hostUrl);
            return DriverManager.getConnection(hostUrl, properties);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            logger.error("Exception Occurred while establishing mysql ldap connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql"), "DatabaseConfiguration-1002");
        }
    }

    public Connection getNativeKerberosConnection( MySQLConfiguration sqlConfiguration) throws ItorixException {
        validateData(sqlConfiguration);
        validateDataKerberosData(sqlConfiguration);
        String hostName = sqlConfiguration.getMysqlHostname();
        String hostport = sqlConfiguration.getMysqlPort();
        String username = sqlConfiguration.getMysqlUserName();
        String password = sqlConfiguration.getMysqlPassword();
        String relam = sqlConfiguration.getRelam();
        String kdcServerhost = sqlConfiguration.getKdcServerhost();

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
        try {
            int allocatedPort = Integer.parseInt(hostport);
            if(sqlConfiguration.getSsh() != null ){
                allocatedPort = sshconnection.prepareSshTunnel(sqlConfiguration);
                hostName = "localhost";
            }
            if(sqlConfiguration.getSsl() != null && sqlConfiguration.getSsl().getSslMode() != SslAuthType.DISABLED) {
                sslConnection.buildProperties(properties, sqlConfiguration.getSsl());
            }
            String hostUrl =String.format("jdbc:mysql://%s:%s", hostName, allocatedPort);
            logger.info("mysql connection url - {}", hostUrl);
            kerberosConnector.CreateKerberosTicket(username, password, relam, kdcServerhost);

            System.setProperty( AUTH_LOGIN_CONFIG, createJassFile());
            return DriverManager.getConnection(hostUrl, properties);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            ex.printStackTrace();
            logger.error("Exception Occurred while establishing mysql kerberos connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql"), "DatabaseConfiguration-1002");
        }
    }


    private void validateData(MySQLConfiguration sqlConfiguration) throws ItorixException {
        if(sqlConfiguration.getMysqlHostname() == null || sqlConfiguration.getMysqlHostname().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"mysql hostname is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(sqlConfiguration.getMysqlPort() == null || sqlConfiguration.getMysqlPort().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"mysql port is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(sqlConfiguration.getMysqlUserName() == null || sqlConfiguration.getMysqlUserName().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"mysql username is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if(sqlConfiguration.getMysqlPassword() == null || sqlConfiguration.getMysqlPassword().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"mysql password is mandatory parameter"), "DatabaseConfiguration-1001");
        }

    }

    private void validateDataKerberosData(MySQLConfiguration sqlConfiguration) throws ItorixException {
        if( sqlConfiguration.getRelam() == null || sqlConfiguration.getRelam().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"kerberos Relam is mandatory parameter"), "DatabaseConfiguration-1001");
        }
        if( sqlConfiguration.getKdcServerhost() == null || sqlConfiguration.getKdcServerhost().equals("")){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1001"),"kerberos kdc server is mandatory parameter"), "DatabaseConfiguration-1001");
        }
    }


    public String createJassFile(){
        final File jaasConfFile;
        try
        {
            jaasConfFile = File.createTempFile(MYSQL_JASS_FILE_NAME, null);
            final PrintStream bos = new PrintStream(new FileOutputStream(jaasConfFile));
            bos.print(MYSQL_JASS_FILE);
            bos.close();
            jaasConfFile.deleteOnExit();
        } catch (final IOException ex) {
            throw new IOError(ex);
        }
        logger.debug("mysql jass file path {}", jaasConfFile.getAbsolutePath());
        return jaasConfFile.getAbsolutePath();
    }
}
