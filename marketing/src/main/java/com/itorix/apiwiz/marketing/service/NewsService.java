package com.itorix.apiwiz.marketing.service;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.news.model.News;
import com.itorix.apiwiz.marketing.news.model.NewsFilterRequest;
import com.itorix.apiwiz.marketing.news.model.NewsStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/v1/marketing")
public interface NewsService {

  @UnSecure(ignoreValidation = true)
  @PostMapping(value = "/news")
  public ResponseEntity<?> createNews(@RequestHeader(value = "x-apikey") String apikey,
      @RequestBody News news) throws Exception;

  @UnSecure(ignoreValidation = true)
  @PatchMapping(value = "/news/{newsId}")
  public ResponseEntity<?> updateNews(@RequestHeader(value = "x-apikey") String apikey,
      @RequestBody News news, @PathVariable(value = "newsId") String newsId) throws Exception;

  @UnSecure(ignoreValidation = true)
  @PatchMapping(value = "/news/{id}/status/{status}")
  public ResponseEntity<?> changeStatus(@RequestHeader(value = "x-apikey") String apikey,
      @PathVariable("id") String newsId, @PathVariable("status") NewsStatus status)
      throws Exception;


  @UnSecure(ignoreValidation = true)
  @GetMapping(value = "/news")
  public ResponseEntity<?> fetchAllNews(@RequestHeader(value = "x-apikey") String apikey,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
      @RequestParam(value = "status", required = false) String status
  ) throws Exception;

  @UnSecure(ignoreValidation = true)
  @PostMapping("/news-type")
  public ResponseEntity<?> getDataByFilter(@RequestHeader(value = "x-apikey") String apikey,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
      @RequestParam(value = "filter") String filter,
      @RequestBody NewsFilterRequest newsFilterRequest
  ) throws Exception;

  @UnSecure(ignoreValidation = true)
  @DeleteMapping(value = "/news/{id}")
  public ResponseEntity<?> deleteNews(@RequestHeader(value = "x-apikey") String apikey,
      @PathVariable("id") String newsId
  ) throws Exception;

}
