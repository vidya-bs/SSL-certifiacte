package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

public class ServerVariablesDiff {

	Map<String, ServerVariable> added = new LinkedHashMap<>();
	Map<String, ServerVariable> missing = new LinkedHashMap<>();
	Map<String, ServerVariable> changed = new LinkedHashMap<>();

	public ServerVariablesDiff diff(ServerVariables leftVariables, ServerVariables rightVariables) {
		
		if(Objects.isNull(leftVariables) && Objects.isNull(rightVariables)) {
			return null;
		}

		if (ComparisonUtils.isDiff(leftVariables, rightVariables)) {
			MapKeyDiff<String, ServerVariable> serverVariableDiffMap = MapKeyDiff.diff(leftVariables, rightVariables);
			this.added = serverVariableDiffMap.getIncreased();
			this.missing = serverVariableDiffMap.getMissing();

			List<String> sharedKeys = serverVariableDiffMap.getSharedKey();

			sharedKeys.forEach(k -> {
				ServerVariable left = leftVariables.get(k);
				ServerVariable right = rightVariables.get(k);

				ServerVariable changedServerVariable = null;

				if (ComparisonUtils.isDiff(left, right)) {

					changedServerVariable = new ServerVariable();

					if (!left.getEnum().equals(right.getEnum())) {
						changedServerVariable.setEnum(right.getEnum());
					}

					if (ComparisonUtils.isDiff(left.getDefault(), right.getDefault())) {
						changedServerVariable.setDefault(right.getDefault());
					}

					if (ComparisonUtils.isDiff(left.getDescription(), right.getDescription())) {
						changedServerVariable.setDescription(right.getDescription());
					}

					// TODO: extensions - later
				}

				if (Objects.nonNull(changedServerVariable)) {
					this.changed.put(k, changedServerVariable);
				}
			});

			return this;
		}

		return null;
	}

	public Map<String, ServerVariable> getAdded() {
		return added;
	}

	public void setAdded(Map<String, ServerVariable> added) {
		this.added = added;
	}

	public Map<String, ServerVariable> getMissing() {
		return missing;
	}

	public void setMissing(Map<String, ServerVariable> missing) {
		this.missing = missing;
	}

	public Map<String, ServerVariable> getChanged() {
		return changed;
	}

	public void setChanged(Map<String, ServerVariable> changed) {
		this.changed = changed;
	}

}
