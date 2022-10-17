package com.itorix.apiwiz.marketing.serviceimpl;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.common.PaginatedResponse;
import com.itorix.apiwiz.marketing.dao.NewsDao;
import com.itorix.apiwiz.marketing.news.model.News;
import com.itorix.apiwiz.marketing.news.model.NewsStatus;
import com.itorix.apiwiz.marketing.pressrelease.model.PressReleaseStatus;
import com.itorix.apiwiz.marketing.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@Slf4j
public class NewsServiceImpl implements NewsService {

    @Autowired
    NewsDao newsDao;

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> createNews(String apikey,News news) throws Exception {
        News obj = newsDao.createNews(news);
        if(obj!=null){
            return new ResponseEntity<>(obj, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> updateNews(String apikey,News news, String newsId) throws Exception {
        return newsDao.updateNews(news,newsId);
    }

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> changeStatus(String apikey,String newsId, NewsStatus status) throws Exception {
            if (status.name().equals("DRAFT")) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return newsDao.changeStatus(newsId, status);
    }

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> fetchAllNews(String apikey,int offset, int pageSize) throws Exception {
        PaginatedResponse paginatedResponse = new PaginatedResponse();
        paginatedResponse.setPagination(newsDao.getPagination(offset,pageSize));
        paginatedResponse.setData(newsDao.getAllNews());
        return new ResponseEntity<>(paginatedResponse,HttpStatus.OK);
    }

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> getDataByFilter(String apikey,int offset, int pageSize, String filter, String filterValue) throws Exception {
        List<News> paginatedData = newsDao.getDataByFilter(filter,filterValue);
        PaginatedResponse response = new PaginatedResponse();
        response.setPagination(newsDao.getPaginationForFilter(offset,pageSize,paginatedData.size()));
        response.setData(paginatedData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> deleteNews(String apikey,String newsId) throws Exception {
        if (newsDao.deleteNews(newsId) != null)
            return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
