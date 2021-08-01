package com.itorix.apiwiz.validator.license.model;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class LicensePolicy {
	private boolean checkExpiry = true;
	private boolean checkNodeBasedLocking;
	private boolean checkAllowedComponents;
}
