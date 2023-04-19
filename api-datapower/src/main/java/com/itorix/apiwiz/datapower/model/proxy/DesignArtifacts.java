package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class DesignArtifacts implements Serializable {
	private List<WsdlFiles> wsdlFiles;

	private List<XsdFiles> xsdFiles;

	private List<Swagger> swaggers;
}
