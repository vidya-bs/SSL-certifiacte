package com.itorix.apiwiz.sso.configuration;

import com.itorix.apiwiz.sso.filter.JsessionAuthFilter;
import com.itorix.apiwiz.sso.handler.AccessDeniedHandlerImpl;
import com.itorix.apiwiz.sso.handler.AuthenticationEntryPointImpl;
import com.itorix.apiwiz.sso.serviceImpl.SAMLUserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.security.GeneralSecurityException;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class SSOSecurityConfig {

    private Logger logger = LoggerFactory.getLogger(SSOSecurityConfig.class);

    @Value("${itorix.ssl.sso.key-alias}")
    private String keyAlias;

    @Value("${itorix.ssl.sso.key-store-password}")
    private String keyStorePassword;

    @Value("${itorix.ssl.sso.key-password}")
    private String password;

    @Value("${server.port}")
    private int port;

    @Value("${itorix.ssl.sso.key-store}")
    private String keyStoreFilePath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${validate.selfSignedCertificate:true}")
    private boolean validateSelfSignCertificate;

    @Value("${login.sso.failure}")
    private String failureRedirectUrl;

    @Value("${itorix.sso.server.host}")
    private String host;

    @Value("${itorix.sso.server.port}")
    private int ssoPort;

    @Value("${itorix.sso.server.scheme}")
    private String protocol;

    @Value("${itorix.sso.metadata.url}")
    private String itorixMetadataUrl;

    @Value("${itorix.sso.workspaceId}")
    private String workspaceId;

    @Value("${itorix.sso.logout.targeturl}")
    private String targetUrl;

    @Value("${itorix.sso.authentication.age}")
    private long maxAuthenticationAgeInHours;


    @Autowired
    private SAMLUserDetailsServiceImpl sAMLUserDetailsServiceImpl;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Autowired
    AuthenticationEntryPointImpl authenticationEntryPointImpl;

    @Autowired
    JsessionAuthFilter jsessionAuthFilter;

    @Configuration
    @Order(100)
    public class SamlWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {

//            String metadataUrl = new StringBuilder(protocol + "://").append(host).append(":").append(ssoPort)
//                    .append(contextPath).append("/saml/idpMetadata").toString();

            String metadataUrl = itorixMetadataUrl + "/" + workspaceId;

            //
            if (!validateSelfSignCertificate) {
                ProtocolSocketFactory socketFactory = null;
                try {
                    socketFactory = new EasySSLProtocolSocketFactory();
                } catch (GeneralSecurityException | IOException e) {
                    log.error("self certificate validation setting failed", e);
                    throw e;
                }
                Protocol.registerProtocol("https", new Protocol("https", socketFactory, port));
            }

            // @formatter:off
            http.csrf().disable().antMatcher("/saml/**").authorizeRequests().antMatchers("/saml/token/**")
                    .authenticated().antMatchers("/saml/**").permitAll().and()
                    .apply(SAMLConfigurer.saml(targetUrl).userDetailsService(sAMLUserDetailsServiceImpl))
                    .failureRedirectUrl(failureRedirectUrl).maxAuthenticationAgeInHours(maxAuthenticationAgeInHours).validateSelfSignCertificate(validateSelfSignCertificate)
                    .serviceProvider().keyStore().storeFilePath(keyStoreFilePath).password(keyStorePassword)
                    .keyname(keyAlias).keyPassword(password).and().protocol(protocol)
                    .hostname(String.format("%s:%s", host, ssoPort)).basePath(contextPath).and().identityProvider()
                    .metadataFilePath(metadataUrl).and();
            http.exceptionHandling().accessDeniedHandler(accessDeniedHandler).and();

            http.logout()
                    .addLogoutHandler((request, response, authentication) -> {
                        try {
                            response.sendRedirect("/saml/logout");
                        } catch (IOException e) {
                           logger.error("Error while logout ", e);
                        }
                    });

        }
    }

    @Configuration
    @Order(1)
    public class WebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            // @formatter:off

            http.csrf().disable().antMatcher("/v1/**").authorizeRequests().anyRequest().authenticated().and()
                    .exceptionHandling().authenticationEntryPoint(authenticationEntryPointImpl)
                    .accessDeniedHandler(accessDeniedHandler).and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().cors();

            http.addFilterBefore(jsessionAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

    }

    @Configuration
    @Order(2)
    public class ActuatorSebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            // @formatter:off
            http.csrf().disable().antMatcher("/actuator/**").authorizeRequests().anyRequest().permitAll();
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            // @formatter:on
        }
    }

}
