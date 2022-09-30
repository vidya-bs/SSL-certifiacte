package com.itorix.apiwiz.datadictionary.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Design.Dictionary.Model")
@Data
public class PortfolioModel extends AbstractObject {

	private String portfolioID;

	private String modelName;

	private String model;

	private ModelStatus status = ModelStatus.Active;

	private Integer revision;

	private String modelId;

}
