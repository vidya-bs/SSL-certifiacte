package com.itorix.apiwiz.serviceregistry.model.documents;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Metadata {

	private String key;

	private String value;

}
