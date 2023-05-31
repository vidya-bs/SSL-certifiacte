package com.itorix.apiwiz.design.studio.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Data;

@Data
public class PaginatedResponse {
    private Pagination pagination;
    private Object data;
}