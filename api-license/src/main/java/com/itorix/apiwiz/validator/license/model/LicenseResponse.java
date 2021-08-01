package com.itorix.apiwiz.validator.license.model;
import com.itorix.apiwiz.validator.license.model.db.License;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LicenseResponse {
	private Pagination page;
	private List<License> licenses;
}
