package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TargetEndpoints implements Serializable {
	private String refId;
	private String name;
}
