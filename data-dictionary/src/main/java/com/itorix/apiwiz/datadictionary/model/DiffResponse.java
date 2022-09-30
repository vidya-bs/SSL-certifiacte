package com.itorix.apiwiz.datadictionary.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;

@Data
public class DiffResponse implements Serializable {
    PortfolioModel model1;
    PortfolioModel model2;
    JsonNode diff;
}