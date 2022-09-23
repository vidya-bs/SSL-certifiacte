package com.itorix.apiwiz.marketing.service;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin
@RestController
public interface WebsiteStorageService {

  @RequestMapping(method = RequestMethod.POST,value = "/v1/marketing/website/upload")
  public ResponseEntity<?>uploadImage(
      @RequestHeader(value = "interactionid",required=false)String interactionid,
      @RequestHeader(value = "JSESSIONID")String jsessionid,
      @RequestPart(value = "image") MultipartFile file
  )throws Exception;
}