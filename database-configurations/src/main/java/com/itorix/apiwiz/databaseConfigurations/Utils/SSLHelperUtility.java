package com.itorix.apiwiz.databaseConfigurations.Utils;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
@Slf4j
public class SSLHelperUtility {

    @Autowired
    PEMImporter pemImporter;

    @Value("${itorix.core.temp.directory}")
    private String KEY_STORE_DIRECTORY_PATH;


    @PostConstruct
    public void createTempDirectory(){
        if (Files.notExists(Path.of(KEY_STORE_DIRECTORY_PATH))) {
            try {
                Files.createDirectories(Path.of(KEY_STORE_DIRECTORY_PATH));
            } catch (IOException e) {
                log.error("Exception occurred ", e);
            }
        }
    }
    public SSLContext CreateKeystoreAndGetSSLContext(String caCert, String clientCert, String clientKey, ClientConnection clientConnection) throws Exception {
        KeyStore keyStore = createKeyStoreFromCerts(caCert, clientCert, clientKey, clientConnection);
        return getSSLContext(keyStore, caCert == null);
    }

    public KeyStore createKeyStoreFromCerts(String caCert, String clientCert, String clientKey, ClientConnection clientConnection) throws Exception {
        byte[] caCertData = readCertificates(caCert);
        byte[] clientCertData = readCertificates(clientCert);
        byte[] clientKeyData = readCertificates(clientKey);

        KeyStore keyStore = null;
        if (caCertData != null || clientCertData != null) {
            keyStore = createKeyStore(caCertData, clientCertData, clientKeyData, clientConnection);
        } else {
            log.error("Exception Occurred while establishing mongodb SSL connection Ca cert cannot be null");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb, ca cert cannot be null"), "DatabaseConfiguration-1002");
        }
        return keyStore;
    }

    private SSLContext getSSLContext(KeyStore keyStore, boolean isSelfSigned) throws Exception {

        SSLContext sslContext;

        try {
            // Retrieve Key Managers from the Client certificate Key Store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, DEFAULT_PASSWORD);
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // Initialize Trust Manager
            TrustManager[] trustManagers =  null;
            if (!isSelfSigned) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
                trustManagers = tmf.getTrustManagers();
            }
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
        } catch (Exception ex) {
            throw ex;
        }
        return sslContext;
    }


    public byte[] readCertificates(String cert) {
        if (cert != null && !cert.isEmpty()) {
            return cert.getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    public KeyStore createKeyStore(byte[] caCertData, byte[] clientCertData, byte[] clientKeyData, ClientConnection clientConnection) throws Exception {
        KeyStore keyStore = createKeyStore();
        try {
            CertificateFactory clientFactory = CertificateFactory.getInstance("X.509");
            List<Certificate> certificates = new ArrayList<>();
            if (caCertData != null) {
                java.security.cert.Certificate caCert = clientFactory.generateCertificate(new ByteArrayInputStream(caCertData));
                keyStore.setCertificateEntry(CA_CERTIFICATE, caCert);
            }
            if (clientCertData != null) {
                java.security.cert.Certificate clientCert = clientFactory.generateCertificate(new ByteArrayInputStream(clientCertData));
                keyStore.setCertificateEntry(CLIENT_CERTIFICATE, clientCert);
                certificates.add(clientCert);
            }
            if (clientKeyData != null) {
                PrivateKey privateKey = pemImporter.createPrivateKey(new String(clientKeyData));
                keyStore.setKeyEntry(CLIENT_KEY, privateKey, DEFAULT_PASSWORD, certificates.toArray(new Certificate[certificates.size()]));
            }

            return persistKeystore(keyStore, clientConnection);
        } catch (Exception e) {
            throw e;
        }

    }

    private KeyStore createKeyStore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, DEFAULT_PASSWORD);
        return keyStore;
    }

    private KeyStore persistKeystore(KeyStore keyStore, ClientConnection clientConnection) throws Exception {
        File keyStoreFile = getKeystoreFile();
        try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
            keyStore.store(fos, DEFAULT_PASSWORD);
        }
        List<String> certFiles = new ArrayList<>();
        certFiles.add(keyStoreFile.getAbsolutePath());
        clientConnection.setFilesToDelete(certFiles);
        return keyStore;
    }

    public File getKeystoreFile() {
        return new File(KEY_STORE_DIRECTORY_PATH + "/keystore-" + System.currentTimeMillis() + KEYSTORE_EXTENSION);
    }
}
