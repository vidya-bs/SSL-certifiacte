package com.itorix.apiwiz.apimonitor.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormURLencoded {
	private String name;
	private String description;
	private boolean value;
}
