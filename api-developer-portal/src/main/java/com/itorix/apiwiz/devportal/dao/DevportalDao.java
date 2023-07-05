package com.itorix.apiwiz.devportal.dao;

import com.itorix.apiwiz.common.model.azure.AzureConfigurationVO;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.kong.KongRuntime;
import com.itorix.apiwiz.common.model.monetization.*;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerMetadata;
import com.itorix.apiwiz.design.studio.model.SwaggerProduct;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
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

	public Object getProductBundleCards(String partnerType,String org, int offset, int pagesize, boolean paginated){
		List<ProductBundleCard> productBundleCards = new ArrayList<>();
		Query query = null;
		if(partnerType != null){
			String [] types = partnerType.split(",");
			query = new Query(Criteria.where("status").is("Approved").and("activeFlag").is(Boolean.TRUE).and("partners").in(types));
		}else{
			query = new Query(Criteria.where("status").is("Approved").and("activeFlag").is(Boolean.TRUE));
		}
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
	public List<PurchaseRecord> getPurchaseHistory(String appId, String developerEmailId, String organization) {
		Query query = new Query();
		if(appId != null && !appId.isEmpty()) query.addCriteria(Criteria.where("appId").is(appId));
		if(developerEmailId != null && !developerEmailId.isEmpty()) query.addCriteria(Criteria.where("developerEmailId").is(developerEmailId));
		if(organization != null && !organization.isEmpty()) query.addCriteria(Criteria.where("ratePlan.organization").is(organization));

		List<PurchaseRecord> purchaseRecords = mongoTemplate.find(query,PurchaseRecord.class);
		if(purchaseRecords == null) purchaseRecords = new ArrayList<>();
		return purchaseRecords;
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

	public List<String> getAllGateways(){
			try {
				final String ApigeeCollection="Connectors.Apigee.Configuration";
				final String ApigeeXCollection="Connectors.ApigeeX.Configuration";
				final String KongCollection="Connectors.Kong.Runtime.List";
				final String AzureCollection="Connectors.Azure.Configuration";

				long countApigee = mongoTemplate.count(new Query(), ApigeeCollection);
				long countApigeeX = mongoTemplate.count(new Query(), ApigeeXCollection);
				long countKong = mongoTemplate.count(new Query(), KongCollection);
				long countAzure = mongoTemplate.count(new Query(), AzureCollection);

				List<String> gateways=new ArrayList<>();

				if(countApigee>0){
					gateways.add("Apigee");
				}

				if(countApigeeX>0){
					gateways.add("ApigeeX");
				}

				if(countKong>0){
					gateways.add("Kong");
				}

				if(countAzure>0){
					gateways.add("Azure");
				}

				return gateways;
			}catch (Exception e){
				throw e;
			}

		}

		public List<?> getGatewayInfo(String name){

			try{
				if(name.equalsIgnoreCase("Apigee")){
					Query query = new Query();
					query.fields().include("orgname").exclude("_id");
					return mongoTemplate.find(query, Document.class,"Connectors.Apigee.Configuration");
				}

				if(name.equalsIgnoreCase("ApigeeX")){
					Query query = new Query();
					query.fields().include("orgname").exclude("_id");
					return mongoTemplate.find(query, Document.class,"Connectors.ApigeeX.Configuration");
				}

				if(name.equalsIgnoreCase("Kong")){
					Query query = new Query();
					query.fields().include("name").exclude("_id");
					return mongoTemplate.find(query,Document.class,"Connectors.Kong.Runtime.List");
				}

				if(name.equalsIgnoreCase("Azure")){
					Query query = new Query();
					query.fields().include("serviceName").exclude("_id");
					return mongoTemplate.find(query,Document.class,"Connectors.Azure.Configuration");
				}

				return new ArrayList<>();
			}catch (Exception e){
				throw e;
			}
		}

		public AzureConfigurationVO getConnector(String connectorName){
			Query query = new Query(Criteria.where("connectorName").is(connectorName));
			return mongoTemplate.find(query,AzureConfigurationVO.class).get(0);
		}

		public KongRuntime getKongRuntime(String runtime){
			Query query = new Query(Criteria.where("name").is(runtime));
			return mongoTemplate.find(query,KongRuntime.class).get(0);
		}


}
