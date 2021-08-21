package com.itorix.apiwiz.apimonitor.model;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VariablesOverviewResponse {

	private Pagination pagination;
	private List<Variables> variables;

}
