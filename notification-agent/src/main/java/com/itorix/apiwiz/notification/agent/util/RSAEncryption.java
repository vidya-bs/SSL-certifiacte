package com.itorix.apiwiz.notification.agent.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class RSAEncryption {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Cipher cipher;

    public RSAEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("RSA");
        try {
            this.loadKeys();
        } catch (Exception e) {
            log.error("Exception occurred",e);
        }
    }

    /**
     * @param msg
     * @param key
     * 
     * @return String
     * 
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    public String encryptText(String msg, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encodeBase64String(cipher.doFinal(msg.getBytes("UTF-8")));
    }

    /**
     * @param msg
     * @param key
     * 
     * @return String
     * 
     * @throws Exception
     */
    public String encryptText(String msg) throws Exception {
        return this.encryptText(msg, this.publicKey);
    }

    /**
     * @param msg
     * @param key
     * 
     * @return String
     * 
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String decryptText(String msg, PrivateKey key)
            throws InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
    }

    public String decryptText(String msg) throws Exception {
        return decryptText(msg, this.privateKey);
    }

    private String readKeyFromFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(RSAEncryption.class.getClassLoader().getResourceAsStream(filename)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        br.close();
        return sb.toString();
    }

    private void loadKeys() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, URISyntaxException {
        String privateKeyContent = readKeyFromFile("private_key_pkcs8.pem");
        String publicKeyContent = readKeyFromFile("public_key.pem");
        privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
        ;
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyContent));
        // X509EncodedKeySpec keySpecPKCS8 = new X509EncodedKeySpec(Base64.decodeBase64(privateKeyContent));
        privateKey = kf.generatePrivate(keySpecPKCS8);
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyContent));
        publicKey = kf.generatePublic(keySpecX509);
    }

    public static void main(String[] args) throws Exception {
        RSAEncryption ac = new RSAEncryption();
        // log.info(ac.decryptText("testkey1234"));
        // String msg = "Itorix@8102";
        // String encrypted_msg = ac.encryptText(msg);
        String decrypted_msg = ac.decryptText(
                "XeX0u28Ya1a3CBm5tihMQFteeA6fZTy8avUIsJ0WnNOaEAM90Hcv9G2xo5z5hI4WMyHffxTAbP2LcXK4u6n5Pw==");
        // log.info("Original Message: " + msg + "\nEncrypted Message: " + encrypted_msg + "\nDecrypted
        // Message: " + decrypted_msg);
        log.info(decrypted_msg);
    }

}
