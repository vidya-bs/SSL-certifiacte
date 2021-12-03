package com.itorix.consentserver.serviceimpl;

import com.itorix.consentserver.common.model.Consent;
import com.itorix.consentserver.common.model.ItorixException;
import com.itorix.consentserver.service.ConsentService;
import com.itorix.consentserver.dao.ConsentServerDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@Component
@Slf4j
public class ConsentController implements ConsentService {

    @Autowired
    ConsentServerDao consentServerDao;

    @Override
    public ResponseEntity<?> getScopeCategories() throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getAllScopeCategory(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getScopeCategoryNames() throws ItorixException, ItorixException {
        return new ResponseEntity<>(consentServerDao.getScopeCategoryNames(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getScopeCategoryByName(String categoryName) throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getScopeCategoryByName(categoryName), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> getScopeCategoryColumns() throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getScopeCategoryColumns(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createConsent(Consent consent) throws ItorixException {
        consentServerDao.createConsent(consent);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<?> getConsentByPrimaryKey(String userId) throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getConsentByPrimaryKey(userId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> revokeConsent(String consentId) throws ItorixException {
        consentServerDao.revokeConsent(consentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getConsentStatus(String userId) throws ItorixException {
        return new ResponseEntity<>(consentServerDao.getConsentStatus(userId), HttpStatus.OK);
    }



}
