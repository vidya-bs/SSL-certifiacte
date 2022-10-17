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
    public ResponseEntity<?> createNews(@RequestBody News news) throws Exception;

    @PatchMapping(value = "/news/{newsId}")
    public ResponseEntity<?> updateNews(@RequestBody News news, @PathVariable(value = "newsId")String newsId) throws Exception;

    @PatchMapping(value = "/news/{id}/status/{status}")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String newsId, @PathVariable("status") NewsStatus status) throws Exception;


    @UnSecure(ignoreValidation = true)
    @RequestMapping(value = "/news")
    public ResponseEntity<?> fetchAllNews(@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize
    ) throws Exception;

    @UnSecure(ignoreValidation = true)
    @RequestMapping(value = "/news/{filterValue}")
    public ResponseEntity<?> getDataByFilter(
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "filter") String filter,
            @PathVariable(value = "filterValue") String filterValue
    ) throws Exception;

    @DeleteMapping(value = "/news/{id}")
    public ResponseEntity<?> deleteNews(
            @PathVariable("id") String newsId
    ) throws Exception;

}
