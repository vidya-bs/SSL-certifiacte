package com.itorix.apiwiz.design.studio.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SwaggerObjectResponse implements Serializable {
	private Metrics metrics;
	private List<Stat> stats;

}
