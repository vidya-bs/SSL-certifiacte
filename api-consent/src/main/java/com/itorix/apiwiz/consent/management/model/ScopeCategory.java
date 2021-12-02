package com.itorix.apiwiz.consent.management.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Consent.ScopeCategory.List")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScopeCategory {

    @Id
    private String name;
    private String displayName;
    private String summary;
    private String description;
    private long expiry;
    private List<Scope> scopeList;

    private Long cts;
    private Long mts;
}
