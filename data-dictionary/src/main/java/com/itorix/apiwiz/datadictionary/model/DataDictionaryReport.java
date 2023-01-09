package com.itorix.apiwiz.datadictionary.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataDictionaryReport {

  private String dictionaryId;
  private List<DDSchema> models;
  private List<String> ruleSetId;

}