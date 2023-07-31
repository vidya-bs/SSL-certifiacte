package com.itorix.apiwiz.identitymanagement.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchedulerDocumentDTO {
    private String documentName;
    private String db;
    private long expiryDays;
}
