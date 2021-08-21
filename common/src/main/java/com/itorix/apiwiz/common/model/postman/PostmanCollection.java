package com.itorix.apiwiz.common.model.postman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanCollection {
	public String id;
	public String name;
	public String description;
	public List<PostmanFolder> folders; // ordered
	public Long timestamp;
	public Boolean synced;
	public List<PostmanRequest> requests; // ordered
	public List<String> order;

	public Map<String, PostmanRequest> requestLookup = new HashMap<String, PostmanRequest>();
	public Map<String, PostmanFolder> folderLookup = new HashMap<String, PostmanFolder>();

	public void init() {
		for (PostmanRequest r : requests) {
			requestLookup.put(r.id, r);
		}
		for (PostmanFolder f : folders) {
			folderLookup.put(f.name, f);
		}
	}
}
