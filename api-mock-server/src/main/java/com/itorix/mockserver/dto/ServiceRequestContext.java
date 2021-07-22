package com.itorix.mockserver.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequestContext implements Serializable, Cloneable {

    private static final long serialVersionUID = -8321298246752932991L;
    protected String hashcode;
    String tenandId;
    protected RequestId requestId;

    public String getHashcode() {
        if (getTenentId() != null)
            return "" + getTenentId();
        else
            return "" + "?" + "-" + System.currentTimeMillis();
    }

    public void setHashcode(String code) {
        this.hashcode = code;
    }

    public String getTenentId() {
        return tenandId;
    }

    public void setTenantId(String tenandId) {
        this.tenandId = tenandId;
    }

    /**
     * Show the value of the context.
     * 
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return null;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(RequestId requestId) {
        this.requestId = requestId;
    }

}