package com.itorix.apiwiz.common.model.kong;

import java.io.Serializable;
import java.util.List;

public class Consumer implements Serializable {

    private String id;
    private String username;

    public Consumer(){

    }

    public Consumer(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}