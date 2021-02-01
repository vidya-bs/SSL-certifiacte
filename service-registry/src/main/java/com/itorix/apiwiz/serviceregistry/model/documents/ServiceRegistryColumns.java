package com.itorix.apiwiz.serviceregistry.model.documents;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Component("ServiceRegistryColumns")
@Document(collection = "Registry.Service.Columns")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRegistryColumns {

	private List<ServiceRegistryColumnEntry> columns;
}
