package com.itorix.apiwiz.design.studio.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "Design.Swagger.Comments")
public class SwaggerComment extends AbstractObject {

	private Integer swaggerRevision;

	private String swaggerName;

	private String comment;

	public Integer getSwaggerRevision() {
		return swaggerRevision;
	}

	public void setSwaggerRevision(Integer swaggerRevision) {
		this.swaggerRevision = swaggerRevision;
	}

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
