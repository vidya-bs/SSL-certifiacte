package com.itorix.apiwiz.serviceregistry.model.documents;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.Getter;
import lombok.Setter;

@Component("ServiceRegistryList")
@Document(collection = "Registry.Service.List")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
// @CompoundIndex(def = "{'projectId':1, 'proxyName':1,
// 'serviceRegistryNames.name':1}", name =
// "service_registry_index", unique = true)
public class ServiceRegistryList extends AbstractObject {
	private String name;
	private String environment;
	private String summary;
	private List<Metadata> metadata;
}
