package com.itorix.apiwiz.common.model.kong;

import java.io.Serializable;
import java.util.List;

public class ConsumerResponse implements Serializable {

    private List<Consumer> data;

    public ConsumerResponse(){

    }

    public ConsumerResponse(List<Consumer> data) {

        this.data = data;
    }

    public List<Consumer> getData() {
        return data;
    }

    public void setData(List<Consumer> data) {
        this.data = data;
    }
}