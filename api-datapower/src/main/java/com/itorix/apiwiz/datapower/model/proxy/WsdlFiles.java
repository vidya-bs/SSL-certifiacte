package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WsdlFiles implements Serializable {
	private String wsdlName;

	private String wsdlLocation;
}
