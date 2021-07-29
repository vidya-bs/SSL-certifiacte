package com.itorix.apiwiz;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LicenseValidator {

	@PostConstruct
	public void init() {
		System.out.println("*************Validator Invoked*********");
	}

}
