package com.itorix.apiwiz.sso.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.websso.WebSSOProfile;

public class SAMLCustomEntryPoint extends SAMLEntryPoint {

    @Autowired(required = false)
    @Override
    public void setMetadata(MetadataManager metadata) {
        super.setMetadata(metadata);
    }

    @Autowired(required = false)
    @Override
    public void setSamlLogger(SAMLLogger samlLogger) {
        super.setSamlLogger(samlLogger);
    }

    @Autowired(required = false)
    @Qualifier("webSSOprofile")
    @Override
    public void setWebSSOprofile(WebSSOProfile webSSOprofile) {
        super.setWebSSOprofile(webSSOprofile);
    }

    @Autowired(required = false)
    @Override
    public void setContextProvider(SAMLContextProvider contextProvider) {
        super.setContextProvider(contextProvider);
    }
}