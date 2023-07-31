package com.itorix.apiwiz.identitymanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("Apiwiz.Archival.Documents")
public class SchedulerDocument {
    @Id
    private String key;
    private String documentName;
    private String db;
    private long expiryDays;

    public SchedulerDocument(){}
    public SchedulerDocument(SchedulerDocumentDTO schedulerDocumentDTO) {
        this.key = schedulerDocumentDTO.getDocumentName().replace(".", "_");
        this.documentName = schedulerDocumentDTO.getDocumentName();
        this.db = schedulerDocumentDTO.getDb();
        this.expiryDays = schedulerDocumentDTO.getExpiryDays();
    }
}
