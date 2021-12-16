package com.itorix.apiwiz.consent.management.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScopeCategoryColumnEntry {

	private String name;
	private String displayName;
	private String summary;
	private boolean mandatory;
	private boolean isPrimaryKey;
	private List<String> enums;

}
