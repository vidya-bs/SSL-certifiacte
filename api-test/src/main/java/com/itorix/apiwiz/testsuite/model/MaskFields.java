package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Component("MaskFields")
@Document(collection = "Test.Mask.List")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaskFields {

	@Id
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String id;

	private List<String> fields;
}