package com.itorix.apiwiz.validator.license.crypto;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.HybridConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Component
public class HybridEncryption {

	KeysetHandle keysetHandle;

	@PostConstruct
	private void loadKey() throws IOException, GeneralSecurityException {
		keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader
				.withFile(new File(this.getClass().getClassLoader().getResource("public.json").getFile())));
	}


	public String encrypt(String plainText) throws GeneralSecurityException {
		HybridConfig.register();
		HybridEncrypt hybridEncrypt = keysetHandle.getPrimitive(HybridEncrypt.class);
		return Base64.getEncoder().encodeToString(hybridEncrypt.encrypt(plainText.getBytes(StandardCharsets.UTF_8), null));
	}

}
