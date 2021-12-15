package com.itorix.consentserver.serviceimpl;

import com.itorix.consentserver.model.Consent;
import com.itorix.consentserver.model.ConsentResponse;
import com.itorix.consentserver.model.ItorixException;
import com.itorix.consentserver.service.ConsentService;
import com.itorix.consentserver.dao.ConsentServerDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@Component
@Slf4j
public class ConsentServiceImpl implements ConsentService {

    @Autowired
    ConsentServerDao consentServerDao;


    @Override
    public ResponseEntity<?> getScopeCategoryNames() throws ItorixException, ItorixException {
        return new ResponseEntity<>(consentServerDao.getScopeCategoryNames(), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> createConsent(Consent consent) throws ItorixException {
        consentServerDao.createConsent(consent);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> getConsents(Map<String, String> searchParams) throws ItorixException {
        ConsentResponse consentsBySearch = consentServerDao.getConsentsBySearch(getOffset(searchParams), getPageSize(searchParams), searchParams);
        return new ResponseEntity(consentsBySearch, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> getConsentById(String consentId) throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getConsentById(consentId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> revokeConsent(String consentId) throws ItorixException {
        consentServerDao.revokeConsent(consentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getConsentStatus(String consentId) throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getConsentStatus(consentId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateConsentScope(String consentId, List<String> scopes) throws ItorixException {
        consentServerDao.updateConsentScope(consentId, scopes);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> expireConsents(String tenantKey, String apiKey) throws ItorixException {
        consentServerDao.expireConsents();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private int getOffset(Map<String, String> searchParams) {
        int offset = 1;
        if(searchParams.containsKey("offset")) {
            offset =  Integer.valueOf(searchParams.get("offset"));
            searchParams.remove("offset");
        }
        return offset;
    }

    private int getPageSize(Map<String, String> searchParams) {
        int pageSize = 10;
        if(searchParams.containsKey("pageSize")) {
            pageSize =  Integer.valueOf(searchParams.get("pageSize"));
            searchParams.remove("pageSize");
        }
        return pageSize;
    }

}
