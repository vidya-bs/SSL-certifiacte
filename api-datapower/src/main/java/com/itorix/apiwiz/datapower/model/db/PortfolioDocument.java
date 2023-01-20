package com.itorix.apiwiz.datapower.model.db;

import java.util.List;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PortfolioDocument {

	String id;
	Integer revision;
	String documentId;
	String documentName;
	String documentSummary;
	String documentOwner;
	String documentOwnerEmail;
	String document;
	List<PortfolioDocument> revisions;
}
