package com.itorix.apiwiz.design.studio.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ScannerDTO implements Serializable {

	private List<String> swaggerId;
	private String operation;
	private String tenantId;

}