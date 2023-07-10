package com.itorix.apiwiz.common.model.kong;

import java.io.Serializable;
import java.util.List;

public class KongWorkspaceResponse implements Serializable {
    private Object next;
    private List<KongWorkspace> data;

    public KongWorkspaceResponse() {

    }

    public KongWorkspaceResponse(Object next, List<KongWorkspace> data) {
        this.next = next;
        this.data = data;
    }

    public Object getNext() {
        return next;
    }

    public void setNext(Object next) {
        this.next = next;
    }

    public List<KongWorkspace> getData() {
        return data;
    }

    public void setData(List<KongWorkspace> data) {
        this.data = data;
    }
}
