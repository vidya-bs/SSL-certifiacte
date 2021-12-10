package com.itorix.apiwiz.consent.management.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("Consent.KeyPair.List")
public class ConsentKeyPairs {

    @Id
    private String tenantId;
    private String privateKey;
    private String publicKey;

}
