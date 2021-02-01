package com.itorix.apiwiz.common.util.encryption;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacSHA256 {
	public static final String HMAC_SHA256 = "HmacSHA256";

	public static String generate( String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
		if (key == null || data == null) throw new NullPointerException();
		final Mac hMacSHA256 = Mac.getInstance(HMAC_SHA256);
		byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
		final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, HMAC_SHA256);
		hMacSHA256.init(secretKey);
		byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
		byte[] res = hMacSHA256.doFinal(dataBytes);
		return Base64.encodeBase64String(res);
	}

	public static String hmacDigest(String msg, String keyString) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException{
		String digest = null;
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), HMAC_SHA256);
		Mac mac = Mac.getInstance(HMAC_SHA256);
		mac.init(key);
		byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
		StringBuffer hash = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hash.append('0');
			}
			hash.append(hex);
		}
		digest = hash.toString();
		return digest;
	}
}