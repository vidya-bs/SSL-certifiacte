package com.itorix.apiwiz.common.model.configmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class DeveloperConfig {
  private List<Developers> developer;
  @Data
  public static class Developers {
    private String email;
    private String role;
  }
}