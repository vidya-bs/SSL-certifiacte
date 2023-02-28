package com.itorix.apiwiz.design.studio.model;

import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;

import java.util.List;

@Data
public class ScannerExecutionEvents extends AbstractObject {
    private String tenant;
    private List<String> executionEventIds;
}
