package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.marketing.blogs.model.Blog;
import com.itorix.apiwiz.marketing.blogs.model.BlogResponseOverview;
import com.itorix.apiwiz.marketing.blogs.model.BlogStatus;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BlogsDao {

  @Qualifier("masterMongoTemplate")
  @Autowired
  private MongoTemplate masterMongoTemplate;

  public Blog getBlogById(String blogId) {
    Query query = new Query().addCriteria(Criteria.where("_id").is(blogId));
    log.debug("getBlogById {}",query);
    return masterMongoTemplate.findOne(query, Blog.class);
  }

  public BlogResponseOverview getAllBlogsWithFilterByPageSizeAndOffset(int pageSize, int offset,
      Boolean isCaseStudy,BlogStatus status) {

    BlogResponseOverview overview=new BlogResponseOverview();
    Query query=new Query().with(Sort.by(Sort.Direction.DESC, "cts")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
    List<Blog> blogs;
    blogs = masterMongoTemplate.find(query, Blog.class);
    if(blogs.isEmpty()){
      return null;
    }

    Query query2 = new Query().with(Sort.by(Sort.Direction.DESC, "cts"));

    if (null == isCaseStudy && null == status) {
      Long total = masterMongoTemplate.count(query2, Blog.class);
      Pagination pagination = new Pagination();
      pagination.setOffset(offset);
      pagination.setPageSize(pageSize);
      pagination.setTotal(total);
      overview.setBlog(blogs);
      overview.setPagination(pagination);
      return overview;
    }

    Query query3=new Query().with(Sort.by(Sort.Direction.DESC, "cts"));


    if(status!=null&&isCaseStudy==null){
      query2.addCriteria(Criteria.where("meta.status").is(status)).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
      query3.addCriteria(Criteria.where("meta.status").is(status));
      blogs=masterMongoTemplate.find(query2,Blog.class);
      Long total = masterMongoTemplate.count(query3, Blog.class);
      Pagination pagination = new Pagination();
      pagination.setOffset(offset);
      pagination.setPageSize(pageSize);
      pagination.setTotal(total);
      overview.setBlog(blogs);
      overview.setPagination(pagination);
      return overview;
    }


    if(isCaseStudy!=null&&status==null){
      if(isCaseStudy){
        query2.addCriteria(Criteria.where("meta.isCaseStudy").is(true)).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        query3.addCriteria(Criteria.where("meta.isCaseStudy").is(true));
      }
      else{
        query2.addCriteria(Criteria.where("meta.isCaseStudy").is(false)).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
        query3.addCriteria(Criteria.where("meta.isCaseStudy").is(false));
      }
      blogs=masterMongoTemplate.find(query2,Blog.class);
      Long total = masterMongoTemplate.count(query3, Blog.class);
      Pagination pagination = new Pagination();
      pagination.setOffset(offset);
      pagination.setPageSize(pageSize);
      pagination.setTotal(total);
      overview.setBlog(blogs);
      overview.setPagination(pagination);
      return overview;
    }

    if(isCaseStudy!=null&&status!=null){
      query2.addCriteria(Criteria.where("meta.isCaseStudy").is(isCaseStudy)).addCriteria(Criteria.where("meta.status").is(status)).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
      query3.addCriteria(Criteria.where("meta.isCaseStudy").is(isCaseStudy)).addCriteria(Criteria.where("meta.status").is(status));
    }

    blogs=masterMongoTemplate.find(query2,Blog.class);
    Long total = masterMongoTemplate.count(query3, Blog.class);
    Pagination pagination = new Pagination();
    pagination.setOffset(offset);
    pagination.setPageSize(pageSize);
    pagination.setTotal(total);
    overview.setBlog(blogs);
    overview.setPagination(pagination);
    return overview;
  }


  public Blog getBlogBySlug(String slug) {
    Query query = new Query().addCriteria(Criteria.where("meta.slug").is(slug));
    log.debug("getBlogBySlug {}",query);
    return masterMongoTemplate.findOne(query, Blog.class);
  }


  public Blog createBlog(Blog blog) {
    String title = blog.getMeta().getTitle();
    String slug = title.toLowerCase().replace(" ", "-").replace(":","-");
    List<Blog> existingBlogs = masterMongoTemplate.findAll(Blog.class);
    if (existingBlogs.stream().anyMatch(b -> StringUtils.equals(b.getMeta().getSlug(), slug))) {
      return null;
    }
    blog.getMeta().setSlug(slug);
    long currentTime = System.currentTimeMillis();
    blog.setCts(currentTime);
    blog.setMts(currentTime);
    return masterMongoTemplate.save(blog);
  }

  public ResponseEntity<?> changeStatus(String blogId, BlogStatus status) {
    Query query = new Query().addCriteria(Criteria.where("_id").is(blogId));
    log.debug("changeStatus {}",query);
    Blog blog = masterMongoTemplate.findOne(query, Blog.class);
    if (blog != null) {
      Update update = new Update();
      update.set("meta.status", status.name());
      masterMongoTemplate.upsert(query, update, Blog.class);
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  public ResponseEntity<?> editBlog(String blogId, Blog blog) {
    Query query = new Query().addCriteria(Criteria.where("_id").is(blogId));

    log.debug("editBlog {}", query);
    Blog existingBlog = masterMongoTemplate.findOne(query, Blog.class);
    if (existingBlog != null) {
      if (null != blog.getContent()) {
        existingBlog.setContent(blog.getContent());
      }
      if (null != blog.getMeta()) {
        if (null != blog.getMeta().getIsCaseStudy()) {
          existingBlog.getMeta().setIsCaseStudy(blog.getMeta().getIsCaseStudy());
        }
        if (null != blog.getMeta().getTags()) {
          existingBlog.getMeta().setTags(blog.getMeta().getTags());
        }
        if (null != blog.getMeta().getTitle()) {
          String newSlug=blog.getMeta().getTitle().toLowerCase().replace(" ","-").replace(":","-");
          existingBlog.getMeta().setTitle(blog.getMeta().getTitle());
          existingBlog.getMeta().setSlug(newSlug);
        }
        if (null != blog.getMeta().getSlug()) {
          existingBlog.getMeta().setSlug(existingBlog.getMeta().getSlug());
        }
        if (null != blog.getMeta().getAuthor()) {
          existingBlog.getMeta().setAuthor(blog.getMeta().getAuthor());
        }
        if (null != blog.getMeta().getDesignation()) {
          existingBlog.getMeta().setDesignation(blog.getMeta().getDesignation());
        }
        if (null != blog.getMeta().getAuthor_image_url()) {
          existingBlog.getMeta().setAuthor_image_url(blog.getMeta().getAuthor_image_url());
        }
        if (null != blog.getMeta().getBanner_image_url()) {
          existingBlog.getMeta().setBanner_image_url(blog.getMeta().getBanner_image_url());
        }
        if (null != blog.getMeta().getThumbnail_image_url()) {
          existingBlog.getMeta().setThumbnail_image_url(blog.getMeta().getThumbnail_image_url());
        }
        if (null != blog.getMeta().getStatus()) {
          existingBlog.getMeta().setStatus(blog.getMeta().getStatus());
        }
      }
      existingBlog.setMts(System.currentTimeMillis());
      masterMongoTemplate.save(existingBlog);
      return new ResponseEntity<>(HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


  public Blog deleteBlog(String blogId) {
    Query query = new Query().addCriteria(Criteria.where("_id").is(blogId));
    log.debug("deleteBlog {}",query);
    return masterMongoTemplate.findAndRemove(query, Blog.class);
  }

}