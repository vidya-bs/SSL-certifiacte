package com.itorix.apiwiz.datapower.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class ProxySearchResponse implements Serializable {
  private String id;
  private String name;
  private String summary;
  private String owner;
}
