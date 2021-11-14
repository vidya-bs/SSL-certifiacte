package com.itorix.apiwiz.sso.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
public interface SSOService {

    @RequestMapping(method = RequestMethod.GET, value = "/saml/token", produces = { "application/json" })
    public @ResponseBody ResponseEntity<Object> getssoToken(
            @RequestParam(value = "redirect_url", required = true) String redirectUrl,
            @RequestHeader(value = "x-source", required = false) String source) throws Exception;

//    @RequestMapping(method = RequestMethod.GET, value = "/saml/idpMetadata", produces = { "application/json" })
//    public @ResponseBody String getSSOMetadata() throws Exception;

    @RequestMapping(method = { RequestMethod.POST }, value = "/v1/saml/config", consumes = { "multipart/form-data" })
    public ResponseEntity<Object> createOrUpdateSamlConfig(
            @RequestPart(value = "samlConfig", required = false) String samlConfig,
            @RequestPart(value = "metafile", required = true) MultipartFile metadata,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid) throws Exception;

    @RequestMapping(method = RequestMethod.GET, value = "/v1/saml/config", produces = { "application/json" })
    public ResponseEntity<Object> getSAMLConfig() throws Exception;

}
