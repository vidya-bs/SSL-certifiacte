package com.itorix.apiwiz.serviceregistry.model.documents;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Component("ServiceRegistry")
@Document(collection = "Registry.Service.Rows")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRegistry {

	@Id
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private ObjectId id;

	String serviceRegistryId;
	Map<String, String> data;
}
