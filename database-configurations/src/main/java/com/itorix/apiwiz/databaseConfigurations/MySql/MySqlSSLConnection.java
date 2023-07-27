package com.itorix.apiwiz.databaseConfigurations.MySql;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySqlSSL;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.SslAuthType;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class MySqlSSLConnection {

  private static final Logger logger = LoggerFactory.getLogger(MySqlSSLConnection.class);

  @Value("${itorix.core.temp.directory}")
  private String KEY_STORE_DIRECTORY_PATH;

  @Autowired
  PEMImporter pemImporter;

  @PostConstruct
  public void createTempDirectory(){
    if (Files.notExists(Path.of(KEY_STORE_DIRECTORY_PATH))) {
      try {
        Files.createDirectories(Path.of(KEY_STORE_DIRECTORY_PATH));
      } catch (IOException e) {
        logger.error("Exception occurred ", e);
      }
    }
  }

  public void buildProperties(Properties properties, MySqlSSL mySqlSsl, ClientConnection mysqlConnection) throws Exception {

    try {
      // SSL Mode
      properties.put("sslMode", mySqlSsl.getSslMode());
      properties.put("useSSL", "true");

      if (SslAuthType.VERIFY_CA == mySqlSsl.getSslMode()
              || SslAuthType.VERIFY_IDENTITY == mySqlSsl.getSslMode() || SslAuthType.REQUIRED == mySqlSsl.getSslMode()) {
        FileUtils.forceMkdir(Path.of(KEY_STORE_DIRECTORY_PATH).toFile());

        Path keyStoreFilePath = Path.of(KEY_STORE_DIRECTORY_PATH).resolve("KEY_STORE_FILE_NAME" + KEYSTORE_EXTENSION);
        Path trustStoreFilePath = Path.of(KEY_STORE_DIRECTORY_PATH).resolve("TRUST_STORE_FILE_NAME" + KEYSTORE_EXTENSION);

        createKeyStoreFile(keyStoreFilePath, trustStoreFilePath, mySqlSsl);

        List<String> certFiles = new ArrayList<>();
        certFiles.add(keyStoreFilePath.toUri().toURL().toString());
        certFiles.add(trustStoreFilePath.toUri().toURL().toString());
        mysqlConnection.setFilesToDelete(certFiles);

        properties.put("clientCertificateKeyStoreUrl", keyStoreFilePath.toUri().toURL().toString());
        properties.put("trustCertificateKeyStoreUrl", trustStoreFilePath.toUri().toURL().toString());
      }
    } catch (ItorixException ex){
      throw ex;
    } catch (Exception ex){
      logger.error("Exception Occured - ", ex);
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"Unable to establish ssl connection"), "DatabaseConfiguration-1000");
    }
  }

  private void createKeyStoreFile(Path keyStoreFilePath, Path trustStoreFilePath, MySqlSSL mySqlSsl) throws Exception {

    String clientKey = mySqlSsl.getSslKeyfile();
    String clientCertificate = mySqlSsl.getSslCertfile();
    String serverCA = mySqlSsl.getSslCafile();

    KeyStore keyStore = pemImporter.createKeyStore(clientKey, clientCertificate);
    try (FileOutputStream fos = new FileOutputStream(keyStoreFilePath.toFile())) {
      keyStore.store(fos, DEFAULT_PASSWORD);
    }

    KeyStore trustStore = pemImporter.createTrustStore(serverCA);
    try (FileOutputStream fos = new FileOutputStream(trustStoreFilePath.toFile())) {
      trustStore.store(fos, DEFAULT_PASSWORD);
    }

  }
}