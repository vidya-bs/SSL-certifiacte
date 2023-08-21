package com.itorix.apiwiz.identitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("Apiwiz.Archival.Documents")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchedulerDocument {
    @Id
    private String key;
    private String name;
    private String db;
    private long expiryDays;
    private boolean masterDb;
    private String field;

    public SchedulerDocument(){}
    public SchedulerDocument(SchedulerDocumentDTO schedulerDocumentDTO) {
        this.key = schedulerDocumentDTO.getName().replace(".", "_");
        this.name = schedulerDocumentDTO.getName();
        this.db = schedulerDocumentDTO.getDb();
        this.expiryDays = schedulerDocumentDTO.getExpiryDays();
        this.masterDb = schedulerDocumentDTO.isMasterDb();
        this.field = schedulerDocumentDTO.getField();
    }
}
