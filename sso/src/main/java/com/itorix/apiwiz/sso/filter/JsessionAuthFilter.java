package com.itorix.apiwiz.sso.filter;

import com.itorix.apiwiz.sso.exception.ErrorCodes;
import com.itorix.apiwiz.sso.exception.ItorixException;
import com.itorix.apiwiz.sso.model.RSAEncryption;
import com.itorix.apiwiz.sso.model.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JsessionAuthFilter extends OncePerRequestFilter {

    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

    private static final Logger log = LoggerFactory.getLogger(JsessionAuthFilter.class);

    @Value(value = "${itorix.security.apikey}")
    String apiKey;

    private static final String TENANT_ID = "tenantId";
    private static final String API_KEY_NAME = "x-apikey";


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String apiKeyHeader = req.getHeader(API_KEY_NAME);

        if (!StringUtils.isEmpty(apiKeyHeader)) {
            try {
                if (StringUtils.hasText(apiKeyHeader)) {
                    unSecureCallValidations(apiKeyHeader);
                    String tenantName = ((String) req.getHeader(TENANT_ID));
                    TenantContext.setCurrentTenant(tenantName);
                    Authentication authentication = new UsernamePasswordAuthenticationToken("test1", null,
                            Arrays.asList(new SimpleGrantedAuthority("test1")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("error occured during decrypting apikey", e);
            }

        }
        chain.doFilter(req, res);
    }


    private void unSecureCallValidations(String apiKeyHeader)
            throws Exception {
        RSAEncryption rsaEncryption = new RSAEncryption();
        apiKeyHeader = rsaEncryption.decryptText(apiKeyHeader);
        if(apiKeyHeader == null){
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-2"), "SSO-2");
        }else{
            if(apiKeyHeader.equals(rsaEncryption.decryptText(apiKey))){
                return;
            }else{
                throw new ItorixException(ErrorCodes.errorMessage.get("SSO-2"), "SSO-2");
            }
        }
    }

}

