package com.itorix.apiwiz.marketing.common;

import lombok.Data;

@Data
public class Pagination {
    private Long total;
    private int offset;
    private int pageSize;
}
