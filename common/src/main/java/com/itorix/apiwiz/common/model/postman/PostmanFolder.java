package com.itorix.apiwiz.common.model.postman;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanFolder {
	public String id;
	public String name;
	public String description;
	public List<String> order; // An ordered list of the request ids
}
