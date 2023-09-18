package com.itorix.apiwiz.databaseConfigurations.PostgreSql;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLSSL;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgresSslAuthType;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.databaseConfigurations.Utils.PEMImporter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class PostgreSqlSSLConnection {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlSSLConnection.class);

    @Value("${itorix.core.temp.directory}")
    private String TEMP_PATH;

    @Autowired
    PEMImporter pemImporter;

    @Autowired
    private RSAEncryption rsaEncryption;

    @PostConstruct
    public void createTempDirectory() {
        if (Files.notExists(Path.of(TEMP_PATH))) {
            try {
                Files.createDirectories(Path.of(TEMP_PATH));
            } catch (IOException e) {
                logger.error("Exception occurred ", e);
            }
        }
    }


    public void buildProperties(Properties properties, PostgreSQLSSL postgreSQLSsl, ClientConnection postgresConnection) throws IOException, ItorixException {

        // SSL Mode
        if (postgreSQLSsl.getSslMode() == null || postgreSQLSsl.getSslMode() == PostgresSslAuthType.disable) {
            return;
        }
        if (postgreSQLSsl.getSslMode() != null) {
            properties.put("sslmode", postgreSQLSsl.getSslMode().getValue());
        }
        properties.put("useSSL", "true");
        long time = System.currentTimeMillis();

        List<String> certFiles = new ArrayList<>();

        if (postgreSQLSsl.getSslClientcert() != null) {
            String clientCertPath = TEMP_PATH + File.separatorChar + CLIENT_CERTIFICATE + "-" + time + PEM_FILE_EXTENSION;
            createTempFile(clientCertPath, postgreSQLSsl.getSslClientcert());
            properties.put(POSTGRES_SSLCERT, clientCertPath);
            certFiles.add(clientCertPath);
        }
        if (postgreSQLSsl.getSslRootcert() != null) {
            String serverCaPath = TEMP_PATH + File.separatorChar + SERVER_CA + "-" + time + PEM_FILE_EXTENSION;
            createTempFile(serverCaPath, postgreSQLSsl.getSslRootcert());
            properties.put(POSTGRES_SSLROOTCRT, serverCaPath);
            certFiles.add(serverCaPath);
        }
        if (postgreSQLSsl.getCertRevocationlist() != null) {
            String clientCrlPath = TEMP_PATH + File.separatorChar + CLIENT_CRL + "-" + time + PEM_FILE_EXTENSION;
            createTempFile(clientCrlPath, postgreSQLSsl.getCertRevocationlist());
            properties.put(POSTGRES_CLIENTCRL, clientCrlPath);
            certFiles.add(clientCrlPath);
        }
        if (postgreSQLSsl.getSslClientcertkey() != null) {
            try {
                String clientKeyDER = TEMP_PATH + File.separatorChar + CLIENT_KEY + "-" + time + DER_FILE_EXTENSION;
                String clientKeyPassword = postgreSQLSsl.getSslClientcertkeyPassWord();
                if(clientKeyPassword != null && !clientKeyPassword.isEmpty() ) {
                    try {
                        clientKeyPassword = rsaEncryption.decryptText(clientKeyPassword);
                    } catch (Exception ex) {
                        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Postgresql! Unable to decrypt the client key password"), "DatabaseConfiguration-1002");
                    }
                }
                pemImporter.convertPEMToDER(postgreSQLSsl.getSslClientcertkey(), clientKeyDER, clientKeyPassword);
                properties.put(POSTGRES_SSLKEY, clientKeyDER);
                certFiles.add(clientKeyDER);
            } catch (ItorixException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.error("Exception Occurred while converting pem to der format - ", ex);
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "PostgreSql"), "DatabaseConfiguration-1002");
            }
        }
        postgresConnection.setFilesToDelete(certFiles);
    }


    public void createTempFile(String path, String content) throws ItorixException {
        try {
            FileUtils.writeStringToFile(new File(path), content, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error("Exception Occurred while creating file {} - \n error message- ", path, ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "PostgreSql"), "DatabaseConfiguration-1002");
        }
    }
}