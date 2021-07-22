package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedMediaType;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;

public class ContentDiff {

	Map<String, MediaType> added = new LinkedHashMap<>();
	Map<String, MediaType> missing = new LinkedHashMap<>();
	Map<String, ChangedMediaType> changed = new LinkedHashMap<>();

	public ContentDiff diff(Content oldContent, Content newContent) {

		if (ComparisonUtils.isDiff(oldContent, newContent)) {

			MapKeyDiff<String, MediaType> contentDiffMap = MapKeyDiff.diff(oldContent, newContent);
			this.added = contentDiffMap.getIncreased();
			this.missing = contentDiffMap.getMissing();

			List<String> sharedKey = contentDiffMap.getSharedKey();

			sharedKey.forEach(k -> {
				MediaType oldMediaType = oldContent.get(k);
				MediaType newMediaType = newContent.get(k);

				ChangedMediaType mediaTypeDiff = new MediaTypeDiff().diff(oldMediaType, newMediaType);
				if (Objects.nonNull(mediaTypeDiff)) {
					this.changed.put(k, mediaTypeDiff);
				}
			});
		}

		return this;
	}

	public Map<String, MediaType> getAdded() {
		return added;
	}

	public void setAdded(Map<String, MediaType> added) {
		this.added = added;
	}

	public Map<String, MediaType> getMissing() {
		return missing;
	}

	public void setMissing(Map<String, MediaType> missing) {
		this.missing = missing;
	}

	public Map<String, ChangedMediaType> getChanged() {
		return changed;
	}

	public void setChanged(Map<String, ChangedMediaType> changed) {
		this.changed = changed;
	}

	public Boolean isNotEmpty() {
		return !this.getAdded().isEmpty() || !this.getChanged().isEmpty() || !this.getMissing().isEmpty();
	}
}
