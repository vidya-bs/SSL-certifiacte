package com.itorix.apiwiz.portfolio.model.db;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Component("portfolios")
@Document(collection = "Plan.Portfolios.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Portfolio extends AbstractObject {
	String name;
	String summary;
	String description;
	String owner;
	String ownerEmail;
	String portfolioImage;

	@JsonProperty("metadata")
	List<Metadata> metadata;

	@JsonProperty("teams")
	List<String> teams;

	@JsonProperty("document")
	List<PortfolioDocument> document;

	@JsonProperty("products")
	List<Products> products;

	@JsonProperty("projects")
	List<Projects> projects;

	@JsonProperty("serviceRegistry")
	List<ServiceRegistry> serviceRegistry;
}
