package com.itorix.apiwiz.identitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Heading {
	private String name;
	private List<String> values;
	private String info;
}
