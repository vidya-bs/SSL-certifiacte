package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

public class InfoDiff {

	public static Info diff(Info oldInfo, Info newInfo) {

		if (ComparisonUtils.isDiff(oldInfo, newInfo)) {

			Info info = new Info();

			// Title comparison
			if (ComparisonUtils.isDiff(oldInfo.getTitle(), newInfo.getTitle())) {
				info.setTitle(newInfo.getTitle());
			}

			// Description comparison
			if (ComparisonUtils.isDiff(oldInfo.getDescription(), newInfo.getDescription())) {
				info.setDescription(newInfo.getDescription());
			}

			// TOS comparison
			if (ComparisonUtils.isDiff(oldInfo.getTermsOfService(), newInfo.getTermsOfService())) {
				info.setTermsOfService(newInfo.getTermsOfService());
			}

			// Contact comparison
			Contact oldContact = oldInfo.getContact();
			Contact newContact = newInfo.getContact();

			if (Objects.nonNull(oldContact)) {
				if (Objects.nonNull(newContact)) {
					if (ComparisonUtils.isDiff(oldContact, newContact)) {
						Contact changedContact = new Contact();

						// Contact name comparison
						if (ComparisonUtils.isDiff(oldContact.getName(), newContact.getName())) {
							changedContact.setName(newContact.getName());
						}

						// Contact email comparison
						if (ComparisonUtils.isDiff(oldContact.getEmail(), newContact.getEmail())) {
							changedContact.setEmail(newContact.getEmail());
						}

						// Contact url comparison
						if (ComparisonUtils.isDiff(oldContact.getUrl(), newContact.getUrl())) {
							changedContact.setUrl(newContact.getUrl());
						}

						// TODO: contact extensions - later

						info.setContact(changedContact);
					}
				}
			}

			// License comparison
			License oldLicense = oldInfo.getLicense();
			License newLicense = newInfo.getLicense();

			if (Objects.nonNull(oldLicense)) {
				if (Objects.nonNull(newLicense)) {
					if (ComparisonUtils.isDiff(oldLicense, newLicense)) {
						License changedLicense = new License();

						// License name comparison
						if (ComparisonUtils.isDiff(oldLicense.getName(), newLicense.getName())) {
							changedLicense.setName(newLicense.getName());
						}

						if (ComparisonUtils.isDiff(oldLicense.getUrl(), newLicense.getUrl())) {
							changedLicense.setUrl(newLicense.getUrl());
						}

						// TODO: License extensions - later

						info.setLicense(changedLicense);
					}
				}
			}

			// Version comparison
			if (ComparisonUtils.isDiff(oldInfo.getVersion(), newInfo.getVersion())) {
				info.setVersion(newInfo.getVersion());
			}

			// TODO: Info extensions - later

			return info;
		}

		return null;
	}
}
