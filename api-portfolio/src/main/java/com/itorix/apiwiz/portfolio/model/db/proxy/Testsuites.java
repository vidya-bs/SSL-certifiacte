package com.itorix.apiwiz.portfolio.model.db.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Testsuites {

	private String testSuiteId;
	private String environmentId;

}
