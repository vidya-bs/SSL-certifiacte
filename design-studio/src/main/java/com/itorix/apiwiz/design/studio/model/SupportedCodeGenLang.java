package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component("supportedcodegenlang")
@Document(collection = "Design.API.CodeGen.SupportedCodeGenLang")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SupportedCodeGenLang {
	private String name;
	private String type;
	private boolean oas2Compatible;
	private boolean oas3Compatible;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isOas2Compatible() {
		return oas2Compatible;
	}
	public void setOas2Compatible(boolean oas2Compatible) {
		this.oas2Compatible = oas2Compatible;
	}
	public boolean isOas3Compatible() {
		return oas3Compatible;
	}
	public void setOas3Compatible(boolean oas3Compatible) {
		this.oas3Compatible = oas3Compatible;
	}
}
