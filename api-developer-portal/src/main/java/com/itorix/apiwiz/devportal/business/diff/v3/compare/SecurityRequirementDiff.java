package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.security.SecurityRequirement;

public class SecurityRequirementDiff {

	List<SecurityRequirement> added = new ArrayList<SecurityRequirement>();
	List<SecurityRequirement> missing = new ArrayList<SecurityRequirement>();
	Map<String, Map<String, Collection<String>>> changed = new LinkedHashMap<>();

	public SecurityRequirementDiff diff(List<SecurityRequirement> left, List<SecurityRequirement> right) {

		if((Objects.isNull(left) && Objects.isNull(right)) || (left.isEmpty() && right.isEmpty())) {
			return null;
		}
		
		ListDiff<SecurityRequirement> diffList = ListDiff.diff(left, right, (t, param) -> {
			for (SecurityRequirement securityRequirement : t) {
				for (String k : securityRequirement.keySet()) {
					if (param.containsKey(k)) {
						return securityRequirement;
					}
				}
			}

			return null;
		});

		this.added.addAll(diffList.getIncreased());
		this.missing.addAll(diffList.getMissing());

		Map<SecurityRequirement, SecurityRequirement> shared = diffList.getShared();

		shared.forEach((oldSecReq, newSecReq) -> {
			Set<String> keySet = oldSecReq.keySet();
			keySet.forEach(k -> {
				Map<String, Collection<String>> diffMap = ComparisonUtils.findDiff(oldSecReq.get(k), newSecReq.get(k));
				this.changed.put(k, diffMap);
			});
		});

		return this;
	}
	
	public Boolean isNotEmpty() {
		return !this.added.isEmpty() || !this.missing.isEmpty() || !this.changed.isEmpty();
	}

}
