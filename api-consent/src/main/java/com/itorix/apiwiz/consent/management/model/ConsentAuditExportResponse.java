package com.itorix.apiwiz.consent.management.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentAuditExportResponse {

	private String fileName;
	private String downloadURI;
	private String sha1;
	private String md5;

}
