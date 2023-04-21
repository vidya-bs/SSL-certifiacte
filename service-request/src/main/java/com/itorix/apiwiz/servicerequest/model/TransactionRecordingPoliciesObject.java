package com.itorix.apiwiz.servicerequest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class TransactionRecordingPoliciesObject {

  private String name;
  private Policies policies;

  @Data
  public static class Policies{
    List<Object> request;
    List<Object> response;
  }
}