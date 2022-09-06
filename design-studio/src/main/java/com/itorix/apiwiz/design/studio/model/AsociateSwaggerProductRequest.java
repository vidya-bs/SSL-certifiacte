package com.itorix.apiwiz.design.studio.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(Include.NON_EMPTY)
public class AsociateSwaggerProductRequest implements Serializable {

  private List<String> productId;

}
