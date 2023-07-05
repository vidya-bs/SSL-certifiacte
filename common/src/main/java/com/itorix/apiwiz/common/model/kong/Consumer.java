package com.itorix.apiwiz.common.model.kong;

import java.util.List;

public class Consumer {

    private String id;
    private String custom_id;
    private String username;
    private String username_lower;
    private List<String> tags;
    private long created_at;

    public Consumer(){

    }

    public Consumer(String id, String custom_id, String username, String username_lower, List<String> tags, long created_at) {
        this.id = id;
        this.custom_id = custom_id;
        this.username = username;
        this.username_lower = username_lower;
        this.tags = tags;
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustom_id() {
        return custom_id;
    }

    public void setCustom_id(String custom_id) {
        this.custom_id = custom_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername_lower() {
        return username_lower;
    }

    public void setUsername_lower(String username_lower) {
        this.username_lower = username_lower;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }
}
