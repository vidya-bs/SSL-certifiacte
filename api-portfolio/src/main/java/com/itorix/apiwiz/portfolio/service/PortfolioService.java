package com.itorix.apiwiz.portfolio.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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

import com.itorix.apiwiz.portfolio.model.PortfolioRequest;
import com.itorix.apiwiz.portfolio.model.db.Metadata;
import com.itorix.apiwiz.portfolio.model.db.Portfolio;
import com.itorix.apiwiz.portfolio.model.db.ProductRequest;
import com.itorix.apiwiz.portfolio.model.db.Products;
import com.itorix.apiwiz.portfolio.model.db.Projects;
import com.itorix.apiwiz.portfolio.model.db.ServiceRegistry;
import com.itorix.apiwiz.portfolio.model.db.proxy.Pipelines;
import com.itorix.apiwiz.portfolio.model.db.proxy.Proxies;

@CrossOrigin
@RestController
public interface PortfolioService {

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.POST }, value = "/v1/portfolios", consumes = { "application/json" }, produces = {
	"application/json" })
	public ResponseEntity<Object> createPortfolio(@RequestBody PortfolioRequest portfolioRequest,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}", consumes = {
			"application/json" })
	public ResponseEntity<Object> updatePortfolio(@RequestBody PortfolioRequest portfolioRequest,
			@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.PUT }, value = "v1/portfolios/{portfolioId}/image", consumes = {
			"multipart/form-data" }, produces = { "application/json" })
	public ResponseEntity<Object> createOrUpdatePortfolioImages(
			@RequestPart(value = "portfolioImage", required = true) MultipartFile image,
			@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}", produces = {
			"application/json" })
	public ResponseEntity<Object> getPortfolios(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "expand", required = false) boolean expand,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/portfolios/{portfolioId}", produces = {
			"application/json" })
	public ResponseEntity<Object> deletePortfolio(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios", produces = { "application/json" })
	public ResponseEntity<Object> getListOfPortfolio(@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize" , defaultValue= "10" ) int pageSize) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/list", produces = { "application/json" })
	public ResponseEntity<Object> getListOfPortfolioList(
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.PUT }, value = "/v1/portfolios/{id}/meta-data", consumes = {
			"application/json" })
	public ResponseEntity<Object> createOrUpdatePortfolioMetadata(@RequestBody List<Metadata> metadata,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.PUT }, value = "/v1/portfolios/{id}/teams", consumes = {
			"application/json" })
	public ResponseEntity<Object> createOrUpdatePortfolioTeam(@RequestBody Portfolio teams,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.POST }, value = "v1/portfolios/{id}/documents", consumes = {
			"multipart/form-data" }, produces = { "application/json" })
	public ResponseEntity<Object> createPortfolioDocument(@RequestParam Map<String, Object> requestParams,
			@RequestPart(value = "document", required = false) MultipartFile document,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "v1/portfolios/{portfolioId}/documents/{documentId}", consumes = {
					"multipart/form-data" })
	public ResponseEntity<Object> updatePortfolioDocument(
			@RequestPart(value = "documentSummary", required = false) String documentSummary,
			@RequestPart(value = "document", required = false) MultipartFile document,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "documentId") String documentId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/documents", produces = {
			"application/json" })
	public ResponseEntity<Object> getPortfolioDocuments(@PathVariable(value = "portfolioId") String portFolioId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.DELETE }, value = "v1/portfolios/{portfolioId}/documents/{documentId}")
	public ResponseEntity<Object> deletePortfolioDocument(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "documentId") String documentId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.POST }, value = "/v1/portfolios/{id}/products", consumes = {
			"application/json" }, produces = {"application/json" })
	public ResponseEntity<Object> createProducts(@RequestBody ProductRequest products,
			@PathVariable(value = "id") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/products/{productId}/image", consumes = {
					"multipart/form-data" })
	public ResponseEntity<Object> uploadProductImage(
			@RequestPart(value = "productImage", required = true) MultipartFile image,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/products/{productId}", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateProduct(@RequestBody Products productsUpdateRequest,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.DELETE }, value = "/v1/portfolios/{portfolioId}/products/{productId}")
	public ResponseEntity<Object> deleteProduct(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/products", produces = {
			"application/json" })
	public ResponseEntity<Object> getProductNames(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset, @RequestHeader(value = "JSESSIONID") String jsessionid, @RequestParam(value = "pagesize", defaultValue= "10" ) int pageSize)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/products/{productId}", produces = {
			"application/json" })
	public ResponseEntity<Object> getProductDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/products/{productId}/metadata", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateProductMetadata(@RequestBody List<Metadata> metadatas,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/products/{productId}/services", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateProductServices(@RequestBody Products product,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "productId") String productId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.POST }, value = "/v1/portfolios/{portfolioId}/service-registry", consumes = {
					"application/json" }, produces = {"application/json" })
	public ResponseEntity<Object> createApiRegistry(@RequestBody ServiceRegistry serviceRegistry,
			@PathVariable(value = "portfolioId") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "v1/portfolios/{portfolioId}/service-registry/{serviceRegistryId}", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateApiRegistry(@RequestBody ServiceRegistry serviceRegistry,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "serviceRegistryId") String servRegistryId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/service-registry", produces = {
			"application/json" })
	public ResponseEntity<Object> getApiRegistryList(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset, @RequestHeader(value = "JSESSIONID") String jsessionid , @RequestParam(value = "pagesize", defaultValue= "10" ) int pageSize)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/portfolios/{portfolioId}/service-registry/{serviceRegistryId}", produces = {
			"application/json" })
	public ResponseEntity<Object> deleteApiRegistryDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "serviceRegistryId") String servRegistryId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/service-registry/{serviceRegistryId}", produces = {
			"application/json" })
	public ResponseEntity<Object> getApiRegistryDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "serviceRegistryId") String servRegistryId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = { RequestMethod.POST }, value = "/v1/portfolios/{portfolioId}/projects", consumes = {
			"application/json" }, produces = {"application/json" })
	public ResponseEntity<Object> createProjects(@RequestBody Projects serviceRegistry,
			@PathVariable(value = "portfolioId") String id, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "v1/portfolios/{portfolioId}/projects/{projectId}", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateProject(@RequestBody Projects serviceRegistry,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/projects", produces = {
			"application/json" })
	public ResponseEntity<Object> getprojectsList(@PathVariable(value = "portfolioId") String portfolioId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset, @RequestHeader(value = "JSESSIONID") String jsessionid,@RequestParam(value = "pagesize", defaultValue= "10" ) int pageSize)
			throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/{portfolioId}/projects/{projectId}", produces = {
			"application/json" })
	public ResponseEntity<Object> getProjectDetails(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/portfolios/{portfolioId}/projects/{projectId}", produces = {
			"application/json" })
	public ResponseEntity<Object> deleteProject(@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.POST }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies", consumes = {
					"application/json" }, produces = {"application/json" })
	public ResponseEntity<Object> createProxy(@RequestBody Proxies proxy, @PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateProxy(@RequestBody Proxies proxy, @PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.GET }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies", produces = {
					"application/json" })
	public ResponseEntity<Object> getProxyList(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestHeader(value = "JSESSIONID") String jsessionid,@RequestParam(value = "pagesize", defaultValue= "10" ) int pageSize) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.GET }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}", produces = {
					"application/json" })
	public ResponseEntity<Object> getProxyDetail(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.DELETE }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}")
	public ResponseEntity<Object> deleteProxyDetail(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "poxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	// @RequestMapping(method = {RequestMethod.PUT} , value =
	// "/v1/portfolios/{portfolioId}/service-registry", consumes = {
	// "multipart/form-data" })
	// public ResponseEntity<Object> updateProductServices(
	// @RequestBody List<ProductServices> products,
	// @PathVariable(value = "portfolioId") String portfolioId,
	// @RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.POST }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}/pipelines", consumes = {
					"application/json" }, produces = { "application/json" })
	public ResponseEntity<Object> createPipeline(@RequestBody Pipelines pipeline,
			@PathVariable(value = "portfolioId") String id, @PathVariable(value = "projectId") String projectId,
			@PathVariable(value = "proxyId") String proxyId, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}/pipelines/{pipelineId}", consumes = {
					"application/json" })
	public ResponseEntity<Object> updateProxyPipeline(@RequestBody Pipelines pipeline,
			@PathVariable(value = "portfolioId") String id, @PathVariable(value = "projectId") String projectId,
			@PathVariable(value = "proxyId") String proxyId, @PathVariable(value = "pipelineId") String pipelineId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.GET }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}/pipelines", produces = {
					"application/json" })
	public ResponseEntity<Object> getPipeline(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.DELETE }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/proxies/{proxyId}/pipelines/{pipelineId}")
	public ResponseEntity<Object> deletePipeline(@PathVariable(value = "portfolioId") String id,
			@PathVariable(value = "projectId") String projectId, @PathVariable(value = "proxyId") String proxyId,
			@PathVariable(value = "pipelineId") String pipelineId,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@PreAuthorize("hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = RequestMethod.GET, value = "/v1/portfolios/search")
	public ResponseEntity<Object> search(
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value = "name") String name, @RequestParam(value = "limit") int limit) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN', 'PROJECT-ADMIN', 'ANALYST') and hasAnyAuthority('TEAM','ENTERPRISE')" )
	@RequestMapping(method = {
			RequestMethod.PUT }, value = "/v1/portfolios/{portfolioId}/projects/{projectId}/upload-design-artifacts", consumes = {
					"multipart/form-data" })
	public ResponseEntity<Object> uploadDesignArtifact(
			@RequestPart(value = "document", required = true) MultipartFile document,
			@RequestPart(value = "documentName", required = true) String documentName,
			@PathVariable(value = "portfolioId") String portfolioId,
			@PathVariable(value = "projectId") String projectId,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;
}
