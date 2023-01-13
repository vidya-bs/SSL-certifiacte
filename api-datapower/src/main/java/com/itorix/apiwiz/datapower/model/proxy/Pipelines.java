package com.itorix.apiwiz.datapower.model.proxy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Pipelines {

	private String id;
	private String branchType;
	private List<Stages> stages;
}
