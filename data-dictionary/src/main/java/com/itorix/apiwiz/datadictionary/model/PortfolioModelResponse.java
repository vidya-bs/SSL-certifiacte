package com.itorix.apiwiz.datadictionary.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortfolioModelResponse {

	private String modelName;

	private ModelStatus status = ModelStatus.Active;

	private Long mts;

	private String modelId;

	private Integer revision;

}