package com.itorix.apiwiz.portfolio.model.db;

import java.util.List;

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
public class ProductRequest {

	enum ProductStatus {
		@JsonProperty("New")
		newStatus, @JsonProperty("Published")
		published, @JsonProperty("Review")
		Review;
	}

	enum ProductAccess {
		internal, external
	}

	@JsonProperty("name")
	String name;

	@JsonProperty("summary")
	String summary;

	@JsonProperty("description")
	String description;

	@JsonProperty("owner")
	String owner;

	String ownerEmail;
	ProductStatus productStatus;
	List<ProductAccess> productAccess;
	boolean publishStatus;
}
