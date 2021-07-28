package com.itorix.apiwiz.business;

import lombok.SneakyThrows;

public interface LicenseBusiness {

	@SneakyThrows
	public String getLicenseToken(String emailId);
}
