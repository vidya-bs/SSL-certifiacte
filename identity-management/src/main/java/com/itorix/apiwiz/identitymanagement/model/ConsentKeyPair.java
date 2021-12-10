package com.itorix.apiwiz.identitymanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("Consent.KeyPair")
public class ConsentKeyPair extends AbstractObject{

    private String tenantKey;
    private String privateKey;
    private String publicKey;

}
