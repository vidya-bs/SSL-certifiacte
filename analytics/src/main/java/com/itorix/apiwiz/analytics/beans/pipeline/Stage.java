package com.itorix.apiwiz.analytics.beans.pipeline;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Stage {

    private String result;
    private String name;
    private Object rerunOfCounter;
    private Object approvalType;
    private Boolean scheduled;
    private Boolean operatePermission;
    private Object approvedBy;
    private Boolean canRun;
    private Integer id;
    private String counter;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}
