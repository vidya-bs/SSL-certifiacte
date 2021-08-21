package com.itorix.apiwiz.apimonitor.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CertificatesOverviewResponse {
	private Pagination pagination;
	private List<Certificates> certificates;
}
