package com.itorix.apiwiz.portfolio.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.Organization;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectProxyResponse;
import com.itorix.apiwiz.common.model.proxystudio.ProxyPortfolio;
import com.itorix.apiwiz.common.model.proxystudio.Scm;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.portfolio.model.PipelineResponse;
import com.itorix.apiwiz.portfolio.model.PortfolioRequest;
import com.itorix.apiwiz.portfolio.model.PromoteProxyRequest;
import com.itorix.apiwiz.portfolio.model.db.*;
import com.itorix.apiwiz.portfolio.model.db.proxy.DesignArtifacts;
import com.itorix.apiwiz.portfolio.model.db.proxy.Pipelines;
import com.itorix.apiwiz.portfolio.model.db.proxy.Proxies;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PortfolioDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	@Autowired
	ApplicationProperties applicationProperties;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Autowired
	private S3Connection s3Connection;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	private IntegrationHelper integrationHelper;

	public String createPortfolio(PortfolioRequest portfolioRequest) throws ItorixException {
		Portfolio portfolio = new Portfolio();

		Query query = new Query().addCriteria(Criteria.where("name").is(portfolioRequest.getName()));

		if (mongoTemplate.count(query, Portfolio.class) != 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		BeanUtils.copyProperties(portfolioRequest, portfolio);

		mongoTemplate.save(portfolio);
		return portfolio.getId();
	}

	public void createOrUpdatePortfolioMetadata(String id, List<Metadata> metadata, String jsessionid)
			throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		Update update = new Update();
		update.set("metadata", metadata);

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
	}

	public void createOrUpdatePortfolioTeam(String id, List<String> teams, String jsessionid) throws ItorixException {

		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		Update update = new Update();
		update.set("teams", teams);

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
	}

	public String createPortfolioDocument(String id, PortfolioDocument document, String jsessionid)
			throws ItorixException {

		Query queryForCount = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("document").elemMatch(Criteria.where("documentName").is(document.getDocumentName()))));

		if (mongoTemplate.count(queryForCount, Portfolio.class) != 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		Update update = new Update();
		document.setId(new ObjectId().toString());
		document.setDocumentId(new ObjectId().toString());
		document.setRevision(1);
		update.push("document", document);

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
		return document.getId();
	}

	public String createPortfolioDocumentRevision(String id, PortfolioDocument document, String jsessionid)
			throws ItorixException {

		Query queryForCount = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("document").elemMatch(Criteria.where("documentId").is(document.getDocumentId()))));

		if (mongoTemplate.count(queryForCount, Portfolio.class) == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		Update update = new Update();
		document.setId(new ObjectId().toString());
		update.push("document", document);

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
		return document.getId();
	}

	public void updatePortfolioDocument(String portfolioId, String documentId, PortfolioDocument portfolioDocument,
			String jsessionid, Integer revision) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("document").elemMatch(Criteria.where("documentId").is(documentId)
						.andOperator(Criteria.where("revision").is(revision)))));
		Update update = new Update();
		boolean objectUpdate = false;
		if (StringUtils.hasText(portfolioDocument.getDocumentSummary())) {
			update.set("document.$.documentSummary", portfolioDocument.getDocumentSummary());
			objectUpdate = true;
		}

		if (StringUtils.hasText(portfolioDocument.getDocument())) {
			update.set("document.$.document", portfolioDocument.getDocument());
			objectUpdate = true;
		}
		if (!objectUpdate) {
			return;
		}

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1006"), "Portfolio-1006");
		}
	}

	public void updatePortfolioDocument(String portfolioId, String documentId, PortfolioDocument portfolioDocument,
			Integer revision, String jsessionid) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("document").elemMatch(Criteria.where("documentId").is(documentId))));
		query.addCriteria(Criteria.where("revision").is(revision));
		Update update = new Update();
		boolean objectUpdate = false;
		if (StringUtils.hasText(portfolioDocument.getDocumentSummary())) {
			update.set("document.$.documentSummary", portfolioDocument.getDocumentSummary());
			objectUpdate = true;
		}

		if (StringUtils.hasText(portfolioDocument.getDocument())) {
			update.set("document.$.document", portfolioDocument.getDocument());
			objectUpdate = true;
		}
		if (!objectUpdate) {
			return;
		}

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1006"), "Portfolio-1006");
		}
	}

	public void updateDocumentLocation(String portfolioId, String documentId, String location, String jsessionid)
			throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("document").elemMatch(Criteria.where("id").is(documentId))));
		Update update = new Update();
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		update.set("document.$.document", location);
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1006"), "Portfolio-1006");
		}
	}

	public void updatePortfolio(PortfolioRequest portfolioRequest, String portfolioId, String jsessionid)
			throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));

		Update update = new Update();

		update.set("summary", portfolioRequest.getSummary());
		update.set("description", portfolioRequest.getDescription());
		update.set("owner", portfolioRequest.getOwner());
		update.set("ownerEmail", portfolioRequest.getOwnerEmail());
		if (null != portfolioRequest.getTestsuites())
			update.set("testsuites", portfolioRequest.getTestsuites());
		if (null != portfolioRequest.getTestsuiteEnvironments())
			update.set("testsuiteEnvironments", portfolioRequest.getTestsuiteEnvironments());

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
	}

	private void updatePortfolioPicture(String portfolioId, String image, String jsessionid) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		Update update = new Update();
		update.set("portfolioImage", image);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		mongoTemplate.updateFirst(query, update, Portfolio.class);
	}

	public String updatePortfolioImage(String portfolioId, byte[] imageBytes, String jsession, String fileName)
			throws Exception {
		String downloadURI;
		Portfolio findById = mongoTemplate.findById(portfolioId, Portfolio.class);
		if (findById == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}

		String workspace = masterMongoTemplate.findById(jsession, UserSession.class).getWorkspaceId();
		if (!StringUtils.isEmpty(findById.getPortfolioImage())) {
			deleteFileJfrogFile(findById.getPortfolioImage().substring(findById.getPortfolioImage().indexOf(workspace)),jsession);
		}
		downloadURI = updateToJfrog(portfolioId + "/" + fileName, imageBytes, jsession);
		updatePortfolioPicture(portfolioId, downloadURI, jsession);
		return downloadURI;
	}

	public Portfolio getPortfolio(String portfolioId, boolean expand) throws ItorixException {
		Portfolio portfolio = null;
		if (expand) {
			portfolio = mongoTemplate.findById(portfolioId, Portfolio.class);
		} else {
			Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
			query.fields().include("name").include("summary").include("description").include("portfolioImage")
					.include("owner").include("ownerEmail").include("teams").include("metadata").include("cts")
					.include("createdBy").include("modifiedBy").include("mts").include("testsuites")
					.include("testsuiteEnvironments");
			List<Portfolio> portfolios = mongoTemplate.find(query, Portfolio.class);
			if (!CollectionUtils.isEmpty(portfolios)) {
				portfolio = portfolios.get(0);
			}
		}
		if (portfolio == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
		return portfolio;
	}

	public PortfolioResponse getListOfPortfolios(int offset, int pageSize) {

		Query query = new Query().with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0)
				.limit(pageSize);

		query.fields().include("name").include("summary").include("owner").include("portfolioImage").include("cts")
				.include("createdBy").include("modifiedBy").include("mts");;
		PortfolioResponse response = new PortfolioResponse();

		List<Portfolio> portfolios = mongoTemplate.find(new Query().with(Sort.by(Direction.DESC, "mts"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize), Portfolio.class);

		if (!CollectionUtils.isEmpty(portfolios)) {
			Long counter = mongoTemplate.count(new Query(), Portfolio.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(portfolios);
		}

		return response;
	}

	public List<Portfolio> getListOfPortfolios() {
		Query query = new Query().with(Sort.by(Direction.DESC, "_id"));
		query.fields().include("name").include("summary").include("owner").include("projects");
		List<Portfolio> portfolios = mongoTemplate.find(query, Portfolio.class);
		for (Portfolio portfolio : portfolios) {
			if (portfolio.getProjects() != null) {
				for (Projects project : portfolio.getProjects()) {
					project.setConsumers(null);
					project.setMetadata(null);
					project.setProducts(null);
					project.setProxies(null);
					project.setTeams(null);
				}
			}
		}
		return portfolios;
	}

	public void deletePortfolios(String portfolioId, String jsessionid) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		String workspace = masterMongoTemplate.findById(jsessionid, UserSession.class).getWorkspaceId();
		deleteFileJfrogFile("/" + workspace + "/portfolio/" + portfolioId,jsessionid);
		if (mongoTemplate.remove(query, Portfolio.class).getDeletedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
	}

	public List<PortfolioDocument> getPortfolioDocuments(String portfolioId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		query.fields().include("document");
		List<Portfolio> find = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(find)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
		return find.get(0).getDocument();
	}

	public List<PortfolioDocument> getPortfolioDocumentSummary(String portfolioId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		query.fields().include("document");
		List<Portfolio> find = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(find)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
		List<PortfolioDocument> summaryList = new ArrayList<>();
		try {
			List<PortfolioDocument> documents = find.get(0).getDocument();
			List<PortfolioDocument> uniqueValues = documents.stream()
					.filter(distinctByKey(PortfolioDocument::getDocumentId)).collect(Collectors.toList());
			for (PortfolioDocument document : uniqueValues) {
				List<PortfolioDocument> revList = new ArrayList<>();
				revList = getPortfolioDocumentRevisions(documents, document.getDocumentId());
				PortfolioDocument portfolioDocument = new PortfolioDocument();
				portfolioDocument.setDocumentName(document.getDocumentName());
				portfolioDocument.setRevisions(revList);
				summaryList.add(portfolioDocument);
			}
		} catch (Exception e) {
		}
		return summaryList;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	public List<PortfolioDocument> getPortfolioDocumentRevisions(List<PortfolioDocument> documents, String documentId)
			throws ItorixException {
		List<PortfolioDocument> doc = documents.stream().filter(d -> d.getDocumentId().equals(documentId))
				.collect(Collectors.toList());
		return doc;
	}

	public Integer getPortfolioDocumentMaxRevision(String portfolioId, String documentId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		query.fields().include("document");
		List<Portfolio> find = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(find)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}
		List<PortfolioDocument> documents = find.get(0).getDocument().stream()
				.filter(d -> d.getDocumentId().equals(documentId)).collect(Collectors.toList());
		return documents.stream().mapToInt(PortfolioDocument::getRevision).max().getAsInt();
	}

	public void deletePortfolioDocument(String portfolioId, String documentId, String jsessionid)
			throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("document").elemMatch(Criteria.where("id").is(documentId))));
		String workspace = masterMongoTemplate.findById(jsessionid, UserSession.class).getWorkspaceId();
		deleteFileJfrogFile("/" + workspace + "/portfolio/" + portfolioId + "/" + documentId,jsessionid);
		if (mongoTemplate.updateMulti(query,
				new Update().pull("document", new Query().addCriteria(Criteria.where("_id").is(documentId))),
				Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1006"), "Portfolio-1006");
		}
	}

	public String createProducts(String portfolioId, ProductRequest products, String jsessionid)
			throws ItorixException {
		Query queryForCount = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("name").is(products.getName()))));

		if (mongoTemplate.count(queryForCount, Portfolio.class) != 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		Products product = new Products();
		BeanUtils.copyProperties(products, product);
		product.setId(new ObjectId().toString());
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		Update update = new Update();
		update.push("products", product);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}

		return product.getId();
	}

	public void updateProduct(String portfolioId, String productId, Products productsUpdateRequest, String jsessionid)
			throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));

		// DBObject dbDoc = new BasicDBObject();
		// mongoTemplate.getConverter().write(productsUpdateRequest, dbDoc);
		Update update = new Update();

		update.set("products.$.summary", productsUpdateRequest.getSummary());
		update.set("products.$.description", productsUpdateRequest.getDescription());
		update.set("products.$.owner", productsUpdateRequest.getOwner());
		update.set("products.$.ownerEmail", productsUpdateRequest.getOwnerEmail());
		update.set("products.$.productStatus", productsUpdateRequest.getProductStatus());
		update.set("products.$.productAccess", productsUpdateRequest.getProductAccess());
		update.set("products.$.publishStatus", productsUpdateRequest.isPublishStatus());

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}
	}

	public void deleteProduct(String portfolioId, String productId, String jsessionid) throws ItorixException {

		String workspace = masterMongoTemplate.findById(jsessionid, UserSession.class).getWorkspaceId();
		deleteFileJfrogFile("/" + workspace + "/portfolio/" + portfolioId + "/" + productId,jsessionid);

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));

		if (mongoTemplate.updateMulti(query,
				new Update().pull("products", new Query().addCriteria(Criteria.where("_id").is(productId))),
				Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}
	}

	public List<Products> getProductNames(String portfolioId, int offset, int pageSize) throws ItorixException {

		Aggregation agg = Aggregation.newAggregation(Aggregation.match(Criteria.where("id").is(portfolioId)),
				Aggregation.project("products"), Aggregation.unwind("$products"),
				// Aggregation.skip(Long.valueOf(offset > 0 ? ((offset - 1) *
				// pageSize) : 0)),
				// Aggregation.limit(pageSize),
				Aggregation.project("products._id", "products.name", "products.summary", "products.productImage",
						"products.productStatus", "products.productAccess"));
		try {
			AggregationResults<Products> resultData = mongoTemplate.aggregate(agg, Portfolio.class, Products.class);
			if (resultData != null) {
				List<Products> project = resultData.getMappedResults();

				ProjectionOperation projectForCount = Aggregation.project("products.id")
						.and(ArrayOperators.arrayOf("products").length()).as("count");

				Aggregation aggForCount = Aggregation
						.newAggregation(Aggregation.match(Criteria.where("id").is(portfolioId)), projectForCount);

				AggregationResults<Map> result = mongoTemplate.aggregate(aggForCount, Portfolio.class, Map.class);
				Long count = 0L;
				if (!result.getMappedResults().isEmpty()) {
					count = Long.valueOf((int) result.getMappedResults().get(0).get("count"));
				}
				return project;
			} else {
				return new ArrayList<Products>();
			}
		} catch (Exception e) {
			return new ArrayList<Products>();
		}
	}

	private PortfolioResponse getPaginatedResponse(int offset, Long counter, Object data, int pageSize) {
		PortfolioResponse response = new PortfolioResponse();
		if (data != null) {
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(data);
		}
		return response;
	}

	public Products getProductDetails(String portfolioId, String productId) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));

		query.fields().include("products.$");

		List<Portfolio> find = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(find)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}
		return find.get(0).getProducts().get(0);
	}

	public void updateProductMetadata(String portfolioId, String productId, List<Metadata> metadatas, String jsessionid)
			throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));
		Update update = new Update();

		update.set("products.$.metadata", metadatas);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}
	}

	public void updateProductServices(String portfolioId, String productId, List<ProductServices> productServices,
			String jsessionid) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));
		Update update = new Update();
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		update.set("products.$.productServices", productServices);
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}
	}

	public String createApiRegistry(String id, ServiceRegistry serviceRegistry, String jsessionid)
			throws ItorixException {

		Query queryForCount = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("serviceRegistry").elemMatch(Criteria.where("name").is(serviceRegistry.getName()))));

		if (mongoTemplate.count(queryForCount, Portfolio.class) != 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		serviceRegistry.setId(new ObjectId().toString());
		Query query = new Query().addCriteria(Criteria.where("id").is(id));
		Update update = new Update();
		update.push("serviceRegistry", serviceRegistry);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}

		return serviceRegistry.getId();
	}

	public void updateApiRegistry(String portfolioId, String servRegistryId, ServiceRegistry serviceRegistry,
			String jsessionid) throws ItorixException {

		serviceRegistry.setId(servRegistryId);
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("serviceRegistry").elemMatch(Criteria.where("id").is(servRegistryId))));

		Update update = new Update();
		update.set("serviceRegistry.$", serviceRegistry);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1011"), "Portfolio-1011");
		}
	}

	public List<ServiceRegistry> getApiRegistryList(String portfolioId, int offset, int pageSize)
			throws ItorixException {

		Aggregation agg = Aggregation.newAggregation(Aggregation.match(Criteria.where("id").is(portfolioId)),
				Aggregation.project("serviceRegistry"), Aggregation.unwind("$serviceRegistry"),
				// Aggregation.skip(Long.valueOf(offset > 0 ? ((offset - 1) *
				// pageSize) : 0)),
				// Aggregation.limit(pageSize),
				Aggregation.project("serviceRegistry._id", "serviceRegistry.name", "serviceRegistry.summary",
						"serviceRegistry.path", "serviceRegistry.verb"));
		try {
			AggregationResults<ServiceRegistry> resultData = mongoTemplate.aggregate(agg, Portfolio.class,
					ServiceRegistry.class);
			if (resultData != null) {
				List<ServiceRegistry> project = resultData.getMappedResults();

				ProjectionOperation projectForCount = Aggregation.project("serviceRegistry.id")
						.and(ArrayOperators.arrayOf("serviceRegistry").length()).as("count");

				Aggregation aggForCount = Aggregation
						.newAggregation(Aggregation.match(Criteria.where("id").is(portfolioId)), projectForCount);

				AggregationResults<Map> result = mongoTemplate.aggregate(aggForCount, Portfolio.class, Map.class);
				Long count = 0L;
				if (!result.getMappedResults().isEmpty()) {
					count = Long.valueOf((int) result.getMappedResults().get(0).get("count"));
				}
				// return getPaginatedResponse(offset, count, project,
				// pageSize);
				return project;
			} else {
				return new ArrayList<ServiceRegistry>();
			}
		} catch (Exception e) {
			return new ArrayList<ServiceRegistry>();
		}
	}

	public ServiceRegistry getApiRegistryDetails(String portfolioId, String servRegistryId) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("serviceRegistry").elemMatch(Criteria.where("id").is(servRegistryId))));

		query.fields().include("serviceRegistry.$");

		List<Portfolio> find = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(find)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1011"), "Portfolio-1011");
		}
		return find.get(0).getServiceRegistry().get(0);
	}

	public String uploadProductPortfolioImage(String portfolioId, String productId, byte[] bytes, String jsessionid,
			String fileName) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));

		String downloadURI;

		if (mongoTemplate.count(query, Portfolio.class) == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1005"), "Portfolio-1005");
		}

		String workspace = masterMongoTemplate.findById(jsessionid, UserSession.class).getWorkspaceId();
		deleteFileJfrogFile("/" + workspace + "/portfolio/" + portfolioId + "/" + productId,jsessionid);

		try {
			downloadURI = updateToJfrog(portfolioId + "/" + productId + "/" + fileName, bytes, jsessionid);
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1009"), "Portfolio-1009");
		}
		updateProductPicture(portfolioId, productId, downloadURI, jsessionid);
		return downloadURI;
	}

	private void updateProductPicture(String portfolioId, String productId, String image, String jsessionid)
			throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("products").elemMatch(Criteria.where("id").is(productId))));

		Update update = new Update();
		update.set("products.$.productImage", image);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		mongoTemplate.updateFirst(query, update, Portfolio.class);
	}

	public String updatePortfolioDocument(String portfolioId, String documentId, byte[] bytes, String jsessionid,
			String fileName, Integer revision) throws Exception {
		String workspace = masterMongoTemplate.findById(jsessionid, UserSession.class).getWorkspaceId();
		deleteFileJfrogFile("/" + workspace + "/portfolio/" + portfolioId + "/" + documentId,jsessionid);
		return updateToJfrog(
				portfolioId + "/" + documentId + "/" + Integer.toString(revision) + "/" + fileName.replaceAll(" ", ""),
				bytes, jsessionid);
	}

	private String updateToJfrog(String folderPath, byte[] bytes, String jsession) throws Exception {

		String workspace = masterMongoTemplate.findById(jsession, UserSession.class).getWorkspaceId();

		StorageIntegration storageIntegration = integrationHelper.getIntegration();
		return storageIntegration.uploadFile(workspace + "/portfolio/" + folderPath, new ByteArrayInputStream(bytes));
	}

	private void deleteFileJfrogFile(String folderPath,String jsession) throws ItorixException {

		try {
			String workspace = masterMongoTemplate.findById(jsession, UserSession.class).getWorkspaceId();
			String type = null;
			if (folderPath.contains("?type=")) {
				Integer typeIndex = folderPath.indexOf("?type=");
				type = folderPath.substring(typeIndex + 6);
				folderPath = folderPath.substring(0,typeIndex);
			}

			StorageIntegration storageIntegration = null;
			if (type != null) {
				if (integrationHelper.checkIntegration(type)) {
					storageIntegration = integrationHelper.getIntegration(type);
				}else{
					log.error("file is not deleted from {}", type);
				}
			}else{
				storageIntegration = integrationHelper.getIntegration();
			}
			if (storageIntegration != null) {
				storageIntegration.deleteFile(folderPath);
			}
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1016"), "Portfolio-1016");
		}
	}

	public void deleteApiRegistryDetails(String portfolioId, String servRegistryId) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("serviceRegistry").elemMatch(Criteria.where("id").is(servRegistryId))));

		if (mongoTemplate
				.updateMulti(query,
						new Update().pull("serviceRegistry",
								new Query().addCriteria(Criteria.where("_id").is(servRegistryId))),
						Portfolio.class)
				.getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1011"), "Portfolio-1011");
		}
	}

	public String createProjects(String portfolioId, Projects project, String jsessionid) throws ItorixException {

		Query queryForCount = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("name").is(project.getName()))));

		if (mongoTemplate.count(queryForCount, Portfolio.class) != 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		project.setId(new ObjectId().toString());
		Query query = new Query().addCriteria(Criteria.where("id").is(portfolioId));
		Update update = new Update();
		update.push("projects", project);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1002"), "Portfolio-1002");
		}

		return project.getId();
	}

	public void updateProject(String portfolioId, String projectId, Projects project, String jsessionid)
			throws ItorixException {

		project.setId(projectId);
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId))));

		Update update = new Update();
		update.set("projects.$", project);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1012"), "Portfolio-1012");
		}
	}

	public List<Projects> getProjectsList(String portfolioId, int offset, int pageSize) throws ItorixException {

		Aggregation agg = Aggregation.newAggregation(Aggregation.match(Criteria.where("id").is(portfolioId)),
				Aggregation.project("projects"), Aggregation.unwind("$projects"),
				// Aggregation.skip(Long.valueOf(offset > 0 ? ((offset - 1) *
				// pageSize) : 0)),
				// Aggregation.limit(pageSize),
				Aggregation.project("projects._id", "projects.name", "projects.summary", "projects.owner",
						"projects.isActive"));
		try {
			AggregationResults<Projects> resultData = mongoTemplate.aggregate(agg, Portfolio.class, Projects.class);
			if (resultData != null) {
				List<Projects> project = resultData.getMappedResults();

				ProjectionOperation projectForCount = Aggregation.project("projects.id")
						.and(ArrayOperators.arrayOf("projects").length()).as("count");

				Aggregation aggForCount = Aggregation
						.newAggregation(Aggregation.match(Criteria.where("id").is(portfolioId)), projectForCount);

				AggregationResults<Map> result = mongoTemplate.aggregate(aggForCount, Portfolio.class, Map.class);
				Long count = 0L;
				if (!result.getMappedResults().isEmpty()) {
					count = Long.valueOf((int) result.getMappedResults().get(0).get("count"));
				}
				// return getPaginatedResponse(offset, count, project,
				// pageSize);
				return project;
			} else {
				return new ArrayList<Projects>();
			}
		} catch (Exception e) {
			return new ArrayList<Projects>();
		}
	}

	public Projects getProjectDetails(String portfolioId, String projectId) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId))));

		query.fields().include("projects.$");

		List<Portfolio> portfolios = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(portfolios)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1012"), "Portfolio-1012");
		}
		return portfolios.get(0).getProjects().get(0);
	}

	public void deleteProject(String portfolioId, String projectId) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId))));

		if (mongoTemplate.updateMulti(query,
				new Update().pull("projects", new Query().addCriteria(Criteria.where("_id").is(projectId))),
				Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1012"), "Portfolio-1012");
		}
	}

	public String createProxy(String portfolioId, String projectId, Proxies proxy, String jsessionid)
			throws ItorixException {

		proxy.setId(new ObjectId().toString());

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId))));

		Update update = new Update();
		update.push("projects.$.proxies", proxy);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1013"), "Portfolio-1013");
		}

		return proxy.getId();
	}

	public void updateProxy(String portfolioId, String projectId, String proxyId, Proxies proxy)
			throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId))));
		proxy.setId(proxyId);

		Update update = new Update();
		update.set("projects.$[project].proxies.$[proxy]", proxy).filterArray("project._id", new ObjectId(projectId))
				.filterArray("proxy._id", new ObjectId(proxyId));

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1013"), "Portfolio-1013");
		}
	}

	public List<Proxies> getProxyList(String portfolioId, String projectId, int offset, int pageSize)
			throws ItorixException {
		Aggregation resultAggregate = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("id").is(portfolioId)), Aggregation.unwind("$projects"),
				Aggregation.match(Criteria.where("projects._id").is(new ObjectId(projectId))),
				Aggregation.unwind("$projects.proxies"),
				Aggregation.project().and("$projects.proxies.id").as("_id").and("$projects.proxies.name").as("name")
						.and("$projects.proxies.gwProvider").as("gwProvider").and("$projects.proxies.proxyVersion")
						.as("proxyVersion"));
		// Aggregation.skip(Long.valueOf(offset > 0 ? ((offset - 1) * pageSize)
		// : 0)),
		// Aggregation.limit(pageSize));

		AggregationResults<Proxies> resultData = mongoTemplate.aggregate(resultAggregate, Portfolio.class,
				Proxies.class);
		List<Proxies> proxies = resultData.getMappedResults();
		ProjectionOperation projectForCount = Aggregation.project("projects.proxies.id")
				.and(ArrayOperators.arrayOf("projects.proxies").length()).as("count");

		Aggregation countAggregator = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("id").is(portfolioId)), Aggregation.unwind("$projects"),
				Aggregation.match(Criteria.where("projects._id").is(new ObjectId(projectId))), projectForCount);

		AggregationResults<Map> countResult = mongoTemplate.aggregate(countAggregator, Portfolio.class, Map.class);

		Long count = 0L;
		if (!countResult.getMappedResults().isEmpty()) {
			count = Long.valueOf((int) countResult.getMappedResults().get(0).get("count"));
		}

		// PortfolioResponse response = getPaginatedResponse(offset, count,
		// proxies, pageSize);
		return proxies;
	}

	public Proxies getProxyDetail(String portfolioId, String projectId, String proxyId) throws ItorixException {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId)),
				Criteria.where("projects.proxies").elemMatch(Criteria.where("id").is(proxyId))));

		query.fields().include("projects.proxies.$");
		List<Portfolio> portfolio = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(portfolio)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1013"), "Portfolio-1013");
		}

		return portfolio.get(0).getProjects().get(0).getProxies().stream().filter(s -> s.getId().equals(proxyId))
				.findFirst().get();
	}

	public void deleteProxyDetail(String portfolioId, String projectId, String proxyId, String jsessionid)
			throws ItorixException {

		String workspace = masterMongoTemplate.findById(jsessionid, UserSession.class).getWorkspaceId();
		Proxies proxyDetail = getProxyDetail(portfolioId, projectId, proxyId);
		if (proxyDetail != null) {
			try {
				DesignArtifacts designArtifacts = proxyDetail.getApigeeConfig().getDesignArtifacts();
				if (designArtifacts != null) {
					designArtifacts.getWsdlFiles().stream().forEach(s -> {
						try {
							if (s.getWsdlLocation().contains(workspace)) {
								deleteFileJfrogFile(
										s.getWsdlLocation().substring(s.getWsdlLocation().indexOf(workspace)),jsessionid);
							}
						} catch (ItorixException e) {
							log.error("error when deleting proxy files");
						}
					});
					designArtifacts.getXsdFiles().stream().forEach(s -> {
						try {
							if (s.getXsdLocation().contains(workspace)) {
								deleteFileJfrogFile(
										s.getXsdLocation().substring(s.getXsdLocation().indexOf(workspace)),jsessionid);
							}

						} catch (ItorixException e) {
							log.error("error when deleting proxy files");
						}
					});
				}
			} catch (Exception e) {
			}
		}

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId)),
				Criteria.where("projects.proxies").elemMatch(Criteria.where("id").is(proxyId))));
		if (mongoTemplate
				.updateMulti(query,
						new Update().pull("projects.$.proxies",
								new Query().addCriteria(Criteria.where("id").is(proxyId))),
						Portfolio.class)
				.getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1013"), "Portfolio-1013");
		}
	}

	public String createPipeline(String id, String projectId, String proxyId, String jsessionid, Pipelines pipeline)
			throws ItorixException {

		pipeline.setId(new ObjectId().toString());

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId)),
				Criteria.where("projects.proxies").elemMatch(Criteria.where("id").is(proxyId))));

		Update update = new Update();

		update.push("projects.$[project].proxies.$[proxy].apigeeConfig.pipelines", pipeline)
				.filterArray("project._id", new ObjectId(projectId)).filterArray("proxy._id", new ObjectId(proxyId));

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getMatchedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1015"), "Portfolio-1015");
		}
		return pipeline.getId();
	}

	public void updateProxyPipeline(String id, String projectId, String proxyId, String pipelineId, Pipelines pipeline,
			String jsessionid) throws ItorixException {

		pipeline.setId(pipelineId);
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId)),
				Criteria.where("projects.proxies").elemMatch(Criteria.where("id").is(proxyId))));
		Update update = new Update();
		update.set("projects.$[project].proxies.$[proxy].apigeeConfig.pipelines.$[pipeline]", pipeline)

				.filterArray("project._id", new ObjectId(projectId)).filterArray("proxy._id", new ObjectId(proxyId))
				.filterArray("pipeline._id", new ObjectId(pipelineId));

		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1015"), "Portfolio-1015");
		}
	}

	public List<Pipelines> getPipeline(String portfolioId, String projectId, String proxyId, String jsessionid)
			throws ItorixException {

		Aggregation resultAggregate = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("id").is(portfolioId)), Aggregation.unwind("$projects"),
				Aggregation.match(Criteria.where("projects._id").is(new ObjectId(projectId))),
				Aggregation.unwind("$projects.proxies"),
				Aggregation.match(Criteria.where("projects.proxies._id").is(new ObjectId(proxyId))));

		AggregationResults<PipelineResponse> resultData = mongoTemplate.aggregate(resultAggregate, Portfolio.class,
				PipelineResponse.class);
		List<PipelineResponse> proxies = resultData.getMappedResults();
		if (proxies != null) {
			if (!CollectionUtils.isEmpty(proxies)) {
				if (proxies.get(0).getProjects() != null && proxies.get(0).getProjects().getProxies() != null
						&& proxies.get(0).getProjects().getProxies().getApigeeConfig() != null && !CollectionUtils
								.isEmpty(proxies.get(0).getProjects().getProxies().getApigeeConfig().getPipelines())) {
					return proxies.get(0).getProjects().getProxies().getApigeeConfig().getPipelines();
				}
			}
		}
		// throw new
		// ItorixException(ErrorCodes.errorMessage.get("Portfolio-1015"),
		// "Portfolio-1015");

		// return
		// portfolio.get(0).getProjects().get(0).getProxies().stream().filter(s
		// ->
		// s.getId().equals(proxyId))
		// .findFirst().get();
		return new ArrayList<Pipelines>();
	}

	public void deletePipeline(String id, String projectId, String proxyId, String pipelineId, String jsessionid)
			throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId)),
				Criteria.where("projects.proxies").elemMatch(Criteria.where("id").is(proxyId)),
				Criteria.where("projects.proxies.apigeeConfig.pipelines")
						.elemMatch(Criteria.where("id").is(pipelineId))));

		Update update = new Update();

		Query queryPipeline = new Query(new Criteria().andOperator(Criteria.where("id").is(pipelineId)));
		update.pull("projects.$[project].proxies.$[proxy].apigeeConfig.pipelines", queryPipeline)
				.filterArray("project._id", new ObjectId(projectId)).filterArray("proxy._id", new ObjectId(proxyId));

		if (mongoTemplate.updateFirst(query, update, Portfolio.class).getModifiedCount() == 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1015"), "Portfolio-1015");
		}
		update = new Update();
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
		update.set("mts", System.currentTimeMillis());
		update.set("modifiedBy", user.getFirstName() + " " + user.getLastName());
		mongoTemplate.updateFirst(query, update, Portfolio.class);
	}

	public Object search(String name, int limit) throws ItorixException {
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<Portfolio> items = mongoTemplate.find(query, Portfolio.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (Portfolio vo : items) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.getId());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("Portfolios", responseFields);
		return response;
	}

	public String uploadDesignArtifact(String portfolioId, String projectId, byte[] documentBytes, String jsessionid,
			String originalFilename) throws Exception {
		return updateToJfrog(
				portfolioId + "/" + projectId + "/proxy/" + System.currentTimeMillis() + "/" + originalFilename,
				documentBytes, jsessionid);
	}

	public List<Portfolio> getProxyDetails(String proxyName) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("projects.proxies.name").is(proxyName)));
		query.fields().include("_id");
		query.fields().include("name");
		query.fields().include("projects._id");
		query.fields().include("projects.name");
		query.fields().include("projects.proxies._id");
		query.fields().include("projects.proxies.name");
		List<Portfolio> portfolio = mongoTemplate.find(query, Portfolio.class);
		if (CollectionUtils.isEmpty(portfolio)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1013"), "Portfolio-1013");
		}
		return portfolio;
	}

	public Portfolio getPortfolio(String portfolioId, String projectId, String proxyId) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(portfolioId),
				Criteria.where("projects").elemMatch(Criteria.where("id").is(projectId)),
				Criteria.where("projects.proxies").elemMatch(Criteria.where("id").is(proxyId))));
		Portfolio portfolio = mongoTemplate.findOne(query, Portfolio.class);
		if (ObjectUtils.isEmpty(portfolio)) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1013"), "Portfolio-1013");
		}
		return portfolio;
	}

}
