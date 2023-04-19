package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DesignArtifacts implements Serializable {
	private List<WsdlFiles> wsdlFiles;

	private List<XsdFiles> xsdFiles;

	private List<Swagger> swaggers;
}
