package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PipelineStats {
    private String pipelineName;
    private int numberOfBuilds;
    private long avgDuration;
}
