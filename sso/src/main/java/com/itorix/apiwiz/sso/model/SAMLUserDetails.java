package com.itorix.apiwiz.sso.model;

import com.itorix.apiwiz.sso.dao.SSODao;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml.SAMLCredential;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SAMLUserDetails implements UserDetails {

    private static final long serialVersionUID = 5952811591131203949L;
    private SAMLCredential credential;
    private SSODao ssoDao;

    public SAMLUserDetails(SAMLCredential credential, SSODao identityManagementDao) {
        super();
        this.credential = credential;
        this.ssoDao = identityManagementDao;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SAMLConfig samlAssertionMapper = ssoDao.getSamlConfig();
        String roleName = null;
        if (samlAssertionMapper != null) {
            roleName = samlAssertionMapper.getGroup();
        }
        List<String> projectRole = ssoDao.getProjectRoleForSaml(samlAssertionMapper, credential);
        return projectRole.stream().map(s -> new SimpleGrantedAuthority(s)).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return credential.getNameID().getValue();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
