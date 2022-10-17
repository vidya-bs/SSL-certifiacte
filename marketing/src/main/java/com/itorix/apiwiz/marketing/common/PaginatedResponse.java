package com.itorix.apiwiz.marketing.common;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Data;

@Data
public class PaginatedResponse {
    private Pagination pagination;
    private Object data;
}