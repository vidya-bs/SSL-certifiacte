package com.itorix.apiwiz.consent.management.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Document("Consent.List")
public class Consent {
	@Indexed
	@Id
	private String id;
	private Map<String, String> consent;
	private List<String> scopes;
	private long cts;
	private long mts;
	private long expiry;
}
