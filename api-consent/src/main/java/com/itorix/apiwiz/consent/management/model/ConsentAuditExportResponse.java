package com.itorix.apiwiz.consent.management.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentAuditExportResponse {

    private String fileName;
    private String downloadURI;
    private String sha1;
    private String md5;

}
