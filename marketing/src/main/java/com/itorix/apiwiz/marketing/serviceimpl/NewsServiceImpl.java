package com.itorix.apiwiz.marketing.serviceimpl;

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

    @Override
    public ResponseEntity<?> createNews(String interactionid, String jsessionid, News news) throws Exception {
        News obj = newsDao.createNews(news);
        if(obj!=null){
            return new ResponseEntity<>(obj, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @Override
    public ResponseEntity<?> updateNews(String interactionid, String jsessionid, News news, String newsId) throws Exception {
        return newsDao.updateNews(news,newsId);
    }

    @Override
    public ResponseEntity<?> changeStatus(String interactionid, String jsessionid, String newsId, NewsStatus status) throws Exception {
            if (status.name().equals("DRAFT")) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return newsDao.changeStatus(newsId, status);
    }

    @Override
    public ResponseEntity<?> fetchAllNews(String interactionid, String jsessionid, String apikey, int offset, int pageSize) throws Exception {
        PaginatedResponse paginatedResponse = new PaginatedResponse();
        paginatedResponse.setPagination(newsDao.getPagination(offset,pageSize));
        paginatedResponse.setData(newsDao.getAllNews());
        return new ResponseEntity<>(paginatedResponse,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getDataByFilter(String interactionid, String jsessionid, String apikey, int offset, int pageSize, String filter, String filterValue) throws Exception {
        List<News> paginatedData = newsDao.getDataByFilter(filter,filterValue);
        PaginatedResponse response = new PaginatedResponse();
        response.setPagination(newsDao.getPaginationForFilter(offset,pageSize,paginatedData.size()));
        response.setData(paginatedData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteNews(String interactionid, String jsessionid, String newsId) throws Exception {
        if (newsDao.deleteNews(newsId) != null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
