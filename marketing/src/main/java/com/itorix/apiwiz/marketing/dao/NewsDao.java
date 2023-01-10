package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.marketing.news.model.News;
import com.itorix.apiwiz.marketing.news.model.NewsStatus;
import lombok.extern.slf4j.Slf4j;
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NewsDao {

  @Qualifier("masterMongoTemplate")
  @Autowired
  MongoTemplate masterMongoTemplate;


  private static String encodeValue(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  private static String decodeValue(String value) {
    try {
      return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }
  public static String validateJavaDate(News news) {
    if(news.getMeta().getPublishingDate()==null)return news.getMeta().getPublishingDate();
    String validDate=null;
    String day=news.getMeta().getPublishingDate().split("-")[0];
    if(Integer.parseInt(day)<10&&day.length()==1)day='0'+day;
    String month=news.getMeta().getPublishingDate().split("-")[1];
    if(Integer.parseInt(month)<10&&month.length()==1)month='0'+month;
    String year=news.getMeta().getPublishingDate().split("-")[2];
    validDate=day+"-"+month+"-"+year;
    return  validDate;
  }

  public News createNews(News news) throws ParseException {
    log.info("News {}", news);
    List<News> allNews = getAllNews();
    String title = news.getMeta().getTitle();
//    String slug = encodeValue(title);
    String slug = title;
    if (allNews.stream().anyMatch(n -> n.getMeta().getSlug().equals(slug))) {
      return null;
    }

    String validDate=validateJavaDate(news);
    news.getMeta().setPublishingDate(validDate);
    long currentTime = System.currentTimeMillis();
    news.setCts(currentTime);
    String year = news.getMeta().getPublishingDate().split("-")[2];
    news.getMeta().setSlug(slug);
    news.getMeta().setYear(Integer.parseInt(year));
    return masterMongoTemplate.save(news);
  }

  public List<News> getAllNews() {
    return masterMongoTemplate.findAll(News.class);
  }


  public List<News> getAllNews(int offset, int pageSize) {
    Query query = new Query().with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
        .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
    return masterMongoTemplate.find(query, News.class);
  }

  public List<News> fetchAllNews(int offset, int pageSize, String status) {
    List<News> newsList = null;
    if (status == null) {
      List<News> existing = getAllNews(offset, pageSize);
      if (existing.isEmpty()) {
        return new ArrayList<>();
      }

      return existing;
    } else {
      Query query = new Query();
      query.addCriteria(Criteria.where("meta.status").is(status))
          .with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
          .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
      newsList = masterMongoTemplate.find(query, News.class);
    }
    if (newsList.isEmpty()) {
      return new ArrayList<>();
    }
    return newsList;
  }


  public ResponseEntity<Object> updateNews(News news, String newsId) {
    log.info("Request body news {}", news);
    Query query = new Query().addCriteria(Criteria.where("_id").is(newsId));
    News existingNews = masterMongoTemplate.findOne(query, News.class);
    if (existingNews == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    long currentTime = System.currentTimeMillis();
    news.setMts(currentTime);
    String year = news.getMeta().getPublishingDate().split("-")[2];
    news.getMeta().setYear(Integer.parseInt(year));
    news.getMeta().setSlug(existingNews.getMeta().getSlug());
    if (existingNews.getMeta().getStatus().equals("PUBLISH")) {
      existingNews.getMeta().setStatus(NewsStatus.PUBLISH);
    }
    Update update = new Update();
    update.set("meta", news.getMeta());
    update.set("content", news.getContent());
    masterMongoTemplate.upsert(query, update, News.class);
    return new ResponseEntity<>(news, HttpStatus.OK);
  }

  public ResponseEntity<Object> changeStatus(String releaseId, NewsStatus status) {
    Query query = new Query().addCriteria(Criteria.where("_id").is(releaseId));
    News news = masterMongoTemplate.findOne(query, News.class);
    if (news != null) {
      Update update = new Update();
      update.set("meta.status", status.name());
      masterMongoTemplate.upsert(query, update, News.class);
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }


  public List<News> getDataByYear(int offset, int pageSize, int year) {
    Query query = new Query().addCriteria(Criteria.where("meta.year").is(year));
    query.with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
        .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
    return masterMongoTemplate.find(query, News.class);
  }

  public List<News> getDataByCategoryOrTagOrSlug(int offset, int pageSize, String type,
      String value) {
    log.info("Get news by category or tag or slug : {}", value);
    List<News> returningList = new ArrayList<>();
    Query query = new Query();
    if (type.equalsIgnoreCase("slug")) {
      query.addCriteria(Criteria.where("meta.slug").is(value));
      query.with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
              .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
      returningList.addAll(masterMongoTemplate.find(query, News.class));
    } else {
      query.addCriteria(
          Criteria.where("meta.tags").elemMatch(Criteria.where("tagName").regex(value, "i")));
      query.with(Sort.by(Sort.Direction.DESC, "meta.publishingDate"))
              .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
      returningList.addAll(masterMongoTemplate.find(query, News.class));
    }
    return returningList;
  }

  public List<News> getDataByFilter(int offset, int pageSize, String filter, String filterValue) {
    switch (filter) {
      case "year":
        return getDataByYear(offset, pageSize, Integer.parseInt(filterValue));
      case "tag":
      case "slug":
        return getDataByCategoryOrTagOrSlug(offset, pageSize, filter, filterValue);
    }
    return new ArrayList<>();
  }

  public News deleteNews(String newsId) {
    Query query = new Query().addCriteria(Criteria.where("_id").is(newsId));
    return masterMongoTemplate.findAndRemove(query, News.class);
  }

  public Pagination getPaginationForFilter(int offset, int pageSize, int size) {
    Pagination pagination = new Pagination();
    pagination.setOffset(offset);
    pagination.setPageSize(pageSize);
    pagination.setTotal((long) size);
    return pagination;
  }

  public Pagination getPagination(int offset, int pageSize) {
    Pagination pagination = new Pagination();
    pagination.setOffset(offset);
    pagination.setPageSize(pageSize);
    pagination.setTotal(masterMongoTemplate.count(new Query(), News.class));
    return pagination;
  }
}
