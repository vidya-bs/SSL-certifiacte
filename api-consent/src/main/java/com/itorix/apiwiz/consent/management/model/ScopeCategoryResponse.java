package com.itorix.apiwiz.consent.management.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScopeCategoryResponse {
    private List<ScopeCategory> scopeCategories;
    private Pagination pagination;
}
