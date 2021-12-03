package com.itorix.consentserver.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "Consent.ScopeCategory.Columns")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScopeCategoryColumns {
    private List<ScopeCategoryColumnEntry> columns;
}
