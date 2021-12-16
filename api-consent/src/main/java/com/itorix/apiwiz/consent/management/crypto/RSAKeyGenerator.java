package com.itorix.apiwiz.consent.management.crypto;

import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.consent.management.model.ConsentKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.*;
import java.util.Base64;

@Component
public class RSAKeyGenerator {

    @Autowired
    private BaseRepository baseRepository;


    public String generateKeyPair(String tenantId) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(512);

        KeyPair pair = generator.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());


        ConsentKeyPair existingConsent = baseRepository.findOne("tenantId", tenantId, ConsentKeyPair.class);

        if (existingConsent == null) {
            existingConsent = new ConsentKeyPair();
            existingConsent.setTenantId(tenantId);
        }
        existingConsent.setPrivateKey(privateKeyStr);
        existingConsent.setPublicKey(publicKeyStr);

        baseRepository.save(existingConsent);
        return publicKeyStr;
    }

}
