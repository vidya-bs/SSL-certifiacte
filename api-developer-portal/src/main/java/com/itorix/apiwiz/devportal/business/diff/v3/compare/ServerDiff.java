package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedServer;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;

public class ServerDiff {

	List<Server> added = new ArrayList<Server>();
	List<Server> missing = new ArrayList<Server>();
	List<ChangedServer> changed = new ArrayList<ChangedServer>();

	public ServerDiff diff(List<Server> oldServers, List<Server> newServers) {

		if (Objects.isNull(oldServers) && Objects.isNull(newServers)) {
			return null;
		}

		if (Objects.isNull(oldServers) && Objects.nonNull(newServers)) {
			this.added.addAll(newServers);
			return this;
		}

		if (Objects.nonNull(oldServers) && Objects.isNull(newServers)) {
			this.missing.addAll(oldServers);
			return this;
		}

		if ((oldServers.isEmpty() && newServers.isEmpty())
				|| (Objects.isNull(oldServers) && Objects.isNull(newServers))) {
			return null;
		}

		if (ComparisonUtils.isDiff(oldServers, newServers)) {
			ListDiff<Server> serverDiff = ListDiff.diff(oldServers, newServers, (t, param) -> {
				for (Server server : t) {
					if (server.getUrl().equals(param.getUrl())) {
						return server;
					}
				}
				return null;
			});

			this.added.addAll(serverDiff.getIncreased());
			this.missing.addAll(serverDiff.getMissing());
			Map<Server, Server> shared = serverDiff.getShared();

			shared.forEach((leftServer, rightServer) -> {
				if (ComparisonUtils.isDiff(leftServer, rightServer)) {
					ChangedServer changedServer = new ChangedServer();

					String leftUrl = leftServer.getUrl();
					String rightUrl = rightServer.getUrl();
					if (ComparisonUtils.isDiff(leftUrl, rightUrl)) {
						changedServer.setUrl(leftUrl);
					}

					String leftDesc = leftServer.getDescription();
					String rightDesc = rightServer.getDescription();
					if (ComparisonUtils.isDiff(leftDesc, rightDesc)) {
						changedServer.setUrl(leftUrl);
						changedServer.setDescription(rightDesc);
					}

					ServerVariables leftVariables = leftServer.getVariables();
					ServerVariables rightVariables = rightServer.getVariables();

					ServerVariablesDiff serverVariableDiff = new ServerVariablesDiff().diff(leftVariables,
							rightVariables);

					if (Objects.nonNull(serverVariableDiff)) {
						changedServer.setUrl(leftUrl);
						changedServer.setServerVariableDiff(serverVariableDiff);
					}

					this.changed.add(changedServer);

					// TODO: extensions - later
				}
			});
			return this;
		}

		return null;
	}

	public List<Server> getAdded() {
		return added;
	}

	public void setAdded(List<Server> added) {
		this.added = added;
	}

	public List<Server> getMissing() {
		return missing;
	}

	public void setMissing(List<Server> missing) {
		this.missing = missing;
	}

	public List<ChangedServer> getChanged() {
		return changed;
	}

	public void setChanged(List<ChangedServer> changed) {
		this.changed = changed;
	}
}
