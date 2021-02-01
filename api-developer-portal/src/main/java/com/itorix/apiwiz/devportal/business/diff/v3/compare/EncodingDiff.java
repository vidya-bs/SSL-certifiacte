package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.media.Encoding;

public class EncodingDiff {

	Map<String, Encoding> added = new LinkedHashMap<String, Encoding>();
	Map<String, Encoding> missing = new LinkedHashMap<String, Encoding>();
	Map<String, Encoding> changed = new LinkedHashMap<String, Encoding>();

	public EncodingDiff diff(Map<String, Encoding> left, Map<String, Encoding> right) {
		MapKeyDiff<String, Encoding> diffMap = MapKeyDiff.diff(left, right);
		if(Objects.nonNull(diffMap.getIncreased())) {
			this.added = diffMap.getIncreased();
		}
		
		if(Objects.nonNull(diffMap.getMissing())) {
			this.missing = diffMap.getMissing();
		}

		diffMap.getSharedKey().forEach(k -> {
			Encoding oldEncoding = left.get(k);
			Encoding newEncoding = right.get(k);

			if (ComparisonUtils.isDiff(oldEncoding, newEncoding)) {
				Encoding changedEncoding = new Encoding();

				if (ComparisonUtils.isDiff(oldEncoding.getContentType(), newEncoding.getContentType())) {
					changedEncoding.setContentType(newEncoding.getContentType());
				}

				if (ComparisonUtils.isDiff(oldEncoding.getAllowReserved(), newEncoding.getAllowReserved())) {
					changedEncoding.setAllowReserved(newEncoding.getAllowReserved());
				}

				if (ComparisonUtils.isDiff(oldEncoding.getExplode(), newEncoding.getExplode())) {
					changedEncoding.setExplode(newEncoding.getExplode());
				}

				if (ComparisonUtils.isDiff(oldEncoding.getStyle(), newEncoding.getStyle())) {
					changedEncoding.setStyle(newEncoding.getStyle());
				}

				// TODO: extensions - later

				this.changed.put(k, changedEncoding);
			}
		});

		return this;
	}

	public Map<String, Encoding> getAdded() {
		return added;
	}

	public void setAdded(Map<String, Encoding> added) {
		this.added = added;
	}

	public Map<String, Encoding> getMissing() {
		return missing;
	}

	public void setMissing(Map<String, Encoding> missing) {
		this.missing = missing;
	}

	public Map<String, Encoding> getChanged() {
		return changed;
	}

	public void setChanged(Map<String, Encoding> changed) {
		this.changed = changed;
	}

	public Boolean isNotEmpty() {
		return !this.getAdded().isEmpty() || !this.getChanged().isEmpty() || !this.getMissing().isEmpty();
	}
}
