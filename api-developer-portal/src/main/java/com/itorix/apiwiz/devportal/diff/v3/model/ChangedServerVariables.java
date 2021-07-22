package com.itorix.apiwiz.devportal.diff.v3.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.servers.ServerVariable;

public class ChangedServerVariables {

	List<ServerVariable> added = new ArrayList<ServerVariable>();
	List<ServerVariable> missing = new ArrayList<ServerVariable>();
	List<ServerVariable> removed = new ArrayList<ServerVariable>();
}
