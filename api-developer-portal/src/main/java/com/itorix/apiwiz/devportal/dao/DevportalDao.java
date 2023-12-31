package com.itorix.apiwiz.devportal.dao;

import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.StaticFields;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXConfigurationVO;
import com.itorix.apiwiz.common.model.azure.AzureConfigurationVO;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.kong.KongRuntime;
import com.itorix.apiwiz.common.model.monetization.*;
import com.itorix.apiwiz.design.studio.model.*;
import com.itorix.apiwiz.devportal.model.DeveloperApp;
import com.itorix.apiwiz.devportal.model.ProductBundleCard;
import com.itorix.apiwiz.devportal.model.ProductBundleCardResponse;
import com.itorix.apiwiz.devportal.model.Specs;
import com.itorix.apiwiz.devportal.model.monetization.AppWallet;
import com.itorix.apiwiz.devportal.model.monetization.ComputedBill;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseRecord;
import com.itorix.apiwiz.devportal.model.monetization.PurchaseResult;
import com.itorix.apiwiz.identitymanagement.model.Pagination;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.http.HTTPUtil;

@Component("devportalDao")
public class DevportalDao {

	private Logger logger = Logger.getLogger(DevportalDao.class);

	@Autowired
	private MongoTemplate mongoTemplate;


	/**
	 * @param httpConn
	 * @param method
	 *
	 * @return
	 *
	 * @throws ItorixException
	 */
	public ResponseEntity<String> proxyService(HTTPUtil httpConn, String method) throws ItorixException {
		try {
			ResponseEntity<String> response;
			if (method.equals("POST"))
				response = httpConn.doPost();
			else if (method.equals("PUT"))
				response = httpConn.doPut();
			else if (method.equals("DELETE"))
				response = httpConn.doDelete();
			else
				response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				ResponseEntity<String> responseEntity = new ResponseEntity<String>(response.getBody(), headers,
						HttpStatus.OK);
				return responseEntity;
			} else if (statusCode.value() >= 401 && statusCode.value() <= 403) {
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			}else if(statusCode.value() == 409 ){
				throw new ItorixException("Request validation failed. App with same name already exists - " + statusCode.value(),"Portal-1000");
			} else if (statusCode.value() >= 404) {
				throw new ItorixException("Request validation failed. Invalid data - " + statusCode.value(),
						"Portal-1001");
			} else
				throw new ItorixException("Invalid request data " + statusCode.value(), "Portal-1001");
		} catch (ItorixException ex) {
			logger.error(ex);
			throw ex;
		} catch (Exception ex) {
			logger.error(ex);
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}

	public Object getProductBundleCards(String partnerType,String org, int offset, int pagesize, boolean paginated,String organizations){
		List<ProductBundleCard> productBundleCards = new ArrayList<>();
		Query query = new Query();
		if(partnerType != null){
			String [] types = partnerType.split(",");
			query.addCriteria(Criteria.where("partners").in(types));
		}
		if(organizations!=null){
			String [] allOrgs = organizations.split(",");
			query.addCriteria(Criteria.where("organization").in(allOrgs));
		}
		query.addCriteria(Criteria.where("status").is("Approved").and("activeFlag").is(Boolean.TRUE));

		if(paginated) {
			query.with(Sort.by(Direction.DESC, "modifiedDate"))
					.skip(offset > 0 ? ((offset - 1) * pagesize) : 0)
					.limit(pagesize);
		}
		if(org != null){
			query.addCriteria(Criteria.where("organization").is(org));
		}
		List<ProductBundle> productBundles = mongoTemplate.find(query,ProductBundle.class);

		productBundles.forEach(productBundle -> {
			Map<String, Set<Specs>> specList = new HashMap<>();
			ProductBundleCard productBundleCard = new ProductBundleCard();
			productBundleCard.setProductBundle(productBundle);
			Query ratePlanQuery = new Query(Criteria.where("configStatus").is("Approved").and("activeFlag").is(Boolean.TRUE)
					.and("productBundleId").is(productBundle.getApigeeProductBundleId()));
			if(org!=null){
				ratePlanQuery.addCriteria(Criteria.where("organization").is(org));
			}
			List<RatePlan> ratePlanList = mongoTemplate.find(ratePlanQuery, RatePlan.class);
			productBundleCard.setRatePlans(ratePlanList);
			TreeSet<String> productBundleProducts =  productBundle.getProducts();
			List<SwaggerMetadata> swaggerMetadata = mongoTemplate.findAll(SwaggerMetadata.class);
			swaggerMetadata.forEach(metadata -> {
				String oas = metadata.getOas();
				Set<String> products = metadata.getProducts();
				if(products != null) {
					List<SwaggerProduct> swaggerProducts = new ArrayList<>();
					for (String product : products) {
						Set<Specs> specs = new HashSet<>();
						SwaggerProduct swaggerProduct = mongoTemplate.findById(product, SwaggerProduct.class);
						if(swaggerProduct != null ){
							if (productBundleProducts.contains(swaggerProduct.getProductName())) {
								Specs spec = new Specs();
								if (oas.equals("2.0")) {
									SwaggerVO swaggerVO = mongoTemplate.findOne(
											Query.query(Criteria.where("name").is(metadata.getSwaggerName())
													.and("status").is("Publish")), SwaggerVO.class);
									if(swaggerVO != null) {
										spec.setSwaggerId(swaggerVO.getSwaggerId());
										spec.setOasVersion(oas);
										spec.setSwaggerName(swaggerVO.getName());
										spec.setSwaggerRevision(swaggerVO.getRevision());
										specs.add(spec);
									}
								} else {
									Swagger3VO swagger3VO = mongoTemplate.findOne(
											Query.query(Criteria.where("name").is(metadata.getSwaggerName())
													.and("status").is("Publish")), Swagger3VO.class);
									if(swagger3VO != null) {
										spec.setSwaggerId(swagger3VO.getSwaggerId());
										spec.setOasVersion(oas);
										spec.setSwaggerName(swagger3VO.getName());
										spec.setSwaggerRevision(swagger3VO.getRevision());
										specs.add(spec);
									}
								}
								if(specList.get(swaggerProduct.getProductName())!=null){
									specList.get(swaggerProduct.getProductName()).addAll(specs);
								}else {
									specList.put(swaggerProduct.getProductName(), specs);
								}
							}
						}
					}
				}
			});
			productBundleCard.setSpecs(specList);
			productBundleCards.add(productBundleCard);
		});
		if(paginated) {
			ProductBundleCardResponse response = new ProductBundleCardResponse();
			Pagination pagination = new Pagination();
			Long counter;
			counter = mongoTemplate.count(query, ProductBundle.class);
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pagesize);
			response.setPagination(pagination);
			response.setResponse(productBundleCards);
			return response;
		}else{
			return productBundleCards;
		}
	}


	public PurchaseResult executePurchase(PurchaseRecord purchaseRecord) {

		PurchaseRecord.PaymentMode paymentMode = purchaseRecord.getPaymentMode();
		double setupFee = purchaseRecord.getRatePlan().getContractDetail().getSetupFee();
		if(paymentMode.equals(PurchaseRecord.PaymentMode.WALLET)){
			//If Wallet, check balance of wallet
			AppWallet wallet = getWalletBalanceByAppId(purchaseRecord.getAppId());
			if(wallet.getBalance() < setupFee){
				return PurchaseResult.INSUFFICIENT_BALANCE;
			}else{
				try{
					if(wallet.getId() != null){
						Query query = new Query();
						query.addCriteria(Criteria.where("_id").is(wallet.getId()));
						mongoTemplate.findAndRemove(query,AppWallet.class);
						wallet.debit(setupFee);
						mongoTemplate.save(wallet);
					}

				}catch (Exception ex){
					logger.error("Could not debit wallet:" + ex.getMessage());
					return PurchaseResult.DEBIT_ERROR;
				}
			}
		}else{
			//Do nothing
		}
		purchaseRecord.setCts(System.currentTimeMillis());
		purchaseRecord.setMts(System.currentTimeMillis());
		mongoTemplate.save(purchaseRecord);
		return PurchaseResult.SUCCESS;
	}
	public List<PurchaseRecord> getPurchaseHistoryByAppId(String appId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(appId));

		List<PurchaseRecord> purchaseRecords = mongoTemplate.find(query,PurchaseRecord.class);
		if(purchaseRecords == null) purchaseRecords = new ArrayList<>();
		return purchaseRecords;
	}
	public List<PurchaseRecord> getPurchaseHistory(String appId, String developerEmailId, String organization) throws ItorixException {
		Query query = new Query();
		if(appId != null && !appId.isEmpty()) query.addCriteria(Criteria.where("appId").is(appId));
		if(developerEmailId != null && !developerEmailId.isEmpty()) query.addCriteria(Criteria.where("developerEmailId").is(developerEmailId));
		if(organization != null && !organization.isEmpty()) {
			query.addCriteria(Criteria.where("ratePlan.organization").is(organization));
		} else {
			List<String> orgNames = getOrganisationNames();
			query.addCriteria(Criteria.where("ratePlan.organization").in(orgNames));
		}
		return mongoTemplate.find(query,PurchaseRecord.class);
	}
	public void deletePurchaseById(String appId, String purchaseId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(appId));
		query.addCriteria(Criteria.where("_id").is(purchaseId));

		mongoTemplate.findAndRemove(query,PurchaseRecord.class);
	}
	public AppWallet getWalletBalanceByAppId(String appId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(appId));

		AppWallet wallet = mongoTemplate.findOne(query,AppWallet.class);
		if(wallet == null){
			wallet = new AppWallet();
			wallet.setAppId(appId);
			//Dummy - Init Wallet with 20-80K balance
			wallet.setBalance(((Math.random() * (80000 - 20000)) + 20000));
			wallet.setCreatedBy("System");
			wallet.setModifiedBy("System");
			wallet = mongoTemplate.save(wallet);
		}

		return wallet;
	}
	public AppWallet addWalletBalanceForAppId(String appId, double topUp) {
		AppWallet wallet = getWalletBalanceByAppId(appId);
		wallet.credit(topUp);
		wallet.setMts(System.currentTimeMillis());

		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(appId));
		mongoTemplate.findAndRemove(query,AppWallet.class);

		return mongoTemplate.save(wallet);

	}
	public ComputedBill computeBillForAppId(String appId, double transactions, String startDate, String endDate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("appId").is(appId));

		List<PurchaseRecord> purchaseRecords = mongoTemplate.find(query,PurchaseRecord.class);
		ComputedBill computedBill = new ComputedBill();
		computedBill.setAppId(appId);
		computedBill.setTransaction(transactions);
		computedBill.setStartDate(startDate);
		computedBill.setEndDate(endDate);
		computedBill.setCts(System.currentTimeMillis());
		computedBill.setMts(System.currentTimeMillis());
		computedBill.setCreatedBy("System");
		computedBill.setModifiedBy("System");
		computedBill.setBilledAmount(0);

		double billedAmount = 0;

		List<RatePlan> ratePlans = new ArrayList<>();

		for(PurchaseRecord purchase : purchaseRecords){
			RatePlan ratePlan = purchase.getRatePlan();
			ratePlans.add(ratePlan);
			billedAmount += ratePlan.getContractDetail().getSetupFee();

			if(ratePlan.getType().equals(RatePlan.Type.FEESONLY)){
				LocalDate dateBefore = LocalDate.parse(startDate);
				LocalDate dateAfter = LocalDate.parse(endDate);
				long transactedDays = ChronoUnit.DAYS.between(dateBefore, dateAfter);

				int ratePlanBillingPeriod = ratePlan.getCostModel().getBillingPeriod();
				int netRatePlanBillingPeriod = 0;

				CostModel.BillingPeriodUnit ratePlanBillingPeriodUnit = ratePlan.getCostModel().getBillingPeriodUnit();
				if(ratePlanBillingPeriodUnit.equals(CostModel.BillingPeriodUnit.DAY)){
					netRatePlanBillingPeriod = ratePlanBillingPeriod;
				} else if (ratePlanBillingPeriodUnit.equals(CostModel.BillingPeriodUnit.WEEK)) {
					netRatePlanBillingPeriod = 7 * ratePlanBillingPeriod;
				}else{
					netRatePlanBillingPeriod = 30 * ratePlanBillingPeriod;
				}

				double multiplier = (transactedDays / netRatePlanBillingPeriod);

				billedAmount += (multiplier * transactions);
			}

			if(ratePlan.getType().name().contains("RATECARD")){
				RateCard rateCard = ratePlan.getRateCard();
				if(rateCard != null){
					if(rateCard.getFlatRate() > 0){
						billedAmount += (rateCard.getFlatRate() * transactions);
					}else{
						List<RateCardVolumeBand> bands = rateCard.getVolumeBundles();
						List<RateCardBundle> bundles = rateCard.getBundles();

						if(bands != null && !bands.isEmpty()){
							int numBands = bands.size();
							double startUnit = 0;
							for(int i=0;i< numBands;i++){
								double usage = bands.get(i).getUsage();
								double rate = bands.get(i).getRate();
								if(startUnit != usage){
									if(startUnit < transactions){
										if(usage <= transactions){
											billedAmount += (usage - startUnit) * rate;
										}else{
											billedAmount += (transactions - startUnit) * rate;
										}
									}
								}
								startUnit = usage;
							}
						}

						if(bundles != null && !bundles.isEmpty()){
							int numBundles = bundles.size();
							double startUnit = 0;
							for(int i=0;i< numBundles;i++){
								double usage = bundles.get(i).getUsage();
								double rate = bundles.get(i).getRate();
								if(startUnit != usage){
									if(startUnit < transactions){
										if(usage <= transactions){
											billedAmount += (usage - startUnit) * rate;
										}else{
											billedAmount += (transactions - startUnit) * rate;
										}
									}
								}
								startUnit = usage;
							}
						}
					}
				}
			}

			if(ratePlan.getType().name().contains("REVSHARE")){
				RevenueShare revenueShare = ratePlan.getRevenueShare();
				if(revenueShare != null){
					if(revenueShare.getSharingModel().equals(RevenueShare.SharingModel.FLEXIBLE)){
						List<RevenueShareBand> bands = revenueShare.getRevenueShareBands();

						if(bands != null && !bands.isEmpty()){
							int numBands = bands.size();
							double startUnit = 0;
							for(int i=0;i< numBands;i++){
								double usage = bands.get(i).getUsage();
								double rate = bands.get(i).getRate();
								if(startUnit != usage){
									if(startUnit < transactions){
										if(usage <= transactions){
											billedAmount += (usage - startUnit) * rate;
										}else{
											billedAmount += (transactions - startUnit) * rate;
										}
									}
								}
								startUnit = usage;
							}
						}

					}else{
						//Dummy Based on 5000USD Revenue... Need to compute with actual revenue generated once the MTN deal is signed
						billedAmount += 5000 * revenueShare.getFixedSharePercentage();
					}
				}
			}
		}

		computedBill.setRatePlans(ratePlans);
		computedBill.setBilledAmount(billedAmount);
		computedBill.setRatePlans(ratePlans);
		return computedBill;
	}


	public void saveDeveloperApp(DeveloperApp developerApp){
		mongoTemplate.save(developerApp);
	}

	public List<DeveloperApp> getRegisteredApps(String org, String email, String appId , String appName) {
		if (org == null && email == null && appId == null && appName == null) {
			return mongoTemplate.findAll(DeveloperApp.class);
		} else {
			Query query = new Query();
			if (org != null) {
				query.addCriteria(Criteria.where("organization").is(org));
			}
			if (email != null) {
				query.addCriteria(Criteria.where("email").is(email));
			}
			if (appId != null) {
				query.addCriteria(Criteria.where("appId").is(appId));
			}
			if (appName != null) {
				query.addCriteria(Criteria.where("appName").is(appName));
			}
			return mongoTemplate.find(query, DeveloperApp.class);
		}
	}

	public List<AppWallet> getWalletBalanceByFilter(String appId, String email) {
		Query query = new Query();
		if(appId != null) query.addCriteria(Criteria.where("appId").is(appId));
		if(email != null) query.addCriteria(Criteria.where("developerEmailId").is(email));

		List<AppWallet> wallets = mongoTemplate.find(query,AppWallet.class);
		if(wallets == null) wallets = new ArrayList<>();
		return wallets;

	}

	public DeveloperApp getDeveloperAppWithAppId(String appId){
		return mongoTemplate.findOne(Query.query(Criteria.where("appId").is(appId)),DeveloperApp.class);
	}
	public List<String> getAllGateways() {
		long countApigee = mongoTemplate.count(new Query(), StaticFields.APIGEE_CONFIG_COLLECTION);
		long countApigeeX = mongoTemplate.count(new Query(), StaticFields.APIGEEX_CONFIG_COLLECTION);
		long countKong = mongoTemplate.count(new Query(), StaticFields.KONG_RUNTIME_COLLECTION);
		long countAzure = mongoTemplate.count(new Query(), StaticFields.AZURE_CONFIG_COLLECTION);

		List<String> gateways = new ArrayList<>();

		if (countApigee > 0) {
			gateways.add(StaticFields.APIGEE);
		}

		if (countApigeeX > 0) {
			gateways.add(StaticFields.APIGEEX);
		}

		if (countKong > 0) {
			gateways.add(StaticFields.KONG);
		}

		if (countAzure > 0) {
			gateways.add(StaticFields.AZURE);
		}
		return gateways;
	}

	public List<?> getGatewayEnvironments(String name, String resourceGroup) throws ItorixException {
		Query query = new Query();
		String collection;
		if (name.equalsIgnoreCase(StaticFields.APIGEE)) {
			query.fields().include("orgname","type").exclude("_id");
			collection=StaticFields.APIGEE_CONFIG_COLLECTION;
		}else if (name.equalsIgnoreCase(StaticFields.APIGEEX)) {
			query.fields().include("orgName").exclude("_id");
			collection=StaticFields.APIGEEX_CONFIG_COLLECTION;
		}else if (name.equalsIgnoreCase(StaticFields.KONG)) {
			query.fields().include("name").exclude("_id");
			collection=StaticFields.KONG_RUNTIME_COLLECTION;
		}else if (name.equalsIgnoreCase(StaticFields.AZURE)) {
			collection=StaticFields.AZURE_CONFIG_COLLECTION;
			if(resourceGroup==null){
				query.fields().include("serviceName","resourceGroup").exclude("_id");
			}else{
				query.addCriteria(Criteria.where("resourceGroup").is(resourceGroup));
				query.fields().include("serviceName","resourceGroup").exclude("_id");
			}
		}else{
			throw  new ItorixException(ErrorCodes.errorMessage.get("Portal-1001"),"Portal-1001");
		}
		return mongoTemplate.find(query, Document.class, collection);
	}

	public AzureConfigurationVO getAzureConnector(String serviceName,String resourceGroup) {
		Query query = new Query(Criteria.where("serviceName").is(serviceName).and("resourceGroup").is(resourceGroup));
		return mongoTemplate.findOne(query, AzureConfigurationVO.class);
	}

	public List<AzureConfigurationVO> getAllAzureConnectors(){
		Query query= new Query();
		query.fields().include("resourceGroup");
		return mongoTemplate.find(query,AzureConfigurationVO.class);
	}
	public KongRuntime getKongRuntime(String runtime) {
		Query query = new Query(Criteria.where("name").is(runtime));
		return mongoTemplate.findOne(query, KongRuntime.class);
	}
	public List<ApigeeXConfigurationVO> getApigeexConnectedOrgs() {
		return mongoTemplate.findAll(ApigeeXConfigurationVO.class);
	}

	public List<ApigeeXConfigurationVO> getApigeexConnectedOrgs(List<String> orgsList) {
		return mongoTemplate.find(new Query(Criteria.where("orgName").in(orgsList)),ApigeeXConfigurationVO.class);
	}

	public List<ApigeeConfigurationVO> getApigeeConnectedOrgs() {
		return mongoTemplate.findAll(ApigeeConfigurationVO.class);
	}
	public List<ApigeeConfigurationVO> getApigeeConnectedOrgs(List<String> orgsList) {
		return mongoTemplate.find(new Query(Criteria.where("orgname").in(orgsList)),ApigeeConfigurationVO.class);
	}


	public List<String> getSwaggerPartners(List<String> partnerIds) {
		Query query = new Query(Criteria.where("id").in(partnerIds));
		query.fields().include("partnerName");
		List<SwaggerPartner> swaggerPartners = mongoTemplate.find(query, SwaggerPartner.class);
		return swaggerPartners.stream()
				.map(SwaggerPartner::getPartnerName)
				.collect(Collectors.toList());
	}
	public List<ProductBundle> getAllProductBundles(Query query) {
		return mongoTemplate.find(query, ProductBundle.class);
	}
	public List<String> getOrganisationNames() throws ItorixException {
		List<Document> apigeexOrgNamesList = (List<Document>) getGatewayEnvironments("apigeex",null);
		List<Document> apigeeOrgNamesList = (List<Document>) getGatewayEnvironments("apigee",null);
		List<String> orgNames = new ArrayList<>();
		for (Document orgName: apigeexOrgNamesList){
			orgNames.add(orgName.get("orgName").toString());
		}
		for (Document orgName: apigeeOrgNamesList){
			orgNames.add(orgName.get("orgname").toString());
		}
		return orgNames;
	}
}