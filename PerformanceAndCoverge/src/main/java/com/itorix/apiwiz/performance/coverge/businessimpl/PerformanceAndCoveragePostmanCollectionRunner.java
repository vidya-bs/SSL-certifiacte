package com.itorix.apiwiz.performance.coverge.businessimpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.postman.PostmanCollection;
import com.itorix.apiwiz.common.model.postman.PostmanEnvironment;
import com.itorix.apiwiz.common.model.postman.PostmanFolder;
import com.itorix.apiwiz.common.model.postman.PostmanRequest;
import com.itorix.apiwiz.common.model.postman.PostmanRequestRunner;
import com.itorix.apiwiz.common.model.postman.PostmanVariables;
import com.itorix.apiwiz.common.model.postman.Trace;
import com.itorix.apiwiz.common.postman.PostmanReader;
import com.itorix.apiwiz.common.postman.PostmanRunResult;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;

import net.sf.json.JSONArray;

@Component
public class PerformanceAndCoveragePostmanCollectionRunner {
	private static final Logger logger = Logger.getLogger(PerformanceAndCoveragePostmanCollectionRunner.class);
	public static final String ARG_COLLECTION = "c";
	public static final String ARG_ENVIRONMENT = "e";
	public static final String ARG_FOLDER = "f";
	public static final String ARG_HALTONERROR = "haltonerror";

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	ApigeeUtil apigeeUtil;

	@Autowired
	CommonServices commonServices;

	public static void main(String[] arg) {
		logger.debug("inside runner");
		String collectionFile = "C:/API/ApigeeUnitTests.postman_collection.json";
		String envFile = "C:/API/Apigee.postman_environment.json";
		PerformanceAndCoveragePostmanCollectionRunner pcr = new PerformanceAndCoveragePostmanCollectionRunner();
		try {
			PostmanRunResult result = pcr.runCollection(collectionFile, envFile, null, false);
			ObjectMapper mapper = new ObjectMapper();
			logger.debug(mapper.writeValueAsString(result));

		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
	}

	public PostmanRunResult executePostManCollection(String postManFileName, String envFileName) throws Exception {
		try {
			return runCollection(postManFileName, envFileName, null, false);
		} catch (Exception e) {
			logger.error("Exception occurred", e);
			throw e;
		}
	}

	public List<Object> executePostManCollectionTraceAsObject(String postManFileName, String envFileName,
			CommonConfiguration cfg) throws Exception {
		try {
			return runCollectionTraceAsObject(postManFileName, envFileName, null, false, cfg);
		} catch (Exception e) {

			logger.error("Exception occurred", e);
			throw e;
		}
	}

	public List<Object> executePostManCollectionTraceAsObject(InputStream postManFileName, InputStream envFileName,
			CommonConfiguration cfg) throws Exception {

		try {
			return runCollectionTraceAsObject(postManFileName, envFileName, null, false, cfg);
		} catch (Exception e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public PostmanRunResult executePostManCollection(InputStream postManFile, InputStream envFile) throws Exception {
		try {
			PostmanReader reader = new PostmanReader();
			PostmanCollection collection = reader.readCollectionFile(postManFile);
			PostmanEnvironment env = reader.readEnvironmentFile(envFile);

			return runCollection(collection, env, null, false);
		} catch (Exception e) {
			logger.error("Exception occurred",e);
			throw e;
		}
	}

	public PostmanRunResult runCollection(String colFilename, String envFilename, String folderName,
			boolean haltOnError) throws Exception {
		PostmanReader reader = new PostmanReader();
		PostmanCollection c = reader.readCollectionFile(colFilename);
		PostmanEnvironment e = reader.readEnvironmentFile(envFilename);
		return runCollection(c, e, folderName, haltOnError);
	}

	public List<Object> runCollectionTraceAsObject(String colFilename, String envFilename, String folderName,
			boolean haltOnError, CommonConfiguration cfg) throws Exception {
		PostmanReader reader = new PostmanReader();
		PostmanCollection c = reader.readCollectionFile(colFilename);
		PostmanEnvironment e = reader.readEnvironmentFile(envFilename);
		return runCollectionTraceAsObject(c, e, folderName, haltOnError, cfg);
	}

	public List<Object> runCollectionTraceAsObject(InputStream colFilename, InputStream envFilename, String folderName,
			boolean haltOnError, CommonConfiguration cfg) throws Exception {
		PostmanReader reader = new PostmanReader();
		PostmanCollection c = reader.readCollectionFile(colFilename);
		PostmanEnvironment e = reader.readEnvironmentFile(envFilename);
		return runCollectionTraceAsObject(c, e, folderName, haltOnError, cfg);
	}

	public PostmanRunResult runCollection(PostmanCollection collection, PostmanEnvironment env, String folderName,
			boolean haltOnError) throws Exception {
		logger.debug("@@@@@ POSTMAN Runner start!");
		PostmanRunResult runResult = new PostmanRunResult();
		PostmanReader reader = new PostmanReader();
		PostmanCollection c = collection;
		c.init();
		PostmanEnvironment e = env;
		e.init();
		PostmanFolder folder = null;
		if (folderName != null && !folderName.isEmpty()) {
			folder = c.folderLookup.get(folderName);
		}

		PostmanVariables var = new PostmanVariables(e);
		PostmanRequestRunner runner = new PostmanRequestRunner(var, haltOnError);
		boolean isSuccessful = true;
		if (c.folders != null && c.folders.size() > 0) {
			for (PostmanFolder pf : c.folders) {
				isSuccessful = runFolder(haltOnError, runner, var, c, pf, runResult);
			}
		} else {
			// Execute all folder all requests
			isSuccessful = runFile(haltOnError, runner, var, c, runResult) && isSuccessful;
			if (haltOnError && !isSuccessful) {
				return runResult;
			}
			/*
			 * for (PostmanFolder pf : c.folders) { isSuccessful =
			 * runFolder(haltOnError, runner, var, c, null, runResult) &&
			 * isSuccessful; if (haltOnError && !isSuccessful) { return
			 * runResult; } }
			 */
		}

		logger.debug("@@@@@ Yay! All Done!");
		logger.debug(runResult);
		return runResult;
	}

	public List<Object> runCollectionTraceAsObject(PostmanCollection collection, PostmanEnvironment env,
			String folderName, boolean haltOnError, CommonConfiguration cfg) throws Exception {
		List<Object> traceList = new ArrayList<>();
		logger.debug("@@@@@ POSTMAN Runner start!");
		PostmanRunResult runResult = new PostmanRunResult();
		PostmanReader reader = new PostmanReader();
		PostmanCollection c = collection;
		c.init();
		PostmanEnvironment e = env;
		e.init();
		PostmanFolder folder = null;
		if (folderName != null && !folderName.isEmpty()) {
			folder = c.folderLookup.get(folderName);
		}

		PostmanVariables var = new PostmanVariables(e);
		PostmanRequestRunner runner = new PostmanRequestRunner(var, haltOnError);
		// boolean isSuccessful = true;
		if (c.folders != null && c.folders.size() > 0) {
			for (PostmanFolder pf : c.folders) {
				traceList = runFolderTraceAsObject(haltOnError, runner, var, c, pf, runResult, cfg);
			}
		} else {
			// Execute all folder all requests
			traceList = runFileTraceAsObject(haltOnError, runner, var, c, runResult, cfg);
			/*
			 * if (haltOnError && !isSuccessful) { return runResult; }
			 */
			/*
			 * for (PostmanFolder pf : c.folders) { isSuccessful =
			 * runFolder(haltOnError, runner, var, c, null, runResult) &&
			 * isSuccessful; if (haltOnError && !isSuccessful) { return
			 * runResult; } }
			 */
		}

		logger.debug("@@@@@ Yay! All Done!");
		logger.debug(runResult);
		return traceList;
	}

	private boolean runFolder(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var,
			PostmanCollection c, PostmanFolder folder, PostmanRunResult runResult) {
		logger.debug("==> POSTMAN Folder: " + folder.name);
		boolean isSuccessful = true;
		for (String reqId : folder.order) {
			runResult.totalRequest++;
			PostmanRequest r = c.requestLookup.get(reqId);
			logger.debug("======> POSTMAN request: " + r.name);
			try {
				boolean runSuccess = runner.run(r, runResult);
				if (!runSuccess) {
					runResult.failedRequest++;
					runResult.failedRequestName.add(folder.name + "." + r.name);
				}
				isSuccessful = runSuccess && isSuccessful;
				if (haltOnError && !isSuccessful) {
					return isSuccessful;
				}
			} catch (Throwable e) {
				logger.error("Exception occurred", e);
				runResult.failedRequest++;
				runResult.failedRequestName.add(folder.name + "." + r.name);
				return false;
			}
		}
		return isSuccessful;
	}

	private List<Object> runFolderTraceAsObject(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var,
			PostmanCollection c, PostmanFolder folder, PostmanRunResult runResult, CommonConfiguration cfg)
			throws ItorixException {
		logger.debug("==> POSTMAN Folder: " + folder.name);
		boolean isSuccessful = true;
		List<Object> traceList = new ArrayList<>();
		for (String reqId : folder.order) {
			runResult.totalRequest++;
			// step 2: create session with filter
			String sessionID = apigeeUtil.createSession(cfg);
			logger.debug("executeCodeCoverage" + cfg.getInteractionid()
					+ "step 2: create session with filter  sessionID ::" + sessionID);
			PostmanRequest r = c.requestLookup.get(reqId);
			logger.debug("======> POSTMAN request: " + r.name);
			String headers = r.headers + "itorix: " + sessionID;
			r.headers = headers;
			try {
				boolean runSuccess = runner.run(r, runResult);
				if (!runSuccess) {
					runResult.failedRequest++;
					runResult.failedRequestName.add(folder.name + "." + r.name);
				}
				JSONArray txIds = null;
				if (sessionID != null) {
					txIds = commonServices.getTransactionIds(cfg, sessionID);
					logger.debug(
							"executeCodeCoverage" + cfg.getInteractionid() + " step4: getTransactionIds ::" + txIds);
				}
				if (cfg.isCodeCoverage()) {
					List<Trace> traces = commonServices.getTransactionData(cfg, sessionID, txIds);
					traceList.addAll(traces);
				}
				if (cfg.isPolicyPerformance()) {
					List<String> tracesObjects = commonServices.getTransactionDataAsString(cfg, sessionID, txIds);
					traceList.addAll(tracesObjects);
				}
				String sessionStatus = commonServices.deleteSession(cfg, sessionID);
				logger.debug("sessionStatus :" + sessionStatus);
				/*
				 * isSuccessful = runSuccess && isSuccessful; if (haltOnError &&
				 * !isSuccessful) { return isSuccessful; }
				 */
			} catch (Throwable e) {
				logger.error("Exception occurred", e);
				runResult.failedRequest++;
				runResult.failedRequestName.add(folder.name + "." + r.name);
				// return false;
			}
		}
		return traceList;
	}

	/*
	 * private boolean runFile(boolean haltOnError, PostmanRequestRunner runner,
	 * PostmanVariables var, PostmanCollection c, PostmanRunResult runResult) {
	 * boolean isSuccessful = true; Map result = new HashMap();
	 * runResult.setResult(result); for (PostmanRequest req : c.requests) {
	 * 
	 * try { boolean runSuccess = runner.run(req, runResult); if (!runSuccess) {
	 * runResult.failedRequest++; runResult.failedRequestName.add(req.name); }
	 * isSuccessful = runSuccess && isSuccessful; if (haltOnError &&
	 * !isSuccessful) { return isSuccessful; } } catch (Throwable e) {
	 * log.error("Exception occurred",e)(); runResult.failedRequest++;
	 * runResult.failedRequestName.add( req.name); return false; }
	 * 
	 * } return isSuccessful; }
	 */

	private boolean runFile(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var, PostmanCollection c,
			PostmanRunResult runResult) {
		boolean isSuccessful = true;
		if (c.order != null && c.order.size() > 0) {
			for (String id : c.order) {
				for (PostmanRequest req : c.requests) {
					if (id.equals(req.id)) {
						try {
							boolean runSuccess = runner.run(req, runResult);
							if (!runSuccess) {
								runResult.failedRequest++;
								runResult.failedRequestName.add(req.name);
							}
							isSuccessful = runSuccess && isSuccessful;
							if (haltOnError && !isSuccessful) {
								return isSuccessful;
							}
						} catch (Throwable e) {
							logger.error("Exception occurred", e);
							runResult.failedRequest++;
							runResult.failedRequestName.add(req.name);
							return false;
						}
					}
				}
			}
		} else {
			for (PostmanRequest req : c.requests) {

				try {
					boolean runSuccess = runner.run(req, runResult);
					if (!runSuccess) {
						runResult.failedRequest++;
						runResult.failedRequestName.add(req.name);
					}
					isSuccessful = runSuccess && isSuccessful;
					if (haltOnError && !isSuccessful) {
						return isSuccessful;
					}
				} catch (Throwable e) {
					logger.error("Exception occurred", e);
					runResult.failedRequest++;
					runResult.failedRequestName.add(req.name);
					return false;
				}
			}
		}
		return isSuccessful;
	}

	private List<Object> runFileTraceAsObject(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var,
			PostmanCollection c, PostmanRunResult runResult, CommonConfiguration cfg) throws ItorixException {
		List<Object> traceList = new ArrayList<>();
		if (c.order != null && c.order.size() > 0) {
			for (String id : c.order) {
				for (PostmanRequest req : c.requests) {
					if (id.equals(req.id)) {

						// step 2: create session with filter
						String sessionID = apigeeUtil.createSession(cfg);
						logger.debug("executeCodeCoverage" + cfg.getInteractionid()
								+ "step 2: create session with filter  sessionID ::" + sessionID);
						String headers = req.headers + "itorix: " + sessionID;
						req.headers = headers;
						try {
							boolean runSuccess = runner.run(req, runResult);
							JSONArray txId = null;
							if (sessionID != null) {
								txId = commonServices.getTransactionIds(cfg, sessionID);
								logger.debug("executeCodeCoverage" + cfg.getInteractionid()
										+ " step4: getTransactionIds ::" + txId);
							}
							if (cfg.isCodeCoverage()) {
								List<Trace> traces = commonServices.getTransactionData(cfg, sessionID, txId);
								traceList.addAll(traces);
							}
							if (cfg.isPolicyPerformance()) {
								List<String> tracesObjects = commonServices.getTransactionDataAsString(cfg, sessionID,
										txId);
								traceList.addAll(tracesObjects);
							}
							String sessionStatus = commonServices.deleteSession(cfg, sessionID);
							logger.debug("sessionStatus :" + sessionStatus);
							if (!runSuccess) {
								runResult.failedRequest++;
								runResult.failedRequestName.add(req.name);
							}
							/*
							 * isSuccessful = runSuccess && isSuccessful; if
							 * (haltOnError && !isSuccessful) { return
							 * isSuccessful; }
							 */
						} catch (Throwable e) {
							logger.error("Exception occurred", e);
							runResult.failedRequest++;
							runResult.failedRequestName.add(req.name);
							// return false;
						}
					}
				}
			}
		} else {
			for (PostmanRequest req : c.requests) {
				// step 2: create session with filter
				String sessionID = apigeeUtil.createSession(cfg);
				logger.debug("executeCodeCoverage" + cfg.getInteractionid()
						+ "step 2: create session with filter  sessionID ::" + sessionID);
				String headers = req.headers + "itorix: " + sessionID;
				req.headers = headers;
				try {
					boolean runSuccess = runner.run(req, runResult);
					JSONArray txIds = null;
					if (sessionID != null) {
						txIds = commonServices.getTransactionIds(cfg, sessionID);
						logger.debug("executeCodeCoverage" + cfg.getInteractionid() + " step4: getTransactionIds ::"
								+ txIds);
					}
					if (cfg.isCodeCoverage()) {
						List<Trace> traces = commonServices.getTransactionData(cfg, sessionID, txIds);
						traceList.addAll(traces);
					}
					if (cfg.isPolicyPerformance()) {
						List<String> tracesObjects = commonServices.getTransactionDataAsString(cfg, sessionID, txIds);
						traceList.addAll(tracesObjects);
					}
					String sessionStatus = commonServices.deleteSession(cfg, sessionID);
					logger.debug("sessionStatus :" + sessionStatus);
					if (!runSuccess) {
						runResult.failedRequest++;
						runResult.failedRequestName.add(req.name);
					}
					/*
					 * isSuccessful = runSuccess && isSuccessful; if
					 * (haltOnError && !isSuccessful) { return isSuccessful; }
					 */
				} catch (Throwable e) {
					logger.error("Exception occurred", e);
					runResult.failedRequest++;
					runResult.failedRequestName.add(req.name);
					// return false;
				}
			}
		}
		return traceList;
	}
}
