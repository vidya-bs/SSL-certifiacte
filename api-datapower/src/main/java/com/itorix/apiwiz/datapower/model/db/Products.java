package com.itorix.apiwiz.datapower.model.db;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Products extends ProductRequest {

	@Id
	String id;
	String productImage;

	@JsonProperty("metadata")
	List<Metadata> metadata;

	@JsonProperty("services")
	List<ProductServices> productServices;
}
