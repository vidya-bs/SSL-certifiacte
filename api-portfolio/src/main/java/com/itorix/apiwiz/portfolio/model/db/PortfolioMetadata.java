package com.itorix.apiwiz.portfolio.model.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Component("portfoliosDocuments")
@Document(collection = "Portfolios.documents")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioMetadata {
	@Id
	String fileName;
	String fileSummary;
	String fileOwner;
	String fileOwnerEmail;
	byte[] file;
}
