package com.itorix.apiwiz.validator.license.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pagination {
	private int total;
	private int offset;
	private int pageSize;
}
