package com.itorix.apiwiz.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LicenseToken {
	private String emailId;
	private long expiry;
	private List<String> nodeIds;
	private LicensePolicy licensePolicy;
}
