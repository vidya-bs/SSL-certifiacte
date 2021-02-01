package com.itorix.apiwiz.sso.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.sso.dao.SSODao;
import com.itorix.apiwiz.sso.model.SAMLUserDetails;



@Component
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

	@Autowired
	private SSODao ssoDao;

	@Override
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		UserDetails userDetails = new SAMLUserDetails(credential, ssoDao);
		return userDetails;
	}
}
