package com.itorix.apiwiz.design.studio.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "Swagger3.BasePath")
public class Swagger3BasePath {

	private String name;
	private List<String> basePath;
}
