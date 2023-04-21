package com.itorix.apiwiz.devportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class Specs {

  private String swaggerId;
  private String swaggerName;
  private String oasVersion;
}