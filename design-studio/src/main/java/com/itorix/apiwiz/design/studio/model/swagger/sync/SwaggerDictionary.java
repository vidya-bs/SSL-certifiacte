package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "Design.Swagger.Dictionary.List")
public class SwaggerDictionary extends AbstractObject {
	private String name;
	private String swaggerId;
	private Integer revision;
	private String oasVersion;
	private String status;
	private List<Dictionary> dictionary;

}
