package com.itorix.apiwiz.design.studio.serviceimpl;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.design.studio.dao.AsyncApiDao;
import com.itorix.apiwiz.design.studio.model.AsyncApi;
import com.itorix.apiwiz.design.studio.model.AsyncapiImport;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import com.itorix.apiwiz.design.studio.service.AsyncApiService;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
public class AsyncApiServiceImpl implements AsyncApiService {

	@Autowired
	AsyncApiDao asyncApiDao;

	@Autowired
	RSAEncryption rsaEncryption;

	@Override
	public ResponseEntity<?> createAsyncApi(String interactionid,  String jsessionid, String name, String asyncapi) throws Exception {
		//TODO check if name already exists
		AsyncApi asyncApiObj = new AsyncApi();
		JSONObject jsonObject = new JSONObject(asyncapi);
		JSONObject info = (JSONObject) jsonObject.get("info");
		asyncApiObj.setName(info.get("title").toString());
		asyncApiObj.setAsyncApi(asyncapi);
		asyncApiDao.createAsyncApiOrPushToDesignStudio(asyncApiObj,jsessionid);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateAsyncApi(String interactionid, String jsessionid,
			String id, String asyncapi) throws Exception {
		asyncApiDao.updateAsyncApi(id,asyncapi,jsessionid);
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
		asyncApiDao.deleteAsyncApi(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> createAsyncApiRevision(String interactionid, String jsessionid,
			String asyncId, String asyncapi) throws Exception {
		asyncApiDao.createAsyncApiRevision(jsessionid,asyncId,asyncapi);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateAsyncApiRevision(String interactionid, String jsessionid,
			String asyncId, int revision, AsyncApi asyncapi) throws Exception {
		asyncApiDao.updateAsyncApiRevision(jsessionid,asyncId,asyncapi,revision);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getRevisionOfAsyncApi(String interactionid, String jsessionid,
												   String asyncapiId, int revison) throws Exception {
		AsyncApi asyncApi = asyncApiDao.getAsyncApiRevision(asyncapiId,revison);
		if(asyncApi==null)
			throw new ItorixException(ErrorCodes.errorMessage.get("AsyncApi-1013"),"AsyncApi-1013");
		return new ResponseEntity<>(asyncApi,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteRevisionOfAsyncApi(String interactionid, String jsessionid,
			String asyncapiId, int revison) throws Exception {
		asyncApiDao.deleteAsyncApiRevision(asyncapiId,revison);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> updateStatusAsyncApiRevision(String interactionid, String jsessionid,
			String asyncId, int revision, StatusHistory statusHistory) throws Exception {
		asyncApiDao.updateStatus(jsessionid,asyncId,statusHistory,revision);
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
