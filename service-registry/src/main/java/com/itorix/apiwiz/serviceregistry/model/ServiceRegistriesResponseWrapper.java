package com.itorix.apiwiz.serviceregistry.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(of={"projectId","proxyName"})
public class ServiceRegistriesResponseWrapper {

	String projectId;
	String proxyName;
	List<NameIdContainer> serviceRegistryNames = new ArrayList<>();

}