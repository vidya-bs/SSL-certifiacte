package com.itorix.apiwiz.configmanagement.model.apigee;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.configmanagement.KVMEntry;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApigeeKVM {

	private String name;
	private String encrypted;
	private List<KVMEntry> entry;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}

	public List<KVMEntry> getEntry() {
		return entry;
	}

	public void setEntry(List<KVMEntry> entry) {
		this.entry = entry;
	}
}
