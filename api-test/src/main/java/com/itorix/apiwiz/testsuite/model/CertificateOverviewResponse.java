package com.itorix.apiwiz.testsuite.model;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CertificateOverviewResponse {

	private Pagination pagination;
	private List<Certificates> certificates;
}
