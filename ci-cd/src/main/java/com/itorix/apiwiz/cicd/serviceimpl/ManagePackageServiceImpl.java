package com.itorix.apiwiz.cicd.serviceimpl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.cicd.beans.Package;
import com.itorix.apiwiz.cicd.beans.PackageReviewComents;
import com.itorix.apiwiz.cicd.dao.PackageDao;
import com.itorix.apiwiz.cicd.service.ManagePackageService;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
@Slf4j
@CrossOrigin
@RestController
public class ManagePackageServiceImpl implements ManagePackageService {

	private static final Logger logger = LoggerFactory.getLogger(ManagePackageServiceImpl.class);

	@Autowired
	private IdentityManagementDao commonServices;

	@Autowired
	private PackageDao packageDao;

	@Override
	public ResponseEntity<?> createPackage(@RequestParam(value = "action", required = false) String action,
			@RequestBody Package packageRequest, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		try {
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			packageRequest.setPackageId(RandomStringUtils.randomAlphanumeric(64));
			packageRequest.setState("NEW");
			packageRequest = packageDao.savePackage(packageRequest, user);
			HttpHeaders headers = new HttpHeaders();
			headers.add("x-packageid", packageRequest.getPackageId());
			headers.add("Access-Control-Expose-Headers", "x-packageid");
			return new ResponseEntity<>(headers, HttpStatus.CREATED);
		} catch (ItorixException e) {
			log.error("Exception occurred", e);
			return new ResponseEntity<>(new ErrorObj(e.getMessage(), e.errorCode), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception ex) {
			return new ResponseEntity<>(new ErrorObj("Error while creating Package", "Package-1000"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> updatePackage(@RequestBody Package packageRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		try {
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			packageDao.editPackage(packageRequest, user);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (ItorixException e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "Package-400"), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorObj("Server Error", "Package-500"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> deletePackage(@PathVariable("reqestId") String reqestId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		try {
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			packageDao.deletePackage(reqestId, user);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (ItorixException e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "Package-400"), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorObj("Server Error", "Package-500"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// @Override
	// public ResponseEntity<?>
	// getPackageProjectData(@RequestParam("projectname") String projectname,
	// @RequestHeader(value = "JSESSIONID") String jsessionId,
	// @RequestHeader(value = "interactionid", required = false) String
	// interactionid,
	// HttpServletRequest request) {
	// try {
	// return new
	// ResponseEntity<>(packageDao.getPackageProjectData(projectname),
	// HttpStatus.OK);
	// } catch (ItorixException e) {
	// return new ResponseEntity<>(new ErrorObj("invalid data", "CI-CD-CU400"),
	// HttpStatus.BAD_REQUEST);
	// }catch (Exception e) {
	// return new ResponseEntity<>(new ErrorObj("invalid data", "Package-500"),
	// HttpStatus.INTERNAL_SERVER_ERROR);
	// }
	// }

	@Override
	public ResponseEntity<?> getPackageProjectData(@RequestParam("proxyname") String proxyname,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		try {
			return new ResponseEntity<>(packageDao.getPackageProxyData(proxyname), HttpStatus.OK);
		} catch (ItorixException e) {
			// logger.error(e.get);
			log.error("Exception occurred", e);
			return new ResponseEntity<>(new ErrorObj("invalid data", "CI-CD-CU400"), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error("Exception occurred", e);
			return new ResponseEntity<>(new ErrorObj("invalid data", "Package-500"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> getPackage(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
			HttpServletRequest request) {
		try {
			return new ResponseEntity<>(packageDao.getPackages(offset, pageSize), HttpStatus.OK);
		} catch (ItorixException e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "CI-CD-CU400"), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "Package-500"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> getPackage(@PathVariable("reqestId") String reqestId,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		try {
			return new ResponseEntity<>(packageDao.getPackages(reqestId), HttpStatus.OK);
		} catch (ItorixException e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "CI-CD-CU400"), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "Package-500"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<?> approvePackage1(Package packageRequest, String jsessionId, String interactionid,
			HttpServletRequest request) {
		try {
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			packageRequest.setState("APPROVE");
			packageDao.approvePackage(packageRequest, user);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (ItorixException e) {
			return new ResponseEntity<>(new ErrorObj("invalid data", "Package-400"), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorObj("Server Error", "Package-500"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> approvePackage(@PathVariable("packageId") String packageId,
			@PathVariable("approveAction") String approveAction, @RequestBody Package packageRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		try {
			User user = commonServices.getUserDetailsFromSessionID(jsessionId);
			Package packagedb = (Package) packageDao.getPackages(packageId);
			if (packagedb != null) {
				packagedb.setComments(packageRequest.getComments());
				if (!StringUtils.isEmpty(approveAction)) {
					if (approveAction.equalsIgnoreCase("APPROVE")) {
						packagedb.setState("APPROVE");
						packageDao.approvePackage(packagedb, user);
						return new ResponseEntity<>(HttpStatus.NO_CONTENT);
					} else if (approveAction.equalsIgnoreCase("REJECT")) {
						packagedb.setState("REJECT");
						packageDao.rejectPackage(packagedb, user);
						return new ResponseEntity<>(HttpStatus.NO_CONTENT);
					} else if (approveAction.equalsIgnoreCase("REVIEW")) {
						packagedb.setState("REVIEW");
						packageDao.reviewPackage(packagedb, user);
						return new ResponseEntity<>(HttpStatus.NO_CONTENT);
					} else {
						return new ResponseEntity<>(new ErrorObj("Invalid approval type", "Packages-P400"),
								HttpStatus.BAD_REQUEST);
					}
				}
			} else {
				return new ResponseEntity<>(new ErrorObj("Invalid packageID", "Packages-P404"), HttpStatus.NOT_FOUND);
			}

		} catch (ItorixException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	@Override
	public ResponseEntity<Void> createReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("packageId") String packageId,
			@RequestBody PackageReviewComents packageReviewComments) throws Exception {
		packageReviewComments.setInteractionid(interactionid);
		packageReviewComments.setJsessionid(jsessionid);
		Package pkg = (Package) packageDao.getPackages(packageId);
		if (pkg == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("package-1001"), packageReviewComments.getPackageId()),
					"package-1001");
		}
		packageReviewComments.setPackageId(packageId);
		packageDao.createOrUpdateReviewComment(packageReviewComments);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> updateReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "packageId") String packageId,
			@PathVariable(value = "commentId") String commentId, @RequestBody PackageReviewComents packageReviewComents)
			throws Exception {
		packageReviewComents.setInteractionid(interactionid);
		packageReviewComents.setJsessionid(jsessionid);
		Package pkg = (Package) packageDao.getPackages(packageId);
		if (pkg == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("package-1001"), packageReviewComents.getPackageId()),
					"Swagger-1000");
		}
		packageReviewComents.setPackageId(packageId);
		packageReviewComents.setId(commentId);;
		packageDao.createOrUpdateReviewComment(packageReviewComents);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> reviewCommentReplay(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable(value = "packageId") String packageId,
			@PathVariable(value = "commentId") String commentId, @RequestBody PackageReviewComents packageReviewComents)
			throws Exception {
		packageReviewComents.setInteractionid(interactionid);
		packageReviewComents.setJsessionid(jsessionid);
		Package pkg = (Package) packageDao.getPackages(packageId);
		if (pkg == null) {
			throw new ItorixException(
					String.format(ErrorCodes.errorMessage.get("package-1001"), packageReviewComents.getPackageId()),
					"Swagger-1000");
		}
		packageReviewComents.setPackageId(packageId);
		packageReviewComents.setCommentId(commentId);
		packageDao.createOrUpdateReviewComment(packageReviewComents);
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> getReviewComment(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("packageId") String packageId)
			throws Exception {
		PackageReviewComents packageReviewComents = new PackageReviewComents();
		packageReviewComents.setInteractionid(interactionid);
		packageReviewComents.setJsessionid(jsessionid);
		packageReviewComents.setPackageId(packageId);;
		Object obj = packageDao.getReviewComment(packageReviewComents);
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> validateName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @PathVariable("packageName") String packageName)
			throws Exception {
		Map response = new HashMap();
		response.put("isValid", packageDao.validatePackage(packageName));
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPackageNames(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity<Object>(packageDao.getPackageNames(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> searchPackage(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception {
		return new ResponseEntity<Object>(packageDao.searchPackage(name, limit), HttpStatus.OK);
	}
}
