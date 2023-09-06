package com.itorix.apiwiz.databaseConfigurations.Utils;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.bc.OpenSSHPrivateKeyFileBC;
import com.sshtools.common.ssh.components.SshKeyPair;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class PEMImporter {

    private static final Logger logger = LoggerFactory.getLogger(PEMImporter.class);

    public KeyStore createKeyStore(String privateKeyPem, String certificatePem)
            throws Exception {
        final X509Certificate[] cert = createCertificates(certificatePem);
        final KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);
        // Import private key
        final PrivateKey key = createPrivateKey(privateKeyPem, null);
        keystore.setKeyEntry("cert", key, DEFAULT_PASSWORD, cert);
        return keystore;
    }

    public KeyStore createTrustStore(String serverCa) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(serverCa.getBytes()));
        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        trustStore.setCertificateEntry(SERVER_CA, cert);

        return trustStore;
    }

    public PrivateKey createPrivateKey(String privateKeyPem, String clientKeyPassword) throws Exception {
        if (privateKeyPem != null && privateKeyPem.contains(BEGIN_RSA_PRIVATE_KEY)) {
            PrivateKey privateKey = convertPKCS1ToPKCS8(privateKeyPem);
            return privateKey;
        }
        privateKeyPem = privateKeyPem.replace(BEGIN_PRIVATE_KEY, "")
                .replace(END_PRIVATE_KEY, "")
                .replace(BEGIN_ENCRYPTED_PRIVATE_KEY, "")
                .replace(END_ENCRYPTED_PRIVATE_KEY, "");
        final byte[] bytes = DatatypeConverter.parseBase64Binary(privateKeyPem);
        if (clientKeyPassword != null) {
            return generatePrivateKeyFromDER(bytes, clientKeyPassword.toCharArray());
        }
        return generatePrivateKeyFromDER(bytes);
    }

    private X509Certificate[] createCertificates(String certificatePem) throws Exception {
        final List<X509Certificate> result = new ArrayList<X509Certificate>();
        if (certificatePem == null || !certificatePem.contains(BEGIN_CERTIFICATE)) {
            throw new IllegalArgumentException("No CERTIFICATE found");
        }
        certificatePem = certificatePem.replace(BEGIN_CERTIFICATE, "")
                .replace(END_CERTIFICATE, "");
        final byte[] bytes = DatatypeConverter.parseBase64Binary(certificatePem);
        X509Certificate cert = generateCertificateFromDER(bytes);
        result.add(cert);
        return result.toArray(new X509Certificate[result.size()]);
    }

    private RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        final KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes, char[] password) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        PBEKeySpec pbeSpec = new PBEKeySpec(password);
        EncryptedPrivateKeyInfo pkinfo = new EncryptedPrivateKeyInfo(keyBytes);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(pkinfo.getAlgName());
        Key secret = skf.generateSecret(pbeSpec);
        PKCS8EncodedKeySpec keySpec = pkinfo.getKeySpec(secret);
        final KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(keySpec);
    }

    private X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        final CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }


    //Convert RSA or pkcs1 to pkcs12
    public PrivateKey convertPKCS1ToPKCS8(String privateKeyContent) throws Exception {

        privateKeyContent = privateKeyContent.replaceAll("\\n", "")
                .replace(BEGIN_RSA_PRIVATE_KEY, "")
                .replace(END_RSA_PRIVATE_KEY, "")
                .replaceAll("\\s", "");
        byte[] cipherBytes = Base64.getDecoder().decode(privateKeyContent);
        KeySpec spec = new PKCS8EncodedKeySpec(cipherBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA", new BouncyCastleProvider());
        PrivateKey privateKey = kf.generatePrivate(spec);
        return privateKey;
    }

    public String convertOpenSSHtoRSA(String privateKey) throws InvalidPassphraseException, IOException {
        try {
            SshKeyPair key = SshKeyUtils.getPrivateKey(privateKey, "");
            String rsaKey = new String(new OpenSSHPrivateKeyFileBC(key, "").getFormattedKey());
            return rsaKey;
        } catch (Exception ex) {
            logger.error("Exception occurred while converting openssh to rsa key format - ", ex);
            throw ex;
        }
    }

    public void convertPEMToDER(String input, String output, String password) throws ItorixException {
        try (FileOutputStream outputStream = new FileOutputStream(output)) {
            byte[] privateKey = stringToPrivateKey(input, password).getEncoded();
            outputStream.write(privateKey, 0, privateKey.length);
        } catch (ItorixException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception occurred - ", e);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), e.getMessage()), "DatabaseConfiguration-1000");
        }
    }


    public PrivateKey stringToPrivateKey(String s, String password)
            throws IOException, PKCSException, ItorixException {
        PrivateKeyInfo pki;
        try (PEMParser pemParser = new PEMParser(new StringReader(s))) {
            Object o = pemParser.readObject();
            if (o instanceof PKCS8EncryptedPrivateKeyInfo) { // encrypted private key in pkcs8-format
                logger.debug("key in pkcs8 encoding");
                PKCS8EncryptedPrivateKeyInfo epki = (PKCS8EncryptedPrivateKeyInfo) o;
                logger.debug("encryption algorithm: " + epki.getEncryptionAlgorithm().getAlgorithm());
                JcePKCSPBEInputDecryptorProviderBuilder builder =
                        new JcePKCSPBEInputDecryptorProviderBuilder().setProvider("BC");
                if (password == null) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "client key is encrypted password is required to decrypt"), "DatabaseConfiguration-1000");
                }
                InputDecryptorProvider idp = builder.build(password.toCharArray());
                pki = epki.decryptPrivateKeyInfo(idp);
            } else if (o instanceof PEMEncryptedKeyPair) { // encrypted private key in pkcs1-format
                logger.debug("key in pkcs1 encoding");
                PEMEncryptedKeyPair epki = (PEMEncryptedKeyPair) o;
                logger.debug("encryption algorithm: " + epki.getDekAlgName());
                if (password == null) {
                    throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"), "client key is encrypted password is required to decrypt"), "DatabaseConfiguration-1000");
                }
                PEMKeyPair pkp = epki.decryptKeyPair(new BcPEMDecryptorProvider(password.toCharArray()));
                pki = pkp.getPrivateKeyInfo();
            } else if (o instanceof PrivateKeyInfo) {
                logger.debug("key in pkcs10 encoding");
                PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) o;
                privateKeyInfo.getPrivateKeyAlgorithm();
                logger.debug("encryption algorithm: " + privateKeyInfo.getPrivateKeyAlgorithm());
                pki = privateKeyInfo;
            } else if (o instanceof PEMKeyPair) { // unencrypted private key
                logger.debug("key unencrypted");
                PEMKeyPair pkp = (PEMKeyPair) o;
                pki = pkp.getPrivateKeyInfo();
            } else {
                throw new PKCSException("Invalid encrypted private key class: " + o.getClass().getName());
            }
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            return converter.getPrivateKey(pki);
        }
    }
}