package com.itorix.apiwiz.validator.license.crypto;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.HybridConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Component
//@ConditionalOnProperty(prefix = "license", name = "check", havingValue = "true")
public class HybridDecryption {

	@Autowired
	private ResourceLoader resourceLoader;

	private KeysetHandle keysetHandle;

	@PostConstruct
	public void loadKey() throws IOException, GeneralSecurityException {
		keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader
				.withInputStream(resourceLoader.getResource("classpath:private.json").getInputStream()));
	}


	public String decrypt(String encryptedText) throws GeneralSecurityException {
		HybridConfig.register();
		HybridDecrypt hybridDecrypt = keysetHandle.getPrimitive(HybridDecrypt.class);
		return new String(hybridDecrypt.decrypt(Base64.getDecoder().decode(encryptedText.getBytes(StandardCharsets.UTF_8)), null));
	}

}
