package com.itorix.apiwiz.serviceregistry.model.documents;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceRegistryColumnEntry {
	String columnId;
	boolean required;
	List<String> enums;
}
