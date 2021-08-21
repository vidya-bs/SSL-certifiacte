package com.itorix.apiwiz.devportal.diff.v3.model;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.ServerVariablesDiff;

import io.swagger.v3.oas.models.servers.Server;

public class ChangedServer extends Server {

	ServerVariablesDiff serverVariableDiff;

	public ServerVariablesDiff getServerVariableDiff() {
		return serverVariableDiff;
	}

	public void setServerVariableDiff(ServerVariablesDiff serverVariableDiff) {
		this.serverVariableDiff = serverVariableDiff;
	}
}
