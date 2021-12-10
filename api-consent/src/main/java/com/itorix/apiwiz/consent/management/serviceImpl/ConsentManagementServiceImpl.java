package com.itorix.apiwiz.consent.management.serviceImpl;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.consent.management.dao.ConsentManagementDao;
import com.itorix.apiwiz.consent.management.model.ScopeCategory;
import com.itorix.apiwiz.consent.management.model.ScopeCategoryColumns;
import com.itorix.apiwiz.consent.management.service.ConsentManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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


	@Override
	public ResponseEntity<?> deleteScopeCategory(String jsessionid, String name) throws ItorixException {
		consentManagementDao.deleteScopeCategory(name);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getScopeCategories(Map<String, String> searchParams) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getScopeCategories(getOffset(searchParams), getPageSize(searchParams), searchParams), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getScopeCategoryByName(String categoryName) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getScopeCategoryByName(categoryName), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createOrUpdateScopeCategoryColumns(String jsessionid,
			ScopeCategoryColumns scopeCategoryColumns) throws ItorixException {
		consentManagementDao.createOrUpdateScopeCategoryColumns(scopeCategoryColumns);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> getScopeCategoryColumns(String jsessionid) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getScopeCategoryColumns(), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<?> getConsentsOverview(String jsessionid, Map<String, String> searchParams) throws ItorixException {
		return new ResponseEntity<>(consentManagementDao.getConsentsOverview(getOffset(searchParams), getPageSize(searchParams), searchParams),
				HttpStatus.OK);
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
