package com.itorix.apiwiz.datadictionary.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Design.Dictionary.Model")
public class PortfolioModel extends AbstractObject {

	private String portfolioID;

	private String modelName;

	private String model;

	private ModelStatus status = ModelStatus.Active;

	public String getPortfolioID() {
		return portfolioID;
	}

	public void setPortfolioID(String portfolioID) {
		this.portfolioID = portfolioID;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public ModelStatus getStatus() {
		return status;
	}
	public void setStatus(ModelStatus status) {
		this.status = status;
	}
}
