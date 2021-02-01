package com.itorix.apiwiz.test.executor.beans;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component("MaskFields")
@Document(collection = "Test.Mask.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaskFields {

	@Id
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String id;

	private List<String> fields = new ArrayList<>();

	public List<String> getMaskingFields() {
		return fields;
	}

	public void setMaskingFields(List<String> maskingFields) {
		this.fields = maskingFields;
	}
}