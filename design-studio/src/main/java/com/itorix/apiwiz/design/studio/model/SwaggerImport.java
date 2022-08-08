package com.itorix.apiwiz.design.studio.model;

import lombok.Data;

@Data
public class SwaggerImport {

	private String name;
	private String path;
	private boolean isLoaded;
	private String reason;
	private String swaggerId;

}
