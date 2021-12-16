package com.itorix.apiwiz.consent.management.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConsentResponse {

	private List<Consent> consentList;
	private Pagination pagination;
}
