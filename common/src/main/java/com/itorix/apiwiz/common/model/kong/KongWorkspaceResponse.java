package com.itorix.apiwiz.common.model.kong;

import java.io.Serializable;
import java.util.List;

public class KongWorkspaceResponse implements Serializable {
    private List<KongWorkspace> data;

    public KongWorkspaceResponse() {

    }

    public KongWorkspaceResponse(List<KongWorkspace> data) {
        this.data = data;
    }

    public List<KongWorkspace> getData() {
        return data;
    }

    public void setData(List<KongWorkspace> data) {
        this.data = data;
    }
}
