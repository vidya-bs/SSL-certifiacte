package com.itorix.apiwiz.design.studio.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Stat implements Serializable {
	private String name;
	private String count;
	private List<Swaggers> swaggers;
}
