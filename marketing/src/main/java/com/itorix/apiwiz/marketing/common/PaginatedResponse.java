package com.itorix.apiwiz.marketing.common;

import lombok.Data;

@Data
public class PaginatedResponse {
    private Pagination pagination;
    private Object data;
}