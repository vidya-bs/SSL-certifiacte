package com.itorix.apiwiz.validator.model;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class LicensePolicy {
	private boolean checkExpiry = true;
	private boolean checkNodeBasedLocking;
	private boolean checkAllowedComponents;

	public LicensePolicy() {

	}
}
