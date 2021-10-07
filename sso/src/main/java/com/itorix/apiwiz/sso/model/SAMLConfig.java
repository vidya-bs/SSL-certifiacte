package com.itorix.apiwiz.sso.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * The following configuration is used to store saml response data node name details.
 *
 * @author kishan
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SAMLConfig {

    // saml response attribute stored for loginId
    private String loginId;
    // saml response attribute stored for firstName
    private String firstName;
    // saml response attribute stored for lastName
    private String lastName;
    // saml response attribute stored for emailId
    private String emailId;

    // saml response attribute having group details
    private String group;
    // metadataUrl of the idp, metadataUrl or metadata should be provided
    private String metadataUrl;

    // saml response attribute having role details
    private String userRoles;

    // metadata file of the idp, either metadataUrl or metadata should be provided
    private byte[] metadata;
    // IDP equivalent role names of itorix application
    private UserDefinedRoles roles;
    private String workspaceId;
}
