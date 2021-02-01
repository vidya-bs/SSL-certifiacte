package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.media.XML;

public class XMLDiff {

	public XML diff(XML left, XML right) {
		XML changedXML = null;

		if (ComparisonUtils.isDiff(left, right)) {
			changedXML = new XML();

			if (ComparisonUtils.isDiff(left.getName(), right.getName())) {
				changedXML.setName(right.getName());
			}

			if (ComparisonUtils.isDiff(left.getNamespace(), right.getNamespace())) {
				changedXML.setNamespace(right.getNamespace());
			}

			if (ComparisonUtils.isDiff(left.getPrefix(), right.getPrefix())) {
				changedXML.setPrefix(right.getPrefix());
			}

			if (ComparisonUtils.isDiff(left.getAttribute(), right.getAttribute())) {
				changedXML.setAttribute(right.getAttribute());
			}

			if (ComparisonUtils.isDiff(left.getWrapped(), right.getWrapped())) {
				changedXML.setWrapped(right.getWrapped());
			}

			// TODO: externsions - later
		}

		return changedXML;
	}

}
