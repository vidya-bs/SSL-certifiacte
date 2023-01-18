package com.itorix.apiwiz.datapower.model.proxy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class DesignArtifacts {
	private List<WsdlFiles> wsdlFiles;

	private List<XsdFiles> xsdFiles;

	private List<Swagger> swaggers;
}
