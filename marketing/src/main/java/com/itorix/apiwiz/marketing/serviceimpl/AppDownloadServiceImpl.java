package com.itorix.apiwiz.marketing.serviceimpl;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.dao.AppDownloadDao;
import com.itorix.apiwiz.marketing.downloads.model.AppDownloadModel;
import com.itorix.apiwiz.marketing.service.AppDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class AppDownloadServiceImpl implements AppDownloadService {
    @Autowired
    AppDownloadDao appDownloadDao;

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> postDownload(String interactionid, String jsessionid, String apikey, AppDownloadModel appDownloadModel) throws Exception {
        return appDownloadDao.postDownload(appDownloadModel);
    }

    @UnSecure(ignoreValidation = true)
    @Override
    public ResponseEntity<?> getDownloads(String interactionid, String jsessionid, String apikey) throws Exception {
        return appDownloadDao.getDownloads();
    }
}
