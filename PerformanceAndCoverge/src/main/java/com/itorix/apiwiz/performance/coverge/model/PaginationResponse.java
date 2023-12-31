package com.itorix.apiwiz.performance.coverge.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;

import java.io.Serializable;

public class PaginationResponse implements Serializable {
    private Pagination pagination;
    private Object data;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
