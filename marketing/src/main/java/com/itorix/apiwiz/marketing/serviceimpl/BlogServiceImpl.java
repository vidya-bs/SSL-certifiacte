package com.itorix.apiwiz.marketing.serviceimpl;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.blogs.model.Blog;
import com.itorix.apiwiz.marketing.blogs.model.BlogResponseOverview;
import com.itorix.apiwiz.marketing.blogs.model.BlogStatus;
import com.itorix.apiwiz.marketing.dao.BlogsDao;
import com.itorix.apiwiz.marketing.service.BlogService;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@CrossOrigin
@RestController
@Slf4j
public class BlogServiceImpl implements BlogService {

  @Autowired
  private BlogsDao blogsDao;

  @Override
  @UnSecure(ignoreValidation = true)
  public ResponseEntity<?> getBlogById(String interactionid, String jsessionid, String apikey, String blogId)
      throws Exception {
    log.info("getBlogById {}",blogId);
    Blog blog = blogsDao.getBlogById(blogId);
    if (blog != null) {
      return new ResponseEntity<>(blog, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Override
  @UnSecure(ignoreValidation = true)
  public ResponseEntity<?> getAllBlogsWithFilterByPageSizeAndOffset(String interactionid, String jsessionid,
      String apikey, int pageSize, int offset, Boolean isCaseStudy,BlogStatus status) throws Exception {
    log.info("getAllBlogs using page size {} and offset {}",pageSize,offset);
    BlogResponseOverview response = blogsDao.getAllBlogsWithFilterByPageSizeAndOffset(pageSize, offset,
        isCaseStudy,status);
    if (response != null) {
      return new ResponseEntity<>(response, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(new ArrayList<Blog>(),HttpStatus.NOT_FOUND);
    }

  }

  @Override
  @UnSecure(ignoreValidation = true)
  public ResponseEntity<?> getBlogBySlug(String interactionid, String jsessionid, String apikey, String slug)
      throws Exception {
    log.info("getBlogBySlug {}",slug);
    Blog blog = blogsDao.getBlogBySlug(slug);
    if (blog != null) {
      return new ResponseEntity<>(blog, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }



  @Override
  public ResponseEntity<?> createBlog(String interactionid, String jsessionid, Blog blog)
      throws Exception {
    log.info("createBlog {}",blog);
    Blog createdBlog = blogsDao.createBlog(blog);
    if (createdBlog != null)
      return new ResponseEntity<>(createdBlog, HttpStatus.CREATED);
    else {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
  }

  @Override
  public ResponseEntity<?> changeStatus(String interactionid, String jsessionid,String blogId,
      BlogStatus status) throws Exception {
    log.info("changeStatus {}",blogId);
    if (status.name().equals("DRAFT")) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return blogsDao.changeStatus(blogId, status);

  }

  @Override
  public ResponseEntity<?> editBlog(String interactionid, String jsessionid,String blogId, Blog blog)
      throws Exception {
    log.info("editBlog {}",blogId);
    return blogsDao.editBlog(blogId, blog);

  }

  @Override
  public ResponseEntity<?> deleteBlog(String interactionid, String jsessionid,String blogId)
      throws Exception {
    log.info("deleteBlog {}",blogId);
    if (blogsDao.deleteBlog(blogId) != null)
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

}