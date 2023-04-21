package com.itorix.apiwiz.devportal.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Data;

@Data
public class ProductBundleCardResponse {

  private Pagination pagination;
  private Object response;

}