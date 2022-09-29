package com.itorix.apiwiz.datadictionary.model;

import lombok.Data;

@Data
public class Revision {

	private Integer revision = 1;
	private String status;
	private String portfolioId;

}
