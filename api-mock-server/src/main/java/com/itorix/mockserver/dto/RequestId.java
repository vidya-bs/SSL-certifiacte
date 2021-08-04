package com.itorix.mockserver.dto;

import java.rmi.dgc.VMID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestId implements Comparable<RequestId> {

    private final VMID id = new VMID();

    public String toString() {
        return id.toString();
    }

    public String toShortString() {
        String[] splitString = id.toString().split(":");
        return splitString[splitString.length - 1];
    }

    public int toShortId() {
        return 0x8000 + Integer.valueOf(toShortString(), 16);
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RequestId requestId = (RequestId) o;

        return !(id != null ? !id.equals(requestId.id) : requestId.id != null);

    }

    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    public int compareTo(RequestId o) {
        if (o == null || o.id == null)
            return 1;
        if (id == null)
            return -1;

        return id.toString().compareTo(o.id.toString());
    }
}