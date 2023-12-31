package com.itorix.apiwiz.sso.configuration;

import com.itorix.apiwiz.sso.handler.SamlAuthenticationFailureHandler;
import org.apache.commons.httpclient.HttpClient;
import org.opensaml.Configuration;
import org.opensaml.PaosBootstrap;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xml.security.x509.CertPathPKIXTrustEvaluator;
import org.opensaml.xml.security.x509.PKIXTrustEvaluator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.processor.*;
import org.springframework.security.saml.storage.SAMLMessageStorageFactory;
import org.springframework.security.saml.trust.MetadataCredentialResolver;
import org.springframework.security.saml.trust.PKIXInformationResolver;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class SAMLConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private Logger logger = LoggerFactory.getLogger(SAMLConfigurer.class);


    private IdentityProvider identityProvider = new IdentityProvider();
    private ServiceProvider serviceProvider = new ServiceProvider();

    private WebSSOProfileOptions webSSOProfileOptions = webSSOProfileOptions();
    private StaticBasicParserPool parserPool = staticBasicParserPool();
    private SAMLProcessor samlProcessor = samlProcessor();
    private SAMLDefaultLogger samlLogger = new SAMLDefaultLogger();
    private WebSSOProfileConsumerImpl webSSOProfileConsumer;
    private SAMLAuthenticationProvider samlAuthenticationProvider;
    private MetadataProvider metadataProvider;
    private ExtendedMetadataDelegate extendedMetadataDelegate;
    private CachingMetadataManager cachingMetadataManager;
    private WebSSOProfile webSSOProfile;
    private SingleLogoutProfile singleLogoutProfile;
    private SAMLUserDetailsService samlUserDetailsService;
    private boolean forcePrincipalAsString = false;
    private String failureRedirectUrl;
    private boolean validateSelfSignCertificate;
    private long maxAuthenticationAgeInHours;

    private String logoutTargetUrl = "/";

    private ObjectPostProcessor<Object> objectPostProcessor = new ObjectPostProcessor<Object>() {
        public <T> T postProcess(T object) {
            return object;
        }
    };

    private SAMLConfigurer(String logoutTargetUrl) {
        this.logoutTargetUrl = logoutTargetUrl;
    }

    @Override
    public void init(HttpSecurity http) {

        samlLogger.setLogAllMessages(true);
        samlLogger.setLogErrors(true);
        samlLogger.setLogMessagesOnException(true);

        metadataProvider = identityProvider.metadataProvider();
        ExtendedMetadata extendedMetadata = extendedMetadata(identityProvider.discoveryEnabled);
        extendedMetadataDelegate = extendedMetadataDelegate(extendedMetadata);
        serviceProvider.keyManager = serviceProvider.keyManager();
        cachingMetadataManager = cachingMetadataManager();
        webSSOProfile = webSSOProfile();
        singleLogoutProfile = singleLogoutProfile();

        if (webSSOProfileConsumer == null) {
            long secondsFromPreviousLogin = maxAuthenticationAgeInHours * 3600;
            logger.info("Max Authentication age for SSO is configured as {} seconds.", secondsFromPreviousLogin);
            webSSOProfileConsumer = new WebSSOProfileConsumerImpl(samlProcessor, cachingMetadataManager);
            webSSOProfileConsumer.setMaxAuthenticationAge(secondsFromPreviousLogin);
        }

        samlAuthenticationProvider = samlAuthenticationProvider(webSSOProfileConsumer);

        bootstrap();

        SAMLContextProvider contextProvider = contextProvider();
        SAMLEntryPoint samlEntryPoint = samlEntryPoint(contextProvider);
        SAMLLogoutFilter samlLogoutFilter = samlLogoutFilter(contextProvider);
        SAMLLogoutProcessingFilter samlLogoutProcessingFilter = samlLogoutProcessingFilter(contextProvider);

        try {
            http.httpBasic().authenticationEntryPoint(samlEntryPoint);

            CsrfConfigurer<HttpSecurity> csrfConfigurer = http.getConfigurer(CsrfConfigurer.class);
            if (csrfConfigurer != null) {
                // Workaround to get working with Spring Security 3.2.
                RequestMatcher ignored = new AntPathRequestMatcher("/saml/SSO");
                RequestMatcher notIgnored = new NegatedRequestMatcher(ignored);
                RequestMatcher matcher = new AndRequestMatcher(new DefaultRequiresCsrfMatcher(), notIgnored);

                csrfConfigurer.requireCsrfProtectionMatcher(matcher);
            }
        } catch (Exception e) {
            logger.error("Error while initializing ", e);
        }

        http.addFilterBefore(metadataGeneratorFilter(samlEntryPoint, extendedMetadata), ChannelProcessingFilter.class)
                .addFilterAfter(samlFilter(samlEntryPoint, samlLogoutFilter, samlLogoutProcessingFilter,
                        contextProvider, failureRedirectUrl), BasicAuthenticationFilter.class)
                .authenticationProvider(samlAuthenticationProvider);
    }

    public static SAMLConfigurer saml(String logoutTargetUrl) {
        return new SAMLConfigurer(logoutTargetUrl);
    }

    public SAMLConfigurer userDetailsService(SAMLUserDetailsService samlUserDetailsService) {
        this.samlUserDetailsService = samlUserDetailsService;
        return this;
    }

    public SAMLConfigurer forcePrincipalAsString() {
        this.forcePrincipalAsString = true;
        return this;
    }

    public SAMLConfigurer maxAuthenticationAgeInHours(long maxAuthenticationAgeInHours) {
        this.maxAuthenticationAgeInHours = maxAuthenticationAgeInHours;
        return this;
    }

    public SAMLConfigurer failureRedirectUrl(String failureRedirectUrl) {
        this.failureRedirectUrl = failureRedirectUrl;
        return this;
    }


    public SAMLConfigurer validateSelfSignCertificate(boolean validateSelfSignCertificate) {
        this.validateSelfSignCertificate = validateSelfSignCertificate;
        return this;
    }

    public SAMLConfigurer webSSOProfileConsumer(WebSSOProfileConsumerImpl webSSOProfileConsumer) {
        this.webSSOProfileConsumer = webSSOProfileConsumer;
        return this;
    }

    public IdentityProvider identityProvider() {
        return identityProvider;
    }

    public ServiceProvider serviceProvider() {
        return serviceProvider;
    }

    private String entityBaseURL() {
        String entityBaseURL = serviceProvider.hostName + "/" + serviceProvider.basePath;
        entityBaseURL = entityBaseURL.replaceAll("//", "/").replaceAll("/$", "");
        entityBaseURL = serviceProvider.protocol + "://" + entityBaseURL;
        return entityBaseURL;
    }

    private SAMLEntryPoint samlEntryPoint(SAMLContextProvider contextProvider) {
        SAMLEntryPoint samlEntryPoint = new SAMLCustomEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions);
        samlEntryPoint.setWebSSOprofile(webSSOProfile);
        samlEntryPoint.setContextProvider(contextProvider);
        samlEntryPoint.setMetadata(cachingMetadataManager);
        samlEntryPoint.setSamlLogger(samlLogger);
        return samlEntryPoint;
    }

    private SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(logoutTargetUrl);
        return logoutSuccessHandler;
    }

    private SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    private SAMLLogoutFilter samlLogoutFilter(SAMLContextProvider contextProvider) {
        SAMLLogoutFilter samlLogoutFilter = new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[] { logoutHandler() }, new LogoutHandler[] { logoutHandler() });
        samlLogoutFilter.setProfile(singleLogoutProfile);
        samlLogoutFilter.setContextProvider(contextProvider);
        samlLogoutFilter.setSamlLogger(samlLogger);
        return samlLogoutFilter;
    }

    private SAMLLogoutProcessingFilter samlLogoutProcessingFilter(SAMLContextProvider contextProvider) {
        SAMLLogoutProcessingFilter samlLogoutProcessingFilter = new SAMLLogoutProcessingFilter(successLogoutHandler(),
                logoutHandler());
        samlLogoutProcessingFilter.setLogoutProfile(singleLogoutProfile);
        samlLogoutProcessingFilter.setContextProvider(contextProvider);
        samlLogoutProcessingFilter.setSamlLogger(samlLogger);
        samlLogoutProcessingFilter.setSAMLProcessor(samlProcessor);
        return samlLogoutProcessingFilter;
    }

    private SAMLProcessor samlProcessor() {
        Collection<SAMLBinding> bindings = new ArrayList<>();
        bindings.add(httpRedirectDeflateBinding(parserPool));
        bindings.add(httpPostBinding(parserPool));
        return new SAMLProcessorImpl(bindings);
    }

    private CachingMetadataManager cachingMetadataManager() {
        List<MetadataProvider> providers = new ArrayList<>();
        providers.add(extendedMetadataDelegate);

        CachingMetadataManager cachingMetadataManager = null;
        try {
            cachingMetadataManager = new CachingMetadataManager(providers);
        } catch (MetadataProviderException e) {
            logger.error("Error while initializing cachingMetadataManager", e);
        }

        cachingMetadataManager.setKeyManager(serviceProvider.keyManager);
        return cachingMetadataManager;
    }

    private StaticBasicParserPool staticBasicParserPool() {
        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        try {
            parserPool.initialize();
        } catch (XMLParserException e) {
            logger.error("Error while initializing staticBasicParserPool", e);
        }
        return parserPool;
    }

    private ExtendedMetadataDelegate extendedMetadataDelegate(ExtendedMetadata extendedMetadata) {
        ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(metadataProvider,
                extendedMetadata);
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);
        return extendedMetadataDelegate;
    }

    private ExtendedMetadata extendedMetadata(boolean discoveryEnabled) {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(discoveryEnabled);
        extendedMetadata.setSignMetadata(true);
        return extendedMetadata;
    }

    private WebSSOProfileOptions webSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }

    private void bootstrap() {
        try {
            PaosBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            logger.error("Error while initializing bootstrap", e);
        }

        NamedKeyInfoGeneratorManager manager = Configuration.getGlobalSecurityConfiguration()
                .getKeyInfoGeneratorManager();
        X509KeyInfoGeneratorFactory generator = new X509KeyInfoGeneratorFactory();
        generator.setEmitEntityCertificate(true);
        generator.setEmitEntityCertificateChain(true);
        manager.registerFactory(SAMLConstants.SAML_METADATA_KEY_INFO_GENERATOR, generator);
    }

    private HTTPPostBinding httpPostBinding(ParserPool parserPool) {
        return new HTTPPostBinding(parserPool, VelocityFactory.getEngine());
    }

    private HTTPRedirectDeflateBinding httpRedirectDeflateBinding(ParserPool parserPool) {
        return new HTTPRedirectDeflateBinding(parserPool);
    }

    private SAMLProcessingFilter samlWebSSOProcessingFilter(SAMLAuthenticationProvider samlAuthenticationProvider,
            SAMLContextProvider contextProvider, SAMLProcessor samlProcessor, String failureRedirectUrl)
            throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();

        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder(
                objectPostProcessor);
        authenticationManagerBuilder.authenticationProvider(samlAuthenticationProvider);
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManagerBuilder.build());
        samlWebSSOProcessingFilter.setContextProvider(contextProvider);
        samlWebSSOProcessingFilter.setSAMLProcessor(samlProcessor);
        SamlAuthenticationFailureHandler samlAuthenticationFailureHandler = new SamlAuthenticationFailureHandler();
        samlAuthenticationFailureHandler.setDefaultFailureUrl(failureRedirectUrl);
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(samlAuthenticationFailureHandler);
        return samlWebSSOProcessingFilter;
    }

    private MetadataGeneratorFilter metadataGeneratorFilter(SAMLEntryPoint samlEntryPoint,
            ExtendedMetadata extendedMetadata) {
        MetadataGeneratorFilter metadataGeneratorFilter = new MetadataGeneratorFilter(
                getMetadataGenerator(samlEntryPoint, extendedMetadata));
        metadataGeneratorFilter.setManager(cachingMetadataManager);
        return metadataGeneratorFilter;
    }

    private FilterChainProxy samlFilter(SAMLEntryPoint samlEntryPoint, SAMLLogoutFilter samlLogoutFilter,
            SAMLLogoutProcessingFilter samlLogoutProcessingFilter, SAMLContextProvider contextProvider,
            String failureRedirectUrl) {
        List<SecurityFilterChain> chains = new ArrayList<>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter(contextProvider)));
        try {
            chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                    samlWebSSOProcessingFilter(samlAuthenticationProvider, contextProvider, samlProcessor,
                            failureRedirectUrl)));
        } catch (Exception e) {
           logger.error("Error while initializing samlFilter", e);
        }
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter));
        SAMLDiscovery samlDiscovery = new SAMLDiscovery();
        samlDiscovery.setMetadata(cachingMetadataManager);
        samlDiscovery.setContextProvider(contextProvider);
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"), samlDiscovery));
        return new FilterChainProxy(chains);
    }

    private WebSSOProfile webSSOProfile() {
        WebSSOProfileImpl webSSOProfile = new WebSSOProfileImpl(samlProcessor, cachingMetadataManager);
        webSSOProfile.setResponseSkew(serviceProvider.responseSkew);
        return webSSOProfile;
    }

    private SingleLogoutProfile singleLogoutProfile() {
        SingleLogoutProfileImpl singleLogoutProfile = new SingleLogoutProfileImpl();
        singleLogoutProfile.setMetadata(cachingMetadataManager);
        singleLogoutProfile.setProcessor(samlProcessor);
        singleLogoutProfile.setResponseSkew(serviceProvider.responseSkew);
        return singleLogoutProfile;
    }

    private SAMLAuthenticationProvider samlAuthenticationProvider(WebSSOProfileConsumerImpl webSSOProfileConsumer) {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setForcePrincipalAsString(forcePrincipalAsString);
        samlAuthenticationProvider.setSamlLogger(samlLogger);
        samlAuthenticationProvider.setConsumer(webSSOProfileConsumer);
        samlAuthenticationProvider.setUserDetails(this.samlUserDetailsService);
        return samlAuthenticationProvider;
    }

    private SAMLContextProvider contextProvider() {
        SAMLContextProviderLB contextProvider = new SAMLContextProviderLB();
        contextProvider.setMetadata(cachingMetadataManager);
        contextProvider.setScheme(serviceProvider.protocol);
        contextProvider.setServerName(serviceProvider.hostName);
        contextProvider.setContextPath(serviceProvider.basePath);
        contextProvider.setKeyManager(serviceProvider.keyManager);

        MetadataCredentialResolver resolver = new MetadataCredentialResolver(cachingMetadataManager,
                serviceProvider.keyManager);
        PKIXTrustEvaluator pkixTrustEvaluator = new CertPathPKIXTrustEvaluator();
        PKIXInformationResolver pkixInformationResolver = new PKIXInformationResolver(resolver, cachingMetadataManager,
                serviceProvider.keyManager);

        contextProvider.setPkixResolver(pkixInformationResolver);
        contextProvider.setPkixTrustEvaluator(pkixTrustEvaluator);
        contextProvider.setMetadataResolver(resolver);

        if (serviceProvider.storageFactory != null) {
            contextProvider.setStorageFactory(serviceProvider.storageFactory);
        }

        return contextProvider;
    }

    private MetadataGenerator getMetadataGenerator(SAMLEntryPoint samlEntryPoint, ExtendedMetadata extendedMetadata) {
        MetadataGenerator metadataGenerator = new MetadataGenerator();

        metadataGenerator.setSamlEntryPoint(samlEntryPoint);
        metadataGenerator.setEntityBaseURL(entityBaseURL());
        metadataGenerator.setKeyManager(serviceProvider.keyManager);
        metadataGenerator.setEntityId(serviceProvider.entityId);
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setExtendedMetadata(extendedMetadata);

        return metadataGenerator;
    }

    public class IdentityProvider {

        private String metadataFilePath;
        private boolean discoveryEnabled = true;

        public IdentityProvider metadataFilePath(String metadataFilePath) {
            this.metadataFilePath = metadataFilePath;
            return this;
        }

        public IdentityProvider discoveryEnabled(boolean discoveryEnabled) {
            this.discoveryEnabled = discoveryEnabled;
            return this;
        }

        private MetadataProvider metadataProvider() {
            if (metadataFilePath.startsWith("http")) {
                return httpMetadataProvider();
            } else {
                return fileSystemMetadataProvider();
            }
        }

        private HTTPMetadataProvider httpMetadataProvider() {

            try {
                HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(new Timer(), new HttpClient(),
                        metadataFilePath);
                httpMetadataProvider.setParserPool(parserPool);
                return httpMetadataProvider;
            } catch (MetadataProviderException e) {
                logger.error("Error while initializing httpMetadataProvider", e);
                return null;
            }
        }

        private FilesystemMetadataProvider fileSystemMetadataProvider() {
            DefaultResourceLoader loader = new DefaultResourceLoader();
            Resource metadataResource = loader.getResource(metadataFilePath);

            File samlMetadata = null;
            try {
                samlMetadata = metadataResource.getFile();
            } catch (IOException e) {
                logger.error("Error while initializing fileSystemMetadataProvider", e);
            }

            FilesystemMetadataProvider filesystemMetadataProvider = null;
            try {
                filesystemMetadataProvider = new FilesystemMetadataProvider(samlMetadata);
            } catch (MetadataProviderException e) {
                logger.error("Error while initializing fileSystemMetadataProvider", e);
            }
            filesystemMetadataProvider.setParserPool(parserPool);

            return filesystemMetadataProvider;
        }

        public SAMLConfigurer and() {
            return SAMLConfigurer.this;
        }
    }

    private MetadataDisplayFilter metadataDisplayFilter(SAMLContextProvider contextProvider) {
        MetadataDisplayFilter metadataDisplayFilter = new MetadataDisplayFilter();
        metadataDisplayFilter.setContextProvider(contextProvider);
        metadataDisplayFilter.setKeyManager(serviceProvider.keyManager);
        metadataDisplayFilter.setManager(cachingMetadataManager);
        return metadataDisplayFilter;
    }

    public class ServiceProvider {

        private KeyStore keyStore = new KeyStore();
        private KeyManager keyManager;
        private String protocol;
        private String hostName;
        private String basePath;
        private String entityId;
        private SAMLMessageStorageFactory storageFactory;
        private int responseSkew = 60;
        private long maxAuthenticationAge = 7200;

        public ServiceProvider protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public ServiceProvider hostname(String hostname) {
            this.hostName = hostname;
            return this;
        }

        public ServiceProvider basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public ServiceProvider entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public ServiceProvider storageFactory(SAMLMessageStorageFactory storageFactory) {
            this.storageFactory = storageFactory;
            return this;
        }

        public ServiceProvider responseSkew(int responseSkew) {
            this.responseSkew = responseSkew;
            return this;
        }

        public ServiceProvider maxAuthenticationAge(long maxAuthenticationAge) {
            this.maxAuthenticationAge = maxAuthenticationAge;
            return this;
        }

        public KeyStore keyStore() {
            return keyStore;
        }

        public SAMLConfigurer and() {
            return SAMLConfigurer.this;
        }

        private KeyManager keyManager() {
            DefaultResourceLoader loader = new DefaultResourceLoader();
            Resource storeFile = loader.getResource(keyStore.getStoreFilePath());
            if (keyStore.getStoreFilePath().startsWith("file://")) {
                try {
                    storeFile = new FileSystemResource(storeFile.getFile());
                } catch (IOException e) {
                    logger.error("Error while initializing keyManager", e);
                    throw new RuntimeException("Cannot load file system resource: " + keyStore.getStoreFilePath(), e);
                }
            }
            Map<String, String> passwords = new HashMap<>();
            passwords.put(keyStore.getKeyname(), keyStore.getKeyPassword());
            return new JKSKeyManager(storeFile, keyStore.getPassword(), passwords, keyStore.getKeyname());
        }

        public class KeyStore {
            private String storeFilePath;
            private String password;
            private String keyname;
            private String keyPassword;

            public KeyStore storeFilePath(String storeFilePath) {
                this.storeFilePath = storeFilePath;
                return this;
            }

            public KeyStore password(String password) {
                this.password = password;
                return this;
            }

            public KeyStore keyname(String keyname) {
                this.keyname = keyname;
                return this;
            }

            public KeyStore keyPassword(String keyPasswordword) {
                this.keyPassword = keyPasswordword;
                return this;
            }

            public ServiceProvider and() {
                return ServiceProvider.this;
            }

            public String getStoreFilePath() {
                return storeFilePath;
            }

            public String getPassword() {
                return password;
            }

            public String getKeyname() {
                return keyname;
            }

            public String getKeyPassword() {
                return keyPassword;
            }

            @Override
            public String toString() {
                return "KeyStore{" + "storeFilePath='" + storeFilePath + '\'' + ", password='" + password + '\''
                        + ", keyname='" + keyname + '\'' + ", keyPassword='" + keyPassword + '\'' + '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;

                KeyStore keyStore = (KeyStore) o;

                if (storeFilePath != null ? !storeFilePath.equals(keyStore.storeFilePath)
                        : keyStore.storeFilePath != null)
                    return false;
                if (password != null ? !password.equals(keyStore.password) : keyStore.password != null)
                    return false;
                if (keyname != null ? !keyname.equals(keyStore.keyname) : keyStore.keyname != null)
                    return false;
                return keyPassword != null ? keyPassword.equals(keyStore.keyPassword) : keyStore.keyPassword == null;

            }

            @Override
            public int hashCode() {
                int result = storeFilePath != null ? storeFilePath.hashCode() : 0;
                result = 31 * result + (password != null ? password.hashCode() : 0);
                result = 31 * result + (keyname != null ? keyname.hashCode() : 0);
                result = 31 * result + (keyPassword != null ? keyPassword.hashCode() : 0);
                return result;
            }
        }
    }

    final class DefaultRequiresCsrfMatcher implements RequestMatcher {
        private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

        public boolean matches(HttpServletRequest request) {
            return !allowedMethods.matcher(request.getMethod()).matches();
        }
    }
}
