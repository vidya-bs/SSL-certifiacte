package com.itorix.apiwiz.design.studio.serviceimpl;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.design.studio.dao.AsyncApiDao;
import com.itorix.apiwiz.design.studio.model.AsyncApi;
import com.itorix.apiwiz.design.studio.model.AsyncapiImport;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import com.itorix.apiwiz.design.studio.service.AsyncApiService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import com.itorix.apiwiz.design.studio.model.NotificationType;
import java.util.Arrays;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContext;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.design.studio.business.NotificationBusiness;


import com.itorix.apiwiz.design.studio.business.NotificationBusiness;

@CrossOrigin
@RestController
public class AsyncApiServiceImpl implements AsyncApiService {

	@Autowired
	AsyncApiDao asyncApiDao;

	@Autowired
	RSAEncryption rsaEncryption;


	@Autowired
	NotificationBusiness notificationBusiness;


	@Override
	public ResponseEntity<?> createAsyncApi(String interactionid,  String jsessionid, String name, String asyncapi) throws Exception {
		//TODO check if name already exists
		AsyncApi asyncApiObj = new AsyncApi();
		JSONObject jsonObject = new JSONObject(asyncapi);
		JSONObject info = (JSONObject) jsonObject.get("info");
		asyncApiObj.setName(info.get("title").toString());
		asyncApiObj.setAsyncApi(asyncapi);
		asyncApiDao.createAsyncApiOrPushToDesignStudio(asyncApiObj,jsessionid);
		UserSession userSession = ServiceRequestContextHolder.getContext().getUserSessionToken();
		asyncApiObj.setCreatedBy(userSession.getUserId());
		notificationBusiness.instantiateNotification(jsessionid, asyncApiObj.getName(), asyncApiObj.getCreatedBy(), "AsyncApi", "AsyncApi has been created for "  );
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateAsyncApi(String interactionid, String jsessionid,
			String id, String asyncapi) throws Exception {
		AsyncApi existingAsyncApi= asyncApiDao.getExistingAsyncById(id);
		asyncApiDao.updateAsyncApi(id,asyncapi,jsessionid);
		notificationBusiness.instantiateNotification(jsessionid, existingAsyncApi.getName(), existingAsyncApi.getCreatedBy(), "AsyncApi", "AsyncApi has been updated for "  );
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllAsyncApis(String interactionid, String jsessionid,Optional<String> name,
			Optional<String> sortBy,Optional<String> status,int offset,
			int pageSize) throws Exception {
		return new ResponseEntity<>(asyncApiDao.getAllAsyncApis(jsessionid,offset, pageSize,name,
				sortBy,status), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllNamesOfAsyncApi(String interactionid, String jsessionid)
			throws Exception {
		return new ResponseEntity<>(asyncApiDao.getAllAsyncApisNames(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllStatsOfAsyncApi(String interactionid, String jsessionid)
			throws Exception {
		return new ResponseEntity<>(asyncApiDao.getAllAsyncApisStats(jsessionid), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllRevisionsOfAsyncApi(String interactionid, String jsessionid,
													   String asyncapiId) throws Exception {
		return new ResponseEntity<>(asyncApiDao.getAllRevisions(asyncapiId),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteAsyncApi(String interactionid, String jsessionid, String id)
			throws Exception {
		AsyncApi existingAsyncApi = asyncApiDao.getExistingAsyncById(id);
		asyncApiDao.deleteAsyncApi(id);
		notificationBusiness.instantiateNotification(jsessionid, existingAsyncApi.getName(), existingAsyncApi.getCreatedBy(), "AsyncApi", "AsyncApi has been deleted "  );
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> createAsyncApiRevision(String interactionid, String jsessionid,
			String asyncId, String asyncapi) throws Exception {
		AsyncApi existingAsyncApi = asyncApiDao.getExistingAsyncById(asyncId);
		asyncApiDao.createAsyncApiRevision(jsessionid,asyncId,asyncapi);
		notificationBusiness.instantiateNotification(jsessionid, existingAsyncApi.getName(), existingAsyncApi.getCreatedBy(), "AsyncApi", "AsyncApi Revision has been created for "  );
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateAsyncApiRevision(String interactionid, String jsessionid,
			String asyncId, int revision, AsyncApi asyncapi) throws Exception {
		AsyncApi existingAsyncApi = asyncApiDao.getExistingAsyncByIdAndRevision(asyncId, revision);
		asyncApiDao.updateAsyncApiRevision(jsessionid,asyncId,asyncapi,revision);
		notificationBusiness.instantiateNotification(jsessionid, existingAsyncApi.getName(), existingAsyncApi.getCreatedBy(), "AsyncApi", "AsyncApi Revision has been updated for "  );
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getRevisionOfAsyncApi(String interactionid, String jsessionid,
												   String asyncapiId, int revison) throws Exception {
		AsyncApi asyncApi = asyncApiDao.getAsyncApiRevision(asyncapiId,revison);
		if(asyncApi==null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1013"), "AsyncApi-1013");
		}
		return new ResponseEntity<>(asyncApi,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteRevisionOfAsyncApi(String interactionid, String jsessionid,
			String asyncapiId, int revision) throws Exception {
		AsyncApi existingAsyncApi = asyncApiDao.getExistingAsyncByIdAndRevision(asyncapiId, revision);
		asyncApiDao.deleteAsyncApiRevision(asyncapiId,revision);
		notificationBusiness.instantiateNotification(jsessionid, existingAsyncApi.getName(), existingAsyncApi.getCreatedBy(), "AsyncApi", "AsyncApi Revision has been deleted for "  );
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> updateStatusAsyncApiRevision(String interactionid, String jsessionid,
			String asyncId, int revision, StatusHistory statusHistory) throws Exception {
		AsyncApi existingAsyncApi = asyncApiDao.getExistingAsyncByIdAndRevision(asyncId, revision);
		asyncApiDao.updateStatus(jsessionid,asyncId,statusHistory,revision);
		notificationBusiness.instantiateNotification(jsessionid, existingAsyncApi.getName(), existingAsyncApi.getCreatedBy(), "AsyncApi", "AsyncApi Revision has been updated for "  );
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> lockRevision(String interactionid, String jsessionid,
										  String asyncapiId, int revison, AsyncApi asyncApi) throws Exception {
		return new ResponseEntity<>(asyncApiDao.lockStatus(asyncapiId,revison,asyncApi),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> searchAsyncApi(String interactionid, String jsessionid, String name,
			int limit) throws Exception {
		return new ResponseEntity<>(asyncApiDao.search(name),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> importAsyncApis(String interactionid, String jsessionid,
			MultipartFile file, String type, String gitURI, String branch, String authType,
			String userName, String password, String personalToken) throws Exception {
		List<AsyncapiImport> asyncapiImports = asyncApiDao.importAsyncApis(file, type, gitURI, branch, authType,
				userName, password, personalToken,jsessionid);
		return new ResponseEntity<>(asyncapiImports, HttpStatus.OK);
	}
}
