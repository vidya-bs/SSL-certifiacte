package com.itorix.apiwiz.databaseConfigurations.PostgreSql;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLSSL;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgresSslAuthType;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
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

    @PostConstruct
    public void createTempDirectory(){
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
        if (postgreSQLSsl.getSslMode() == PostgresSslAuthType.disable) {
            return;
        }
        if (postgreSQLSsl.getSslMode() != null) {
            properties.put("sslmode", postgreSQLSsl.getSslMode().getValue());
        }
        properties.put("useSSL", "true");
        long time = System.currentTimeMillis();

        String clientCertPath = TEMP_PATH + "/" + CLIENT_CERTIFICATE + "-" + time + ".pem";
        String clientKeyPath = TEMP_PATH + "/" + CLIENT_KEY + "-" + time + ".pem";
        String serverCaPath = TEMP_PATH + "/" + SERVER_CA + "-" + time + ".pem";
        try {
            FileUtils.writeStringToFile(new File(clientCertPath), postgreSQLSsl.getSslClientcert());
            FileUtils.writeStringToFile(new File(clientKeyPath), postgreSQLSsl.getSslClientcertkey());
            FileUtils.writeStringToFile(new File(serverCaPath), postgreSQLSsl.getSslRootcert());
        } catch (Exception ex) {
            logger.error("Exception Occurred - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "PostgreSql"), "DatabaseConfiguration-1002");
        }

        String clinetKeyDER = TEMP_PATH + "/" + CLIENT_KEY + "-" + time + ".der";
        String clientKeyPassword = postgreSQLSsl.getSslClientcertkeyPassWord();
        try {
            pemImporter.convertPEMToDER(postgreSQLSsl.getSslClientcertkey(), clinetKeyDER, clientKeyPassword);
        }  catch (ItorixException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error("Exception Occurred while converting pem to der format - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "PostgreSql"), "DatabaseConfiguration-1002");
        }

        List<String> certFiles = new ArrayList<>();
        certFiles.add(clientCertPath);
        certFiles.add(clientKeyPath);
        certFiles.add(serverCaPath);
        certFiles.add(clinetKeyDER);
        postgresConnection.setFilesToDelete(certFiles);

        properties.put(POSTGRES_SSLCERT, clientCertPath);
        properties.put(POSTGRES_SSLKEY, clinetKeyDER);
        properties.put(POSTGRES_SSLROOTCRT, serverCaPath);
    }
}