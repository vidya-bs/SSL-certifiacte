package com.itorix.apiwiz.design.studio.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class Metrics implements Serializable {
    private String type;
    private HashMap<String, Object> values;
}
