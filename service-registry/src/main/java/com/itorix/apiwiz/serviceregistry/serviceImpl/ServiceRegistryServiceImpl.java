package com.itorix.apiwiz.serviceregistry.serviceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.serviceregistry.dao.ServiceRegistryDao;
import com.itorix.apiwiz.serviceregistry.model.ServiceRegistryEntriesResponse;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistry;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryColumns;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryList;
import com.itorix.apiwiz.serviceregistry.model.documents.ServiceRegistryResponse;
import com.itorix.apiwiz.serviceregistry.service.ServiceRegistryService;

@CrossOrigin
@RestController
public class ServiceRegistryServiceImpl implements ServiceRegistryService {

	@Autowired
	ServiceRegistryDao serviceRegistryDao;

	@Override
	public ResponseEntity<ServiceRegistryColumns> getServiceRegistryColumns(
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws ItorixException {
		ServiceRegistryColumns serviceRegistryColumns = serviceRegistryDao.getServiceRegistryColumns();
		return new ResponseEntity<ServiceRegistryColumns>(serviceRegistryColumns, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createOrUpdateServRegColumns(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistryColumns registryColumns) throws ItorixException {
		serviceRegistryDao.createOrUpdateSRColumns(registryColumns);
		return new ResponseEntity<ServiceRegistryColumns>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistryList serviceRegistry) throws ItorixException {
		serviceRegistry = serviceRegistryDao.createServiceRegistry(serviceRegistry);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", "X-serviceregistryid");
		headers.add("X-serviceregistryid", serviceRegistry.getId());
		return new ResponseEntity<ServiceRegistryColumns>(headers, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> deleteServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException {
		serviceRegistryDao.deleteServiceRegistry(serviceRegistryId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> updateServiceRegistry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId,
			@RequestBody ServiceRegistryList registryResponser) throws ItorixException {
		serviceRegistryDao.updateServiceRegistry(serviceRegistryId, registryResponser);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<ServiceRegistryResponse> getServiceRegistry(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize)
			throws ItorixException {
		ServiceRegistryResponse serviceRegistryList = serviceRegistryDao.getServiceaRegistry(offset, pageSize);
		return new ResponseEntity<>(serviceRegistryList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ServiceRegistryEntriesResponse> getServiceRegistryEntries(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId) throws ItorixException {
		ServiceRegistryList serviceRegistryList = serviceRegistryDao.getServiceRegistryListById(serviceRegistryId);
		List<ServiceRegistry> serviceRegistryEntries = serviceRegistryDao.getServiceRegistryEntries(serviceRegistryId);
		if (!serviceRegistryEntries.isEmpty()) {
			List<Map<String, String>> data = new ArrayList<>();
			for (ServiceRegistry serviceRegistry : serviceRegistryEntries) {
				Map<String, String> dataMap = new HashMap<>();
				dataMap.put("rowId", serviceRegistry.getId().toHexString());
				dataMap.putAll(serviceRegistry.getData());
				data.add(dataMap);
			}
			return new ResponseEntity<>(
					new ServiceRegistryEntriesResponse(serviceRegistryList.getName(), serviceRegistryId, data),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId, @RequestBody ServiceRegistry serviceRegistry)
			throws ItorixException {
		ObjectId rowId = serviceRegistryDao.createServiceRegistryEntry(serviceRegistryId, serviceRegistry.getData())
				.getId();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", "X-rowid");
		headers.add("X-rowid", rowId.toString());
		return new ResponseEntity<>(headers, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody ServiceRegistry serviceRegistry, @PathVariable("service-registry-id") String serviceRegistryId,
			@PathVariable("row-id") String rowId) throws ItorixException {
		serviceRegistryDao.updateServiceRegistryEntry(serviceRegistryId, rowId, serviceRegistry.getData());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> deleteServiceRegistryEntry(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("service-registry-id") String serviceRegistryId, @PathVariable("row-id") String rowId)
			throws ItorixException {
		serviceRegistryDao.deleteServiceRegistryEntry(rowId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> swaggerSearch(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam("name") String name,
			@RequestParam("limit") int limit) throws ItorixException {
		Object response = null;
		response = serviceRegistryDao.search(name, limit);
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}
}
