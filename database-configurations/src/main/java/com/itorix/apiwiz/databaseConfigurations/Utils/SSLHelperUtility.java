package com.itorix.apiwiz.databaseConfigurations.Utils;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.*;
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

    @Autowired
    private RSAEncryption rsaEncryption;


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
    public SSLContext CreateKeystoreAndGetSSLContext(MongoDBConfiguration mongoDBConfiguration, ClientConnection clientConnection) throws Exception {
        String caCert = mongoDBConfiguration.getSsl().getCertificateAuthority();
        String clientKey = mongoDBConfiguration.getSsl().getClientKey();
        String clientCert = mongoDBConfiguration.getSsl().getClientCertificate();
        String clientKeyPassword = mongoDBConfiguration.getSsl().getClientKeyPassword();
        if(clientKeyPassword != null && !clientKeyPassword.isEmpty() ) {
            try {
                clientKeyPassword = rsaEncryption.decryptText(clientKeyPassword);
            } catch (Exception ex) {
                throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"), "Postgresql! Unable to decrypt the client key password"), "DatabaseConfiguration-1002");
            }
        }
        KeyStore keyStore = createKeyStoreFromCerts(caCert, clientCert, clientKey,clientKeyPassword, clientConnection);
        return getSSLContext(keyStore, caCert == null);
    }

    public KeyStore createKeyStoreFromCerts(String caCert, String clientCert, String clientKey, String clientKeyPassword, ClientConnection clientConnection) throws Exception {
        byte[] caCertData = readCertificates(caCert);
        byte[] clientCertData = readCertificates(clientCert);
        byte[] clientKeyData = readCertificates(clientKey);

        if (caCertData != null || clientCertData != null) {
            return createKeyStore(caCertData, clientCertData, clientKeyData, clientKeyPassword, clientConnection);
        } else {
            log.error("Exception Occurred while establishing mongodb SSL connection Ca cert cannot be null");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"MongoDb, ca cert cannot be null"), "DatabaseConfiguration-1002");
        }
    }

    private SSLContext getSSLContext(KeyStore keyStore, boolean isSelfSigned) throws Exception {

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
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        } catch (Exception ex) {
            throw ex;
        }
    }


    public byte[] readCertificates(String cert) {
        if (cert != null && !cert.isEmpty()) {
            return cert.getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    public KeyStore createKeyStore(byte[] caCertData, byte[] clientCertData, byte[] clientKeyData, String clientKeyPassword, ClientConnection clientConnection) throws Exception {
        KeyStore keyStore = createKeyStore();
        try {
            CertificateFactory clientFactory = CertificateFactory.getInstance("X.509");
            List<Certificate> certificates = new ArrayList<>();
            if (caCertData != null) {
                Certificate caCert = clientFactory.generateCertificate(new ByteArrayInputStream(caCertData));
                keyStore.setCertificateEntry(CA_CERTIFICATE, caCert);
            }
            if (clientCertData != null) {
                Certificate clientCert = clientFactory.generateCertificate(new ByteArrayInputStream(clientCertData));
                keyStore.setCertificateEntry(CLIENT_CERTIFICATE, clientCert);
                certificates.add(clientCert);
            }
            if (clientKeyData != null) {
                PrivateKey privateKey = pemImporter.stringToPrivateKey(new String(clientKeyData), clientKeyPassword);
                keyStore.setKeyEntry(CLIENT_KEY, privateKey, DEFAULT_PASSWORD, certificates.toArray(new Certificate[certificates.size()]));
            }

            return persistKeystore(keyStore, clientConnection);
        } catch (Exception e) {
            log.error("Exception occurred - ", e);
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
