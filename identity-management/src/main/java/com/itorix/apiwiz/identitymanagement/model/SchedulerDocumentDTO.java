package com.itorix.apiwiz.identitymanagement.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchedulerDocumentDTO {
    private String name;
    private String db;
    private long expiryDays;
    private boolean masterDb;
    private String field;
}
