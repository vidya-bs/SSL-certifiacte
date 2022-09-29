package com.itorix.apiwiz.datadictionary.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Revision {

	private Integer revision = 1;
	private String status;
	private String portfolioId;

}
