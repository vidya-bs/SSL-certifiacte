package com.itorix.apiwiz.marketing.service;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.blogs.model.Blog;
import com.itorix.apiwiz.marketing.blogs.model.BlogStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface BlogService {

  @UnSecure(ignoreValidation = true)
  @RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/blogs/{blogId}")
  public ResponseEntity<?> getBlogById(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
      @RequestHeader(value = "x-apikey",required = false) String apikey,
      @PathVariable("blogId") String blogId
  ) throws Exception;


  @UnSecure(ignoreValidation = true)
  @RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/blogs")
  public ResponseEntity<?> getAllBlogsWithFilterByPageSizeAndOffset(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
      @RequestHeader(value = "x-apikey",required = false) String apikey,
      @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
      @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
      @RequestParam(value = "isCaseStudy", required = false) Boolean isCaseStudy,
      @RequestParam(value="status",required=false) BlogStatus status
  ) throws Exception;


  @UnSecure(ignoreValidation = true)
  @RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/blogs/slug/{slug}")
  public ResponseEntity<?> getBlogBySlug(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
      @RequestHeader(value = "x-apikey",required = false) String apikey,
      @PathVariable("slug") String slug
  ) throws Exception;





  @RequestMapping(method = RequestMethod.POST, value = "/v1/marketing/blogs")
  public ResponseEntity<?> createBlog(
      @RequestHeader(value = "interactionid") String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @RequestBody Blog blog
  ) throws Exception;



  @RequestMapping(method = RequestMethod.PATCH, value = "/v1/marketing/blogs/{blogId}/status/{status}")
  public ResponseEntity<?> changeStatus(
      @RequestHeader(value = "interactionid") String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable("blogId") String blogId,
      @PathVariable("status") BlogStatus status
  ) throws Exception;

  @RequestMapping(method = RequestMethod.PUT, value = "/v1/marketing/blogs/{blogId}")
  public ResponseEntity<?> editBlog(@RequestHeader(value = "interactionid") String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable("blogId") String blogId,
      @RequestBody Blog blog
  ) throws Exception;

  @RequestMapping(method = RequestMethod.DELETE, value = "/v1/marketing/blogs/{blogId}")
  public ResponseEntity<?> deleteBlog(
      @RequestHeader(value = "interactionid") String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable("blogId") String blogId
  ) throws Exception;

}