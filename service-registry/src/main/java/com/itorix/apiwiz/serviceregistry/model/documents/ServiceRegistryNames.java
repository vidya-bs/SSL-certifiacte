package com.itorix.apiwiz.serviceregistry.model.documents;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRegistryNames {
String name;
}
