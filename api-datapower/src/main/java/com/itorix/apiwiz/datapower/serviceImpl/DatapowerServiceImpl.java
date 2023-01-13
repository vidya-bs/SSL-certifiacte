package com.itorix.apiwiz.datapower.serviceImpl;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.datapower.dao.ExcelReader;
import com.itorix.apiwiz.datapower.dao.ProxyDAO;
import com.itorix.apiwiz.datapower.model.PromoteProxyRequest;
import com.itorix.apiwiz.datapower.model.ProxySearchRequest;
import com.itorix.apiwiz.datapower.model.proxy.GenerateProxyRequestDTO;
import com.itorix.apiwiz.datapower.model.proxy.Proxy;
import com.itorix.apiwiz.datapower.service.DatapowerService;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@CrossOrigin
@RestController
public class DatapowerServiceImpl implements DatapowerService {

	private Logger logger = LoggerFactory.getLogger(DatapowerServiceImpl.class);

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ExcelReader excelReader;

	@Autowired
	private ProxyDAO proxyDao;


	@Override
	public ResponseEntity<Object> createProxy(Proxy proxy, String jsessionid) throws Exception{
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		proxy.setCreatedBy(user.getFirstName() + " " + user.getLastName());
		proxy.setCts(System.currentTimeMillis());
		proxy.setMts(System.currentTimeMillis());
		proxy.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		String id = proxyDao.createProxy(proxy);
		return new ResponseEntity<>("{\"id\": \"" + id + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updateProxy(Proxy proxy, String proxyId, String jsessionid) throws Exception{
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		proxy.setMts(System.currentTimeMillis());
		proxy.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		proxy.setId(proxyId);
		proxyDao.updateProxy(proxy);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getProxy(String proxyId, String jsessionid, int offset, int pageSize) throws Exception{
		if(proxyId != null) {
			return new ResponseEntity<>(proxyDao.getProxy(proxyId), HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(proxyDao.getListOfProxies(offset, pageSize), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> deleteProxy(String proxyId, String jsessionid) throws Exception{
		proxyDao.deleteProxy(proxyId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	private ResponseEntity<Object> generateDatapowerApigeeProxy(String proxyId,
			MultipartFile[] attachments,
			GenerateProxyRequestDTO requests, String jsessionid)
			throws Exception {
		return new ResponseEntity<>(
				proxyDao.generateApigeeProxy(proxyId, attachments, requests, jsessionid),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> importDataExcel(@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		String targetFile = applicationProperties.getTempDir() + file.getOriginalFilename();
		file.transferTo(new File(targetFile));
		return new ResponseEntity<>(excelReader.readDataFromExcel(targetFile, jsessionid), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> generateDatapowerProxy(String proxyId, String jsessionid,
			MultipartFile[] attachments, GenerateProxyRequestDTO requests) throws Exception {
		if (attachments.length > 0 || requests!=null) {
			return generateDatapowerApigeeProxy(proxyId, attachments, requests, jsessionid);
		}
		return new ResponseEntity<>(proxyDao.generateProxy(proxyId, jsessionid), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> searchDatapowerProxy(ProxySearchRequest proxySearchRequest, String jsessionid)
			throws Exception {
		return new ResponseEntity<>(proxyDao.searchProxy(proxySearchRequest, jsessionid), HttpStatus.OK);
	}

	public ResponseEntity<Object> promoteDatapowerProxy(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody PromoteProxyRequest promoteProxyRequest) throws Exception {
		proxyDao.promoteProxy(promoteProxyRequest, jsessionid);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	


}
