package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.tags.Tag;

public class TagDiff {

	List<Tag> added = new ArrayList<>();
	List<Tag> missing = new ArrayList<>();
	List<Tag> changed = new ArrayList<>();

	public TagDiff diff(List<Tag> leftTags, List<Tag> rightTags) {

		if (Objects.isNull(leftTags) && Objects.isNull(rightTags)) {
			return null;
		}

		if (ComparisonUtils.isDiff(leftTags, rightTags)) {

			ListDiff<Tag> tagDiff = ListDiff.diff(leftTags, rightTags, (t, param) -> {
				for (Tag tag : t) {
					if (tag.getName().equals(tag.getName())) {
						return tag;
					}
				}
				return null;
			});

			this.added.addAll(tagDiff.getIncreased());
			this.missing.addAll(tagDiff.getMissing());

			Map<Tag, Tag> shared = tagDiff.getShared();
			shared.forEach((leftTag, rightTag) -> {
				Tag changedTag = null;

				if (!ComparisonUtils.isDiff(leftTag.getName(), rightTag.getName())) {
					changedTag = new Tag();

					if (ComparisonUtils.isDiff(leftTag.getDescription(), rightTag.getDescription())) {
						changedTag.setDescription(rightTag.getDescription());
					}

					changedTag.setName(rightTag.getName());

					ExternalDocumentation externalDocumentation = new ExternalDocumentationDiff()
							.diff(leftTag.getExternalDocs(), rightTag.getExternalDocs());

					if (Objects.nonNull(externalDocumentation)) {
						changedTag.setExternalDocs(externalDocumentation);
					}

					// TODO: extensions - later

					this.changed.add(changedTag);
				}
			});

			return this;
		}

		return null;
	}

	public List<Tag> getAdded() {
		return added;
	}

	public void setAdded(List<Tag> added) {
		this.added = added;
	}

	public List<Tag> getMissing() {
		return missing;
	}

	public void setMissing(List<Tag> missing) {
		this.missing = missing;
	}

	public List<Tag> getChanged() {
		return changed;
	}

	public void setChanged(List<Tag> changed) {
		this.changed = changed;
	}
}
