package com.itorix.apiwiz.sso.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractObject implements BaseObject {

    @Indexed
    @Id
    private String id;
    private Long cts;
    private String createdUserName;
    private String modifiedUserName;

    private Long mts;
    private String createdBy;
    private String modifiedBy;

    @JsonIgnore
    private String interactionid;

    @JsonIgnore
    private String jsessionid;

    public static final String LABEL_ID = "_id";
    public static final String LABEL_CREATED_TIME = "cts";
    public static final String LABEL_MODIFIED_TIME = "mts";
    public static final String LABEL_CREATED_BY = "createdBy";
    public static final String LABEL_MODIFIED_BY = "modifiedBy";
    public static final String LABEL_CREATED_USERNAME = "createdUserName";
    public static final String LABEL_MODIFIED_USERNAME = "modifiedUserName";

    public AbstractObject() {
        super();
    }

    public AbstractObject(AbstractObject abstractObject) {
        this.id = abstractObject.id;
        this.cts = abstractObject.cts;
        this.mts = abstractObject.mts;
        this.createdBy = abstractObject.createdBy;
        this.modifiedBy = abstractObject.modifiedBy;
        this.createdUserName = abstractObject.createdUserName;
        this.modifiedUserName = abstractObject.modifiedUserName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Long getCts() {
        return cts;
    }

    @Override
    public void setCts(Long cts) {
        this.cts = cts;
    }

    @Override
    public Long getMts() {
        return mts;
    }

    @Override
    public void setMts(Long mts) {
        this.mts = mts;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public String getModifiedUserName() {
        return modifiedUserName;
    }

    public void setModifiedUserName(String modifiedUserName) {
        this.modifiedUserName = modifiedUserName;
    }

    public String getInteractionid() {
        return interactionid;
    }

    public void setInteractionid(String interactionid) {
        this.interactionid = interactionid;
    }

    public String getJsessionid() {
        return jsessionid;
    }

    public void setJsessionid(String jsessionid) {
        this.jsessionid = jsessionid;
    }

}