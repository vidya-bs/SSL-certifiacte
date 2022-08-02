package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subscriber extends AbstractObject{
	public enum Type {
		DEVELOPER,
		CONSUMER
	}
	@JsonProperty("name")
	String name;
	
	@JsonProperty("emailId")
	String emailId;

	@JsonProperty("type")
	Type type;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emailId == null) ? 0 : emailId.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || this.getClass() != obj.getClass())
			return false;
		Subscriber subscriber = (Subscriber) obj;
		return this.emailId.equals(subscriber.emailId);

	}

	public void setType(String type){
		this.type = Type.valueOf(type);
	}
}

