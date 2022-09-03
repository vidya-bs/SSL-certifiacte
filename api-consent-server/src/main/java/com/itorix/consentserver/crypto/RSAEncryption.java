package com.itorix.consentserver.crypto;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
@Slf4j
@Component
public class RSAEncryption {

	private Cipher cipher = Cipher.getInstance("RSA");;

	public RSAEncryption() throws NoSuchPaddingException, NoSuchAlgorithmException {

	}


	public static void main(String[] args) throws Exception {
		RSAEncryption ac = new RSAEncryption();
		//ac.generateKeys();
		String encryptText = ac.encryptText("80264bbd-6f76-40c4-8c3a-ac70f8183c26", "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIT6ip1esuvC0L7pWYkD0aEAF5R7t/sb4Bg28idUO+kJ25nNkzHG+PcCT/3du60R3Ns0HKX9ouxly3IIH4FGWhcCAwEAAQ==");
		log.info(encryptText);
		//String decryptText = ac.decryptText("bYinFpZJmboLvxNCjtWgbyWAU3K9ThA0d1IL2wmR6vFa8x6w/78K8yjPh0HAbcADOrNKGMADyjvkex99/IYH8w==", "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAhPqKnV6y68LQvulZiQPRoQAXlHu3+xvgGDbyJ1Q76Qnbmc2TMcb49wJP/d27rRHc2zQcpf2i7GXLcggfgUZaFwIDAQABAkAEacg15tGrT2DcJSRqjqzjWfxuWhjGBavPQeuRcJM4baWct6ZJRBhphkN5HfvQeTIrDTP/ldh9gTCR9K6gXQ2JAiEA8uL+djXSYY2O52m39uA13U8H7ou5Ycc7xgKDUrBDR30CIQCMKHpAEa9ryh8igHAL1oJ4R6B2g4KCbZaIa3y6VP4kIwIgJnVMx1jkU+//JaM2rs93l/AwmFPlaZcLXuH8+zXPoV0CICm8J+MKIR1Mu4avji3IO2OLmJmRwXwLG+cVcreddWvXAiEAu3bwylOlvIZ8vFCn4GAJay8+YRS4EMfgZjylYZfJJj0=");
		//log.info("Decrypted Text " + decryptText);
	}

	public String encryptText(String msg, String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException {
		X509EncodedKeySpec ks = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey.getBytes(StandardCharsets.UTF_8)));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pub = kf.generatePublic(ks);
		this.cipher.init(ENCRYPT_MODE, pub);
		return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
	}

	public String decryptText(String msg, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException {
		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey.getBytes(StandardCharsets.UTF_8)));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey pub = kf.generatePrivate(ks);
		this.cipher.init(DECRYPT_MODE, pub);
		return new String(cipher.doFinal(Base64.getDecoder().decode(msg)), "UTF-8");
	}

	private void generateKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(512);

		KeyPair pair = generator.generateKeyPair();
		PrivateKey privateKey = pair.getPrivate();
		PublicKey publicKey = pair.getPublic();

		String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyStr =  Base64.getEncoder().encodeToString(publicKey.getEncoded());

		log.info(privateKeyStr);
		log.info(publicKeyStr);
	}
}
