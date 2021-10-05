package com.itorix.apiwiz.sso.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.sso.dao.SSODao;
import com.itorix.apiwiz.sso.exception.ErrorCodes;
import com.itorix.apiwiz.sso.exception.ItorixException;
import com.itorix.apiwiz.sso.model.*;
import com.itorix.apiwiz.sso.service.SSOService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@CrossOrigin
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SSOServiceImpl implements SSOService {

    private static final Logger logger = LoggerFactory.getLogger(SSOServiceImpl.class);

    public static final String SESSION_TOKEN_NAME = "JSESSIONID";

    public static final String API_KEY_NAME = "x-apikey";

    public static final String INTERACTION_ID = "interactionId";

    @Autowired
    protected BaseRepository baseRepository;

    @Autowired
    HttpSession session;

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Autowired
    SSODao ssoDao;

    @Value("${itorix.core.security.jwks.url}")
    String certificateUrl;

    @Qualifier("noVerify")
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    private RSAEncryption rsaEncryption;

    @Override
    public ResponseEntity<Object> getssoToken(@RequestParam(value = "redirect_url", required = true) String redirectUrl,
            @RequestParam(value = "x-source", required = false) String source) throws Exception {
        logger.debug("Inside getSSOToken ");
        SAMLCredential credentials = (SAMLCredential) SecurityContextHolder.getContext().getAuthentication()
                .getCredentials();
        UserInfo user = ssoDao.createOrUpdateUser(credentials);
        logger.debug("User created successfully.");
        session.invalidate();
        SecurityContextHolder.clearContext();

        Object authenticate = authenticate(user, true);
        HttpHeaders headers = new HttpHeaders();
        if (authenticate != null) {
            headers.add(SESSION_TOKEN_NAME, ((UserSession) authenticate).getId());
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1025"), "SSO-1025");
        }

        StringBuilder url = new StringBuilder(redirectUrl);
        if (!StringUtils.isEmpty(source)) {
            HttpHeaders certificateHeaders = new HttpHeaders();
            certificateHeaders.add("x-source", source);
            certificateHeaders.add("x-tenant", ((UserSession) authenticate).getWorkspaceId());

            HttpEntity<String> entity = new HttpEntity<>(certificateHeaders);
            ResponseEntity<String> result = restTemplate.exchange(certificateUrl, HttpMethod.GET, entity, String.class);
            url.append("?").append("code=").append(encryptText(((UserSession) authenticate).getId(), result.getBody()));
        } else {
            url.append("?").append("code=").append(((UserSession) authenticate).getId());
        }

        headers.setLocation(URI.create(url.toString()));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @Override
    public ResponseEntity<Object> createOrUpdateSamlConfig(
            @RequestPart(value = "samlConfig", required = false) String config,
            @RequestPart(value = "metafile", required = false) MultipartFile metadata,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SAMLConfig ssoConfig = mapper.readValue(config, SAMLConfig.class);
        RestTemplate restTemplate = new RestTemplate();

        if (StringUtils.hasText(ssoConfig.getMetadataUrl())) {
            String metaDataResponse = restTemplate.getForEntity(ssoConfig.getMetadataUrl(), String.class).getBody();
            if (!StringUtils.hasText(metaDataResponse)) {
                throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1020"), "SSO-1020");
            }
            ssoConfig.setMetadata(metaDataResponse.getBytes());
        } else if (metadata != null && metadata.getInputStream() != null && metadata.getBytes() != null
                && metadata.getBytes().length != 0) {
            ssoConfig.setMetadata(metadata.getBytes());
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1019"), "SSO-1019");
        }

        ssoDao.createOrUpdateSamlConfig(ssoConfig);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public String getSSOMetadata() throws Exception {
        return ssoDao.getSSOMetadata();
    }

    @Override
    public ResponseEntity<Object> getSAMLConfig() throws Exception {
        return new ResponseEntity<>(ssoDao.getSamlConfig(), HttpStatus.OK);
    }

    public String encryptText(String msg, String publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA");

        try {
            PublicKey loadPublicKey = loadPublicKey(publicKey);
            cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey);
        } catch (Exception e) {
            logger.error("error while encrypting jsession", e);
        }
        return Base64.encodeBase64String(cipher.doFinal(msg.getBytes("UTF-8")));
    }

    private PublicKey loadPublicKey(String publicKeyContent)
            throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, URISyntaxException {
        publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyContent));
        return kf.generatePublic(keySpecX509);
    }

    public User findByEmailUserName(String userId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where(User.LABEL_EMAIL).is(userId),
                Criteria.where(User.LABEL_LOGINID).is(userId)));
        return masterMongoTemplate.findOne(query, User.class);
    }

    public Workspace getWorkspace(String workapaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workapaceId)));
        Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
        return workspace;
    }

    public UserSession authenticate(UserInfo userInfo, boolean preAuthenticated) throws Exception {
        logger.debug("UserService.authenticate : " + userInfo);
        UserSession userSession = null;
        if (preAuthenticated || userInfo.allowLogin() == true) {
            User user = findByEmailUserName(userInfo.getLoginId());
            Workspace workspace = getWorkspace(userInfo.getWorkspaceId());
            if (user == null) {
                throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1012"), "SSO-1012");
            }
            if (workspace == null) {
                throw new ItorixException(ErrorCodes.errorMessage.get("SSO-022"), "SSO-022");
            }
            UserWorkspace userWorkspace = user.getUserWorkspace(userInfo.getWorkspaceId());
            if (userWorkspace == null || userWorkspace.getActive() != true) { // (!user.getUserWorkspace(userInfo.getWorkspaceId()).getActive())){
                throw new ItorixException(ErrorCodes.errorMessage.get("SSO-022"), "SSO-022");
            }

            if (user.canLogin() != true) {
                throw new ItorixException(ErrorCodes.errorMessage.get("SSO-002"), "SSO-002");
            }

            if (preAuthenticated || rsaEncryption.decryptText(user.getPassword())
                    .equals(rsaEncryption.decryptText(userInfo.getPassword()))) {
                userSession = new UserSession(user);
                userSession.setRequestAttributes(request);
                userSession.setLoginId(user.getLoginId());
                userSession.setWorkspaceId(workspace.getName());
                userSession.setTenant(workspace.getTenant());
                userSession.setRoles(user.getUserWorkspace(workspace.getName()).getRoles());
                userSession.setUserType(user.getUserWorkspace(workspace.getName()).getUserType());
                String status = workspace.getStatus() != null && workspace.getStatus() != "" ? workspace.getStatus()
                        : "active";
                userSession.setStatus(status);
                user.setUserCount(0);
                saveUser(user);
                userSession.setPlanId(workspace.getPlanId());
                userSession.setPaymentSchedule(workspace.getPaymentSchedule());
                userSession.setSubscriptionId(workspace.getSubscriptionId());
                if (workspace.getIsTrial() == true) {
                    Date now = new Date();
                    if (workspace.getExpiresOn() != null && now.compareTo(workspace.getExpiresOn()) > 0) {
                        long diff = workspace.getExpiresOn().getTime() - now.getTime();
                        int days = 0;
                        if (diff > 0)
                            days = (int) diff / 1000 / 60 / 60 / 24;
                        userSession.setIsTrial(true);
                        userSession.setTrialPeriod(workspace.getTrialPeriod());
                        userSession.setTrialExpired("true");
                        userSession.setExpiresOn(String.valueOf(days));
                        userSession.setStatus("cancelled");
                    } else {
                        long diff = workspace.getExpiresOn().getTime() - now.getTime();
                        int days = 0;
                        if (diff > 0)
                            days = (int) diff / 1000 / 60 / 60 / 24;
                        userSession.setIsTrial(true);
                        userSession.setTrialPeriod(workspace.getTrialPeriod());
                        userSession.setTrialExpired("false");
                        userSession.setExpiresOn(String.valueOf(days));
                        userSession.setStatus(status);
                    }
                }
                masterMongoTemplate.save(userSession);
                return userSession;
            } else {
                if (user.getUserCount() < 5) {
                    user.setUserCount(user.getUserCount() + 1);
                    saveUser(user);
                    throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1012"), "SSO-1012");
                } else {
                    user.setUserStatus(UserStatus.getStatus(UserStatus.LOCKED));
                    saveUser(user);
                    throw new ItorixException(ErrorCodes.errorMessage.get("SSO-002"), "SSO-002");
                }
            }
        }
        throw new ItorixException(ErrorCodes.errorMessage.get("SSO-1012"), "SSO-1012");
    }

    public User saveUser(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        return baseRepository.save(user, masterMongoTemplate);
    }
}