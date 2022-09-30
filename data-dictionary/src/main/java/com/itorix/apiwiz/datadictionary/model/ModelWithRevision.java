package com.itorix.apiwiz.datadictionary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelWithRevision {

  ModelStatus status;
  Integer revision;
  String modelId;
  JsonNode model;
}