package com.itorix.apiwiz.marketing.service;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.news.model.News;
import com.itorix.apiwiz.marketing.news.model.NewsStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/v1/marketing")
public interface NewsService {
    @PostMapping(value = "/news")
    public ResponseEntity<?> createNews(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @RequestBody News news
    ) throws Exception;

    @PatchMapping(value = "/news/{newsId}")
    public ResponseEntity<?> updateNews(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @RequestBody News news,
            @PathVariable(value = "newsId")String newsId
    ) throws Exception;

    @PatchMapping(value = "/news/{id}/status/{status}")
    public ResponseEntity<?> changeStatus(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable("id") String newsId,
            @PathVariable("status") NewsStatus status
    ) throws Exception;


    @UnSecure(ignoreValidation = true)
    @RequestMapping(value = "/news")
    public ResponseEntity<?> fetchAllNews(
            @RequestHeader(value = "interactionid", required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
            @RequestHeader(value = "x-apikey",required = false) String apikey,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @RequestMapping(value = "/news/{filterValue}")
    public ResponseEntity<?> getDataByFilter(
            @RequestHeader(value = "interactionid", required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
            @RequestHeader(value = "x-apikey",required = false) String apikey,
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "filter") String filter,
            @PathVariable(value = "filterValue") String filterValue
    ) throws Exception;

    @DeleteMapping(value = "/news/{id}")
    public ResponseEntity<?> deleteNews(
            @RequestHeader(value = "interactionid") String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable("id") String newsId
    ) throws Exception;

}
