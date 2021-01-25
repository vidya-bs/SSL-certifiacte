package com.itorix.apiwiz.portfolio.serviceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.portfolio.dao.PortfolioDao;
import com.itorix.apiwiz.portfolio.model.PortfolioRequest;
import com.itorix.apiwiz.portfolio.model.db.Metadata;
import com.itorix.apiwiz.portfolio.model.db.Portfolio;
import com.itorix.apiwiz.portfolio.model.db.PortfolioDocument;
import com.itorix.apiwiz.portfolio.model.db.PortfolioResponse;
import com.itorix.apiwiz.portfolio.model.db.ProductRequest;
import com.itorix.apiwiz.portfolio.model.db.Products;
import com.itorix.apiwiz.portfolio.model.db.Projects;
import com.itorix.apiwiz.portfolio.model.db.ServiceRegistry;
import com.itorix.apiwiz.portfolio.model.db.proxy.Pipelines;
import com.itorix.apiwiz.portfolio.model.db.proxy.Proxies;
import com.itorix.apiwiz.portfolio.service.PortfolioService;

@CrossOrigin
@RestController
public class PortfolioServiceImpl implements PortfolioService {

	private Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);

	@Autowired
	PortfolioDao portfolioDao;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Override
	public ResponseEntity<Object> createPortfolio(@RequestBody PortfolioRequest portfolioRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);

		portfolioRequest.setCreatedBy(user.getFirstName() + " " + user.getLastName());
		portfolioRequest.setCts(System.currentTimeMillis());
		portfolioRequest.setMts(System.currentTimeMillis());
		portfolioRequest.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		String id = portfolioDao.createPortfolio(portfolioRequest);
		return new ResponseEntity<>("{\"id\": \"" + id + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updatePortfolio(@RequestBody PortfolioRequest portfolioRequest,
			@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		portfolioRequest.setMts(System.currentTimeMillis());
		portfolioRequest.setModifiedBy(user.getFirstName() + " " + user.getLastName());

		portfolioDao.updatePortfolio(portfolioRequest, portfolioId,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> createOrUpdatePortfolioImages(
			@RequestPart(value = "portfolioImage", required = true) MultipartFile image,
			@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		if (image == null || image.getBytes() == null || image.getBytes().length == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-8"), "Portfolio-8");
		}

		byte[] imageBytes = image.getBytes();
		portfolioDao.updatePortfolioImage(portfolioId, imageBytes, jsessionid, image.getOriginalFilename());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getPortfolios(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "expand", required = false) boolean expand,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Portfolio portfolio = portfolioDao.getPortfolio(portfolioId, expand);
		return new ResponseEntity<>(portfolio, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getListOfPortfolio(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		PortfolioResponse portfolios = portfolioDao.getListOfPortfolios(offset, pageSize);
		return new ResponseEntity<>(portfolios, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Object> getListOfPortfolioList(
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception{
		return new ResponseEntity<>(portfolioDao.getListOfPortfolios(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deletePortfolio(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		portfolioDao.deletePortfolios(portfolioId,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> createOrUpdatePortfolioMetadata(@RequestBody List<Metadata> metadata,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.createOrUpdatePortfolioMetadata(id, metadata,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> createOrUpdatePortfolioTeam(@RequestBody Portfolio portfolio,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.createOrUpdatePortfolioTeam(id, portfolio.getTeams(),jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> createPortfolioDocument(@RequestParam Map<String, Object> requestParams,
			@RequestPart(value = "document", required = false) MultipartFile document,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {

		List<String> missingField = new ArrayList<>();
		if (!StringUtils.hasText((String) requestParams.get("documentName"))) {
			missingField.add("documentName");
		}

		if (!StringUtils.hasText((String) requestParams.get("documentSummary"))) {
			missingField.add("documentSummary");
		}

		if (document == null || document.getBytes() == null || document.getBytes().length == 0) {
			missingField.add("document");
		}

		if (!CollectionUtils.isEmpty(missingField)) {
			throw new ItorixException(
					(String.format(ErrorCodes.errorMessage.get("Portfolio-6"), String.join(",", missingField))),
					"Portfolio-6");
		}

		PortfolioDocument portfolioDocument = PortfolioDocument.builder()
				.documentName((String) requestParams.get("documentName"))
				.documentOwner((String) requestParams.get("documentOwner"))
				.documentOwnerEmail((String) requestParams.get("documentOwnerEmail"))
				.documentSummary((String) requestParams.get("documentSummary")).build();

		String location = null;
		String docId = portfolioDao.createPortfolioDocument(id, portfolioDocument,jsessionid);
		try {
			location = portfolioDao.updatePortfolioDocument(id, docId, document.getBytes(), jsessionid,
					document.getOriginalFilename());
		} catch (Exception e) {
			logger.error("exception when updating portfolio document", e);
			portfolioDao.deletePortfolioDocument(id, docId, jsessionid);
		}
		portfolioDao.updateDocumentLocation(id, docId, location,jsessionid);

		return new ResponseEntity<>("{\"id\": \"" + docId + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updatePortfolioDocument(
			@RequestPart(value = "documentSummary", required = false) String documentSummary,
			@RequestPart(value = "document", required = false) MultipartFile document,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "documentId") String documentId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		if (document == null || document.getBytes() == null || document.getBytes().length == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-5"), "Portfolio-5");
		}

		String updatePortfolioDocument = portfolioDao.updatePortfolioDocument(portfolioId, documentId,
				document.getBytes(), jsessionid, document.getOriginalFilename());
		PortfolioDocument portfolioDocument = PortfolioDocument.builder().documentSummary(documentSummary)
				.document(updatePortfolioDocument).build();
		portfolioDao.updatePortfolioDocument(portfolioId, documentId, portfolioDocument,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<Object> deletePortfolioDocument(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "documentId") String documentId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		portfolioDao.deletePortfolioDocument(portfolioId, documentId,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getPortfolioDocuments(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		List<PortfolioDocument> portfolioDocument = portfolioDao.getPortfolioDocuments(portfolioId);
		return new ResponseEntity<>(portfolioDocument, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createProducts(@RequestBody ProductRequest products,
			@PathVariable(value = "id") String portfolioId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		String id = portfolioDao.createProducts(portfolioId, products,jsessionid);
		return new ResponseEntity<>("{\"id\": \"" + id + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> uploadProductImage(
			@RequestPart(value = "productImage", required = true) MultipartFile image,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		byte[] bytes = image.getBytes();
		portfolioDao.uploadProductPortfolioImage(portfolioId, productId, bytes, jsessionid,
				image.getOriginalFilename());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> updateProduct(@RequestBody Products productsUpdateRequest,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.updateProduct(portfolioId, productId, productsUpdateRequest,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> deleteProduct(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {

		portfolioDao.deleteProduct(portfolioId, productId,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getProductNames(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		return new ResponseEntity<>(portfolioDao.getProductNames(portfolioId, offset, pageSize), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProductDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		Products Product = portfolioDao.getProductDetails(portfolioId, productId);
		return new ResponseEntity<>(Product, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateProductMetadata(@RequestBody List<Metadata> metadata,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.updateProductMetadata(portfolioId, productId, metadata,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> updateProductServices(@RequestBody Products product,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.updateProductServices(portfolioId, productId, product.getProductServices(),jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> createApiRegistry(@RequestBody ServiceRegistry serviceRegistry,
			@PathVariable(value = "portfolioId") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		String apiRegistryId = portfolioDao.createApiRegistry(id, serviceRegistry,jsessionid);
		return new ResponseEntity<>("{\"id\": \"" + apiRegistryId + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updateApiRegistry(@RequestBody ServiceRegistry serviceRegistry,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "serviceRegistryId") String servRegistryId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		portfolioDao.updateApiRegistry(portfolioId, servRegistryId, serviceRegistry,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@Override
	public ResponseEntity<Object> getApiRegistryList(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		return new ResponseEntity<>(portfolioDao.getApiRegistryList(portfolioId, offset, pageSize), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getApiRegistryDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "serviceRegistryId") String servRegistryId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		ServiceRegistry serviceRegistry = portfolioDao.getApiRegistryDetails(portfolioId, servRegistryId);
		return new ResponseEntity<>(serviceRegistry, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> deleteApiRegistryDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "serviceRegistryId") String servRegistryId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		portfolioDao.deleteApiRegistryDetails(portfolioId, servRegistryId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> createProjects(@RequestBody Projects project,
			@PathVariable(value = "portfolioId") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		String projectId = portfolioDao.createProjects(id, project,jsessionid);
		return new ResponseEntity<>("{\"id\": \"" + projectId + "\"}", HttpStatus.CREATED);

	}

	@Override
	public ResponseEntity<Object> updateProject(@RequestBody Projects project,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.updateProject(portfolioId, projectId, project,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getprojectsList(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		return new ResponseEntity<>(portfolioDao.getProjectsList(portfolioId, offset, pageSize), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProjectDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		Projects project = portfolioDao.getProjectDetails(portfolioId, projectId);
		return new ResponseEntity<>(project, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteProject(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {
		portfolioDao.deleteProject(portfolioId, projectId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createProxy(@RequestBody Proxies proxy,
			@PathVariable(value = "portfolioId") String id, @PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		String proxyId = portfolioDao.createProxy(id, projectId, proxy,jsessionid);
		return new ResponseEntity<>("{\"id\": \"" + proxyId + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updateProxy(@RequestBody Proxies proxy,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		portfolioDao.updateProxy(portfolioId, projectId, proxyId, proxy);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getProxyList(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "pagesize", defaultValue = "10") int pageSize) throws Exception {
		return new ResponseEntity<>(portfolioDao.getProxyList(portfolioId, projectId, offset, pageSize), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProxyDetail(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		Proxies proxy = portfolioDao.getProxyDetail(portfolioId, projectId, proxyId);
		return new ResponseEntity<>(proxy, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteProxyDetail(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		portfolioDao.deleteProxyDetail(portfolioId, projectId, proxyId,jsessionid);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> createPipeline(@RequestBody Pipelines pipeline,
			@PathVariable(value = "portfolioId") String id, @PathVariable(value = "projectId") String projectId,
			@PathVariable(value = "proxyId") String proxyId, @RequestHeader(value = "JSESSIONID") String jsessionid)
					throws Exception {

		String pipelenieId = portfolioDao.createPipeline(id, projectId, proxyId,jsessionid,pipeline);
		return new ResponseEntity<>("{\"id\": \"" + pipelenieId + "\"}", HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Object> updateProxyPipeline(@RequestBody Pipelines pipelines,
			@PathVariable(value = "portfolioId") String id, @PathVariable(value = "projectId") String projectId,
			@PathVariable(value = "proxyId") String proxyId, @PathVariable(value = "pipelineId") String pipelineId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		portfolioDao.updateProxyPipeline(id, projectId, proxyId, pipelineId, pipelines,jsessionid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}


	@Override
	public ResponseEntity<Object> getPipeline(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception{

		List<Pipelines> pipelines = portfolioDao.getPipeline(id, projectId, proxyId,jsessionid);
		return new ResponseEntity<>(pipelines, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> deletePipeline(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@PathVariable(value = "pipelineId") String pipelineId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception{
		portfolioDao.deletePipeline(id, projectId, proxyId,pipelineId,jsessionid);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> search(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "limit" ,defaultValue = "10") int limit) throws Exception{
		return new ResponseEntity<Object>(portfolioDao.search(name, limit), HttpStatus.OK);
	}

	public ResponseEntity<Object> uploadDesignArtifact(
			@RequestPart(value = "document", required = true) MultipartFile document,
			@RequestPart(value = "documentName", required = true) String documentName,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		if (document == null || document.getBytes() == null || document.getBytes().length == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-5"), "Portfolio-5");
		}

		byte[] documentBytes = document.getBytes();

		String documentLocation = portfolioDao.uploadDesignArtifact(portfolioId, projectId, documentBytes, jsessionid,
				document.getOriginalFilename());

		Map<String, String> map = new HashMap<>();
		map.put("documentName", documentName);
		map.put("documentLocation", documentLocation);

		return new ResponseEntity<>(map, HttpStatus.CREATED);
	}

}
