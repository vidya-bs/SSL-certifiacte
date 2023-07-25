package com.itorix.apiwiz.databaseConfigurations.MongoDb;

import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoAuthentication;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoSSH;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.databaseConfigurations.Utils.MongoKerberosConnector;
import com.itorix.apiwiz.databaseConfigurations.Utils.MongoSOCKS5Connector;
import com.itorix.apiwiz.databaseConfigurations.Utils.SSLHelperUtility;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.KeyStore;


@Component
public class MongoDbConnector {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbConnector.class);

    @Autowired
    private MongoDbSSHConnection sshConnection;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private SSLHelperUtility sslHelperUtility;

    @Autowired
    private MongoSOCKS5Connector mongoSOCKS5Connector;

    @Autowired
    private MongoKerberosConnector mongoKerberosConnector;

    public MongoClient getConnection(MongoDBConfiguration mongoDBConfiguration)
        throws ItorixException {
        if(mongoDBConfiguration.getAuthentication() == null){
            logger.error("Invalid mongodb Authentication - {}", mongoDBConfiguration.getAuthentication());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MongoDbAuthentication is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        String url = mongoDBConfiguration.getUrl();
        if(url ==  null || url.isEmpty()){
            logger.error("Invalid mongoDb url - {}", url);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connection url is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        try {
            if(mongoDBConfiguration.getSsh() != null){
                return getSSHConnection(mongoDBConfiguration);
            }
            return createMongoClient(url);
        } catch (Exception ex){
            logger.error("Exception creating MongoDBClient - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb"), "DatabaseConfiguration-1002");
        }
    }

    public MongoClient getConnectionByUsernamePassword(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
        try {
            if (mongoDBConfiguration.getAuthentication() == null) {
                logger.error("Invalid mongodb Authentication - {}", mongoDBConfiguration.getAuthentication());
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "MongoDbAuthentication is mandatory parameter but missing"), "DatabaseConfiguration-1000");
            }
            String url = mongoDBConfiguration.getUrl();
            if(url ==  null || url.isEmpty()){
                logger.error("Invalid mongoDb url - {}", url);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connection url is mandatory parameter but missing"), "DatabaseConfiguration-1000");
            }
            if(mongoDBConfiguration.getSsh() != null){
                return getSSHConnection(mongoDBConfiguration);
            }
            return createMongoClient(url);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            ex.printStackTrace();
            logger.error("Exception creating MongoDBClient - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb"), "DatabaseConfiguration-1002");
        }
    }

    public MongoClient getSSLConnection(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
        if(mongoDBConfiguration.getAuthentication() == null){
            logger.error("Invalid mongodb Authentication - {}", mongoDBConfiguration.getAuthentication());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MongoDbAuthentication is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        String url = mongoDBConfiguration.getUrl();
        if(url ==  null || url.isEmpty()){
            logger.error("Invalid mongoDb url - {}", url);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Connection url is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }

        // TODO check x509 otherwise tls/ssl connection
//        checkAndAppendSSLQuery(url);
        try {
            String caCert = mongoDBConfiguration.getSsl().getCertificateAuthority();
            String clientKey = mongoDBConfiguration.getSsl().getClientKey();
            String clientCert = mongoDBConfiguration.getSsl().getClientCertificate();

            SSLContext sslContext = sslHelperUtility.CreateKeystoreAndGetSSLContext(caCert,clientCert, clientKey);

            if(mongoDBConfiguration.getSsh() != null){
                return getSSHConnection(mongoDBConfiguration);
            }

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(url))
                    .applyToSslSettings(builder -> builder.context(sslContext))
                    .build();
            return createMongoClient(settings);
        } catch (ItorixException ex){
            throw ex;
        }  catch (Exception ex){
            logger.error("Exception Occurred while establishing mongodb SSL connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb"), "DatabaseConfiguration-1002");
        }
    }

    public MongoClient getSSHConnection(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
        try {
            String url = mongoDBConfiguration.getUrl();
            MongoSSH mongoSSH = mongoDBConfiguration.getSsh();
//            if(mongoSSH.getSshAuthType()== MongoDbSshAuthType.SOCKS5){ //socks5 manual connector
//                return mongoSOCKS5Connector.connect(mongoDBConfiguration);
//            }
            //ssh connections
            int allocatedPort = sshConnection.prepareSshTunnel(mongoDBConfiguration);
            String host = mongoDBConfiguration.getHost();
            String[] hosts = host.split(":");
            url = url.replace(hosts[0], "localhost").replace(hosts[1], String.valueOf(allocatedPort));
            return createMongoClient(url);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            logger.error("Exception Occurred while establishing mongodb SSH connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb"), "DatabaseConfiguration-1002");
        }
    }

    public MongoClient getLdapConnection(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
        MongoAuthentication mongoAuthentication = mongoDBConfiguration.getAuthentication();
        if(mongoAuthentication == null){
            logger.error("Invalid mongodb Authentication - {}", mongoDBConfiguration.getAuthentication());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MongoDbAuthentication is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        if(mongoAuthentication.getLdapUsername() == null && StringUtils.isEmpty(mongoAuthentication.getUsername())){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Ldap Username is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        if(mongoAuthentication.getLdapPassword() == null && StringUtils.isEmpty(mongoAuthentication.getLdapPassword())){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Ldap Password is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }

        String url = mongoDBConfiguration.getUrl();
        try{
            if(mongoDBConfiguration.getSsh() != null){
                return getSSHConnection(mongoDBConfiguration);
            }
            return createMongoClient(url);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            logger.error("Exception Occurred while establishing mongodb Ldap connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb"), "DatabaseConfiguration-1002");
        }
    }

    public MongoClient getKerberosConnection(MongoDBConfiguration mongoDBConfiguration) throws ItorixException {
        MongoAuthentication mongoAuthentication = mongoDBConfiguration.getAuthentication();
        if(mongoAuthentication == null){
            logger.error("Invalid mongodb Authentication - {}", mongoDBConfiguration.getAuthentication());
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MongoDbAuthentication is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        if(mongoAuthentication.getKerberosUserPrincipal() == null && StringUtils.isEmpty(mongoAuthentication.getKerberosUserPrincipal())){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Kerberos Username is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        if(mongoAuthentication.getKerberosUserPassword() == null && StringUtils.isEmpty(mongoAuthentication.getKerberosUserPassword())){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Kerberos Password is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }
        if(mongoAuthentication.getKerberosServerUrl() == null && StringUtils.isEmpty(mongoAuthentication.getKerberosServerUrl())){
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Kerberos Server url is mandatory parameter but missing"), "DatabaseConfiguration-1000");
        }

        String kerberosUserPrincipal = mongoAuthentication.getKerberosUserPrincipal();
        String kerberosUserPassword = mongoAuthentication.getKerberosUserPassword();
        String url = mongoDBConfiguration.getUrl();
        String kerberosServer = mongoAuthentication.getKerberosServerUrl();
        String kerberosServiceRealm = mongoAuthentication.getKerberosServiceRealm();
        try{
            //TODO parse url to get host
            return mongoKerberosConnector.kerberosAuth(url, kerberosServiceRealm, kerberosServer, kerberosUserPrincipal, kerberosUserPassword);
        } catch (ItorixException ex){
            throw ex;
        } catch (Exception ex){
            logger.error("Exception Occurred while establishing mongodb Ldap connection - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb"), "DatabaseConfiguration-1002");
        }
    }
    private MongoClient createMongoClient(String url) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(url)).build();
        return MongoClients.create(settings);
    }
    private MongoClient createMongoClient(MongoClientSettings settings) {
        return MongoClients.create(settings);
    }

}
