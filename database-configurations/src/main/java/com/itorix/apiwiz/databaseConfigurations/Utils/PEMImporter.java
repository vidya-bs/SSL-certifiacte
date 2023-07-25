package com.itorix.apiwiz.databaseConfigurations.Utils;

import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.bc.OpenSSHPrivateKeyFileBC;
import com.sshtools.common.ssh.components.SshKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
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
    final PrivateKey key = createPrivateKey(privateKeyPem);
    keystore.setKeyEntry("cert", key, DEFAULT_PASSWORD, cert);
    return keystore;
  }

  public KeyStore createTrustStore(String serverCa) throws Exception {
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(serverCa.getBytes()));
    X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
    trustStore.setCertificateEntry(SERVER_CA, cert);

    return trustStore;
  }

  public PrivateKey createPrivateKey(String privateKeyPem) throws Exception {
    if (privateKeyPem != null && !privateKeyPem.contains(BEGIN_PRIVATE_KEY)) {
      PrivateKey privateKey = convertPKCS1ToPKCS8(privateKeyPem);
      return privateKey;
    }
    privateKeyPem = privateKeyPem.replace(BEGIN_PRIVATE_KEY, "")
            .replace(END_PRIVATE_KEY, "");
    final byte[] bytes = DatatypeConverter.parseBase64Binary(privateKeyPem);
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
    } catch (Exception ex){
      logger.error("Exception occurred while converting openssh to rsa key format - ", ex);
      throw ex;
    }
    }
}