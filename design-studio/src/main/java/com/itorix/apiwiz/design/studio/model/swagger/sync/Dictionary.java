package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Dictionary {
	private String name;
	private String id;
	@JsonProperty("schemas")
	private List<Model> models;
}
