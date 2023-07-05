package com.itorix.apiwiz.common.model.kong;

import java.io.Serializable;
import java.util.List;

public class ConsumerResponse implements Serializable {
    private Object next;
    private List<Consumer> data;

    public ConsumerResponse(){

    }

    public ConsumerResponse(Object next, List<Consumer> data) {
        this.next = next;
        this.data = data;
    }

    public Object getNext() {
        return next;
    }

    public void setNext(Object next) {
        this.next = next;
    }

    public List<Consumer> getData() {
        return data;
    }

    public void setData(List<Consumer> data) {
        this.data = data;
    }
}
