package com.itorix.apiwiz.consent.management.serviceImpl;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.consent.management.dao.ConsentManagementDao;
import com.itorix.apiwiz.consent.management.model.Consent;
import com.itorix.apiwiz.consent.management.model.ScopeCategory;
import com.itorix.apiwiz.consent.management.model.ScopeCategoryColumns;
import com.itorix.apiwiz.consent.management.service.ConsentManagementService;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class ConsentManagementServiceImpl implements ConsentManagementService {

	@Autowired
	ConsentManagementDao consentManagementDao;

	@Override
	public ResponseEntity<?> createScopeCategory(String jsessionid, ScopeCategory scopeCategory)
			throws ItorixException {
		consentManagementDao.save(scopeCategory);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> updateScopeCategory(String jsessionid, ScopeCategory scopeCategory)
			throws ItorixException {
		consentManagementDao.update(scopeCategory);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> getScopeCategoryNames(String distinctBy) throws ItorixException {
		if (distinctBy == null) {
			return new ResponseEntity<>(consentManagementDao.getAllScopeCategory(), HttpStatus.OK);
		}
		return new ResponseEntity<>(consentManagementDao.getScopeCategoryNames(distinctBy), HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> getScopeCategoryByName(String jsessionid, String name) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getScopeCategoryByName(name), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteScopeCategory(String jsessionid, String name) throws ItorixException {
		consentManagementDao.deleteScopeCategory(name);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createOrUpdateScopeCategoryColumns(String jsessionid,
			ScopeCategoryColumns scopeCategoryColumns) throws ItorixException {
		consentManagementDao.createOrUpdateScopeCategoryColumns(scopeCategoryColumns);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> getScopeCategoryColumns(String jsessionid) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getScopeCategoryColumns(), HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> createConsent(Consent consent) throws ItorixException {
		consentManagementDao.createConsent(consent);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> getConsentsOverview(String jsessionid, int offset, int pageSize, String consentStatus,
			String category) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getConsentsOverview(offset, pageSize, consentStatus, category),
				HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> getConsentByPrimaryKey(String userId) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getConsentByPrimaryKey(userId), HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> revokeConsent(String consentId) throws ItorixException {
		consentManagementDao.revokeConsent(consentId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@UnSecure
	@Override
	public ResponseEntity<?> getConsentStatus(String userId) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getConsentStatus(userId), HttpStatus.OK);
	}

}
