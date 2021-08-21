package com.itorix.apiwiz.design.studio.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SwaggerCloneDetails {
	private String name;
	private String version;
	private String description;
	private String basePath;
	private String currentSwaggerID;
	private Integer revision;
}
