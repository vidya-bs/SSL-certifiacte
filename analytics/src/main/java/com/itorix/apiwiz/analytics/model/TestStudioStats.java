package com.itorix.apiwiz.analytics.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TestStudioStats {
    private Map<String, Integer> topFiveTestsBasedOnSuccessRatio;
}
