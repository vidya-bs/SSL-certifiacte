package com.itorix.apiwiz.sso.filter;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itorix.apiwiz.sso.model.RSAEncryption;
import com.itorix.apiwiz.sso.model.TenantContext;

@Component
public class JsessionAuthFilter extends OncePerRequestFilter {

    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

    private static final Logger log = LoggerFactory.getLogger(JsessionAuthFilter.class);

    @Value(value = "${itorix.security.apikey}")
    String apiKey;

    private static final String TENANT_ID = "tenantId";
    private static final String API_KEY_NAME = "x-apikey";

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String SESSION_TOKEN_NAME = "JSESSIONID";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String apiKeyHeader = req.getHeader(API_KEY_NAME);

        if (!StringUtils.isEmpty(apiKeyHeader)) {
            try {
                if (StringUtils.hasText(apiKeyHeader) && new RSAEncryption().decryptText(apiKey).equals(apiKeyHeader)) {
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
}