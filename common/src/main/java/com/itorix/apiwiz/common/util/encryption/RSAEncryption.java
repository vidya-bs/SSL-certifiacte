package com.itorix.apiwiz.common.util.encryption;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAEncryption {
	private static final Logger logger = LoggerFactory.getLogger(RSAEncryption.class);
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Cipher cipher;

	public RSAEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.cipher = Cipher.getInstance("RSA");
		try {
			this.loadKeys();
		} catch (Exception e) {
			logger.error("Exception occurred", e);
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
				.replace("-----END PUBLIC KEY-----", "");;
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyContent));
		// X509EncodedKeySpec keySpecPKCS8 = new
		// X509EncodedKeySpec(Base64.decodeBase64(privateKeyContent));
		privateKey = kf.generatePrivate(keySpecPKCS8);
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyContent));
		publicKey = kf.generatePublic(keySpecX509);
	}

	public static void main(String[] args) throws Exception {
		RSAEncryption ac = new RSAEncryption();
		logger.info(ac.decryptText(
				"CLnoEPujiZGwmoLvB0aVCGttQiU5F34xq18Jch8a1EDdC8ydxEmeSYPRtRQHnllMhf+SkNwxTSCo+LEazRdM4g=="));
		// logger.info(ac.decryptText(
		// "gnqqXJkKwJq8h+jcFNxgipqrYQ1QfKWJ/4mNfVPbiLwuaZ4orglap/Orga9zU3mzz8YcbjQ/ntWNy+XSL4WVjg=="));
		String msg = "Welcome";
		String encrypted_msg = ac.encryptText(msg);
		logger.info("enc_message : " + encrypted_msg);
		String msg1 = "3ekGp5Xd6eDlNeeZbpuuuKEnP81MInAbby9TTRnnStc";
		String encrypted_msg1 = ac.encryptText(msg1);
		String decrypted_msg = ac.decryptText(encrypted_msg);
		String decrypted_msg1 = ac.decryptText(encrypted_msg1);
		logger.info("Original Message: " + msg + msg1 + "\nEncrypted Message: " + encrypted_msg + encrypted_msg1
				+ "\nDecrypted Message: " + decrypted_msg + decrypted_msg1);
	}
}
