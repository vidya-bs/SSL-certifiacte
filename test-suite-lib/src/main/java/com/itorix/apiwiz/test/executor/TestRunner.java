package com.itorix.apiwiz.test.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.itorix.apiwiz.test.api.factory.APIFactory;
import com.itorix.apiwiz.test.component.CancellationExecutor;
import com.itorix.apiwiz.test.dao.TestSuitExecutorSQLDao;
import com.itorix.apiwiz.test.dao.TestSuiteExecutorDao;
import com.itorix.apiwiz.test.db.TestExecutorEntity;
import com.itorix.apiwiz.test.exception.HaltExecution;
import com.itorix.apiwiz.test.executor.beans.*;
import com.itorix.apiwiz.test.executor.model.TenantContext;
import com.itorix.apiwiz.test.executor.validators.JsonValidator;
import com.itorix.apiwiz.test.executor.validators.ResponseValidator;
import com.itorix.apiwiz.test.executor.validators.XmlValidator;
import com.itorix.apiwiz.test.logging.LoggerService;
import com.itorix.apiwiz.test.util.MaskFieldUtil;
import com.itorix.apiwiz.test.util.RSAEncryption;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unused")
@Component
public class TestRunner {

    private final static Logger logger = LoggerFactory.getLogger(TestRunner.class);

    private static final MustacheFactory mf = new DefaultMustacheFactory();

    @Autowired
    private TestSuiteExecutorDao dao;

    @Autowired
    private TestSuitExecutorSQLDao sqlDao;

    @Autowired
    CancellationExecutor cancellationExecutor;

    @Autowired
    LoggerService loggerService;

    public enum API {
        GET, PUT, POST, DELETE, OPTIONS, PATCH;
    }

    public void run(ExecutionContext context) {
        loggerService.logServiceRequest();
        TenantContext.setCurrentTenant(context.getTenant());
        TestSuiteResponse response = context.getTestSuiteResponse();
        TestExecutorEntity testExecutorEntity = context.getTestExecutorEntity();
        TestSuiteResponse finalResponse = null;
        try {
            TestSuite testSuite = dao.getTestSuiteById(response.getTestSuiteId());
            Variables variables = dao.getVariablesById(response.getConfigId());
            finalResponse = executeTestSuite(response.getId(), testSuite, variables, false, false,
                    context.getGlobalTimeout());
            finalResponse.getTestSuite().setExecutionStatus(null);
            if (finalResponse != null) {
                finalResponse.setConfigId(response.getConfigId());
                finalResponse.setCreatedBy(response.getCreatedBy());
                finalResponse.setCts(response.getCts());
                finalResponse.setCounter(response.getCounter());
                finalResponse.setTestSuiteAgent(response.getTestSuiteAgent());
                finalResponse.setUserId(response.getUserId());
            }

            if (!cancellationExecutor.getAndRemoveTestSuiteCancellationId(response.getId())) {
                dao.updateTestSuiteStatus(response.getId(), finalResponse, finalResponse.getStatus());
                sqlDao.updateStatusForTestExecutionId(testExecutorEntity.getTestSuiteExecutionId(),
                        TestExecutorEntity.STATUSES.COMPLETED.getValue());
            }
        } catch (Exception ex) {
            logger.error("exception when executing test runner", ex);
        } finally {
            loggerService.logServiceResponse(finalResponse, context);
        }
    }

    public TestSuiteResponse executeTestSuite(String testSuiteResponseID, TestSuite testSuite, Variables vars,
            Boolean skipAssertions, Boolean isMonitoring, int globalTimeout) {
        List<String> succededScenarios = new ArrayList<String>();
        List<String> failedScenarios = new ArrayList<String>();
        double scenarioSuccessSum = 0.0;

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> encryptedVariables = null;
        List<Header> encryptedVariableHeaders = null;
        MaskFields maskingFields = dao.getMaskFields();
        List<String> maskingFieldsValue = null;
        if (maskingFields != null) {
            maskingFieldsValue = maskingFields.getMaskingFields();
        }

        try {
            Variables clonedVariables = objectMapper.readValue(objectMapper.writeValueAsString(vars), Variables.class);
            encryptedVariables = getGlobalVars(clonedVariables);
            encryptedVariableHeaders = clonedVariables.getVariables();
            for (Header header : encryptedVariableHeaders) {
                if (maskingFieldsValue != null && maskingFieldsValue.contains(header.getName())) {
                    header.setValue(MaskFieldUtil.getMaskedValue(header.getValue()));
                }
            }

            List<Header> headerVariables = vars.getVariables();
            for (Header variable : headerVariables) {
                if (variable.isEncryption()) {
                    variable.setValue((new RSAEncryption()).decryptText(variable.getValue()));
                }
            }

        } catch (Exception e) {
            log.error("Exception occurred",e);
        }

        Map<String, String> globalVars = getGlobalVars(vars);
        // Populate Data for testcase executions
        // TODO: Parallelization based on number of processors available

        if (testSuite.getScenarios() != null) {
            testSuite.getScenarios().parallelStream().forEach(scenario -> {
                if(scenario != null && scenario.getTestCases() != null){
                    scenario.getTestCases().parallelStream().forEach(testCase -> testCase.setStatus("DID NOT EXECUTE")); //Init TestCase Statuses
                }
            });
            for (Scenario scenario : testSuite.getScenarios()) {
                Map<String, Boolean> succededTests = new HashMap<>();
                Map<String, Boolean> failedTests = new HashMap<>();
                try {
                    logger.debug("executing scenario for testSuiteResponseID" + testSuiteResponseID);

                    if (cancellationExecutor.getAndRemoveTestSuiteCancellationId(testSuiteResponseID)) {
                        return new TestSuiteResponse(testSuite.getId(), null, testSuite,
                                TestExecutorEntity.STATUSES.CANCELLED.getValue());
                    }
                    if (scenario != null && scenario.getTestCases() != null) {
                        List<TestCase> testCases = scenario.getTestCases();

                        int numberOfTestCases = testCases.size();
                        int counter = 0;
                        for (; counter < numberOfTestCases; counter++) {
                            TestCase testCase = testCases.get(counter);
                            Long starttime = System.currentTimeMillis();
                            Map<String, Integer> testStatus = new HashMap<String, Integer>();
                            try {
                                if (canExecuteTestCase(testCase, succededTests, failedTests)) {
                                    String sslReference = fillTemplate(testCase.getSslReference(), globalVars);
                                    SSLConnectionSocketFactory sslConnectionFactory = dao
                                            .getSSLConnectionFactory(sslReference);

                                    // creating maskingFields object in case it is null, just to avoid null checks at many
                                    // places
                                    if (maskingFields == null) {
                                        maskingFields = new MaskFields();
                                    }
                                    invokeTestCase(testCase, globalVars, encryptedVariables, maskingFields, testStatus,
                                            skipAssertions, isMonitoring, sslConnectionFactory, globalTimeout);
                                } else {
                                    counter++;
                                    if (counter > testCases.size()) {
                                        counter = 0;
                                    }
                                    continue;
                                }
                                if (testStatus.get("FAIL") == null) {
                                    testCases.get(counter).setStatus("PASS");
                                    succededTests.put(testCase.getName(), true);
                                } else {
                                    testCases.get(counter).setStatus("FAIL");
                                    logger.debug("test case execution failed for {}", testCase.getName());
                                    failedTests.put(testCase.getName(), true);
                                }
                            } catch(HaltExecution ex) {
                                logger.debug("Halting execution of test cases since a test case {} failed. ", testCase.getName());
                                testCase.setStatus("FAIL");
                                testCase.setMessage(ex.getMessage());
                                computeHeaders(testCase.getRequest().getHeaders(), globalVars);
                                failedTests.put(testCase.getName(), true);
                                if(!scenario.isContinueOnError()) {
                                    throw new HaltExecution("Scenario " + scenario.getName() + " is configured to skip upcoming test scenarios");
                                }
                                break;

                            }catch (AssertionError | Exception ex) {
                                logger.error("Error executing {} , {} ", testCase.getName(), ex);
                                testCase.setStatus("FAIL");
                                testCase.setMessage(ex.getMessage());
                                computeHeaders(testCase.getRequest().getHeaders(), globalVars);
                                failedTests.put(testCase.getName(), true);
                                if(!scenario.isContinueOnError()) {
                                    throw new HaltExecution("Scenario " + scenario.getName() + " is configured to skip upcoming test scenarios");
                                }
                            } finally {
                                testCase.setDuration(System.currentTimeMillis() - starttime);
                                if (counter > testCases.size()) {
                                    counter = 0;
                                }
                            }
                        }
                    }
                    if (!failedTests.isEmpty()) {
                        scenario.setStatus("FAIL");
                        failedScenarios.add(scenario.getName());
                    } else {
                        scenario.setStatus("PASS");
                        succededScenarios.add(scenario.getName());
                    }

                    double successRate = 0;

                    if (!CollectionUtils.isEmpty(succededTests)) {
                        successRate = Double
                                .valueOf(((double) succededTests.size() / scenario.getTestCases().size()) * 100);
                    }

                    scenarioSuccessSum += successRate;
                    scenario.setSuccessRate((int) Math.round(successRate));
                } catch(Exception ex) {
                    if(ex instanceof HaltExecution){
                        logger.debug("Skipping processing of remaining scenarios. To process the remaining scenarios modify continueOnError flag on scenario {} ", scenario.getName());
                    }else{
                        logger.debug("An Error occurred while processing scenario {} : {}",scenario.getName(),ex.getMessage());
                    }

                    scenario.setStatus("FAIL");
                    failedScenarios.add(scenario.getName());
                    double successRate = 0;
                    if (!CollectionUtils.isEmpty(succededTests)) {
                        successRate = Double
                                .valueOf(((double) succededTests.size() / scenario.getTestCases().size()) * 100);
                    }
                    scenarioSuccessSum += successRate;
                    scenario.setSuccessRate((int) Math.round(successRate));
                    break;
                }
            }

            if (failedScenarios.isEmpty()) {
                testSuite.setStatus("PASS");
            } else {
                logger.debug(" number of failedscenarios in testSuiteResponseID {} , {} ", testSuiteResponseID,
                        failedScenarios.size());
                testSuite.setStatus("FAIL");
            }

            // for (Scenario scenario : testSuite.getScenarios()) {
            // clonedVariables
            // }

            // setting encrypted values which will be added to the testSuite global level
            if (testSuite.getScenarios() != null) {
                for (Scenario scenario : testSuite.getScenarios()) {
                    if (scenario != null && scenario.getTestCases() != null) {
                        List<TestCase> testCases = scenario.getTestCases();
                        for (TestCase testCase : testCases) {
                            if (testCase.getResponse() != null) {
                                List<Variable> variables = testCase.getResponse().getVariables();
                                String value = null;
                                for (Variable variable : variables) {
                                    value = encryptedVariables.get(variable.getName());
                                    if (!CollectionUtils.isEmpty(maskingFieldsValue)
                                            && maskingFieldsValue.contains(variable.getName())
                                            && StringUtils.hasText(value)) {
                                        value = MaskFieldUtil.getMaskedValue(value);
                                    }
                                    Header header = new Header(variable.getName(), "", value, variable.isEncryption());
                                    encryptedVariableHeaders.add(header);
                                }
                            }
                        }
                    }
                }
            }

            testSuite.setSuccessRate(
                    (int) Math.round(Double.valueOf(scenarioSuccessSum / testSuite.getScenarios().size())));
            testSuite.setVars(encryptedVariableHeaders);
            testSuite.setDate(java.time.LocalDateTime.now().toString());
        }
        return new TestSuiteResponse(testSuite.getId(), null, testSuite,
                TestExecutorEntity.STATUSES.COMPLETED.getValue());
    }

    // public TestSuiteResponse executeTestSuiteForCodeCoverage(TestSuite testSuite, Variables vars) {
    // return executeTestSuite(testSuite, vars, true, false);
    // }
    //
    // public TestSuiteResponse executeTestSuiteForMonitor(TestSuite testSuite, Variables vars) {
    // return executeTestSuite(testSuite, vars, true, true);
    // }

    /**
     *
     * Usecases to be covered 1. No dependencies - First Time Execution - Loop Execution 2. Having Dependencies - First
     * Time Execution - Loop Execution 3. Dependent testcase passed 4. Dependent testcase failed 5. Dependent testcase
     * didn't execute
     *
     * @param testCase
     * @param succededTests
     * @param failedTests
     * 
     * @return
     * 
     * @throws Exception
     */
    public boolean canExecuteTestCase(TestCase testCase, Map<String, Boolean> succededTests,
            Map<String, Boolean> failedTests) throws Exception {
        if (testCase.getDependsOn() == null) {
            if (succededTests.get(testCase.getName()) != null || failedTests.get(testCase.getName()) != null) {
                return false;
            }
        } else {
            if (succededTests.get(testCase.getDependsOn()) != null) {
                if (succededTests.get(testCase.getName()) != null || failedTests.get(testCase.getName()) != null) {
                    return false;
                }
            } else if (failedTests.get(testCase.getDependsOn()) != null) {
                throw new Exception(
                        testCase.getDependsOn() + " testcase failed... Can't be able execute " + testCase.getName());
            } else {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> getGlobalVars(Variables vars) {
        return computeHeaders(vars.getVariables(), null);
    }

    public void invokeTestCase(TestCase testCase, Map<String, String> globalVars,
            Map<String, String> encryptedVariables, MaskFields maskingFields, Map<String, Integer> testStatus,
            Boolean skipAssertion, Boolean isMonitoring, SSLConnectionSocketFactory sslConnectionFactory,
            int globalTimeout) throws Exception {
        // Replace all parameters with Template framework
        if (testCase.getVerb() != null) {
            if (isMonitoring && !testCase.isMonitored()) {
                return;
            }

            ObjectMapper mapper = new ObjectMapper();

            List<QueryParam> clonedQueryParams;
            if (!CollectionUtils.isEmpty(testCase.getRequest().getQueryParams())) {
                clonedQueryParams = mapper.readValue(mapper.writeValueAsString(testCase.getRequest().getQueryParams()),
                        new TypeReference<List<QueryParam>>() {
                        });

                maskingFields.getMaskingFields().forEach(s -> {
                    clonedQueryParams.forEach(m -> {
                        if (m.getValue().equals("{{" + s + "}}")) {
                            m.setValue(MaskFieldUtil.getMaskedValue(globalVars.get(s)));
                        }
                    });
                });
            } else {
                clonedQueryParams = null;
            }

            testCase.setPath(fillTemplate(testCase.getPath(), globalVars));
            String path = testCase.getPath();
            String clonedPath = new String(path);
            path = computeQueryParams(path, testCase.getRequest().getQueryParams(), globalVars);

            testCase.setSchemes(fillTemplate(testCase.getSchemes(), globalVars));
            testCase.setHost(fillTemplate(testCase.getHost(), globalVars));
            testCase.setPort(fillTemplate(testCase.getPort(), globalVars));

            path = testCase.getSchemes() + "://" + testCase.getHost() + ":" + testCase.getPort() + path;
            int timeout = testCase.getTimeout() != null && testCase.getTimeout() > 0 ? testCase.getTimeout().intValue()
                    : globalTimeout;
            if (path != null) {
                testCase.setPath(path);
            }

            // sending cloned headers which are in decrypted format
            // Later updating the test case header based on type isEncryptionFlag
            List<Header> header = testCase.getRequest().getHeaders();
            List<Header> clonedHeaders = mapper.readValue(mapper.writeValueAsString(header),
                    new TypeReference<List<Header>>() {
                    });

            Map<String, String> headers = computeHeaders(clonedHeaders, globalVars);
            // String bodyType = addBodyHeader(testCase);
            HttpResponse response = null;
            String reqBody = null;

            if (testCase.getRequest() != null && testCase.getRequest().getBody() != null
                    && testCase.getRequest().getBody().getData() != null) {
                reqBody = fillTemplate(testCase.getRequest().getBody().getData(), globalVars);
                String reqBodyToSet = fillTemplate(testCase.getRequest().getBody().getData(), encryptedVariables);
                if (maskingFields != null && !maskingFields.getMaskingFields().isEmpty()) {
                    reqBodyToSet = MaskFieldUtil.getMaskedResponseForJson(maskingFields.getMaskingFields(),
                            reqBodyToSet);
                }

                testCase.getRequest().getBody().setData(reqBodyToSet);
            }

            if (testCase.getVerb().equalsIgnoreCase(API.GET.toString())) {
                response = APIFactory.invokeGet(path, headers, testCase.getName(), sslConnectionFactory, timeout);
            } else if (testCase.getVerb().equalsIgnoreCase(API.POST.toString())) {

                String content = null;
                if (testCase.getRequest() != null && (!CollectionUtils.isEmpty(testCase.getRequest().getFormParams())
                        || !CollectionUtils.isEmpty(testCase.getRequest().getFormURLEncoded()))) {

                    List<NameValuePair> generateNameValuePairs = null;
                    if (!CollectionUtils.isEmpty(testCase.getRequest().getFormParams())) {
                        content = "multi-part";
                        List<FormParam> formParamClone = mapper.readValue(
                                mapper.writeValueAsString(testCase.getRequest().getFormParams()),
                                new TypeReference<List<FormParam>>() {
                                });

                        generateNameValuePairs = generateNameValuePairs(formParamClone, globalVars);

                        maskingFields.getMaskingFields().forEach(s -> {
                            testCase.getRequest().getFormParams().forEach(m -> {
                                if (m.getValue().equals("{{" + s + "}}")) {
                                    m.setValue(MaskFieldUtil.getMaskedValue(globalVars.get(s)));
                                }
                            });
                        });

                        generateNameValuePairs(testCase.getRequest().getFormParams(), encryptedVariables);
                    } else {
                        content = "form-url-encoded";
                        List<FormParam> formParamClone = mapper.readValue(
                                mapper.writeValueAsString(testCase.getRequest().getFormURLEncoded()),
                                new TypeReference<List<FormParam>>() {
                                });

                        generateNameValuePairs = generateNameValuePairs(formParamClone, globalVars);

                        generateNameValuePairs = generateNameValuePairs(testCase.getRequest().getFormURLEncoded(),
                                encryptedVariables);

                        maskingFields.getMaskingFields().forEach(s -> {
                            testCase.getRequest().getFormParams().forEach(m -> {
                                if (m.getValue().equals("{{" + s + "}}")) {
                                    m.setValue(MaskFieldUtil.getMaskedValue(globalVars.get(s)));
                                }
                            });
                        });

                        generateNameValuePairs(testCase.getRequest().getFormParams(), encryptedVariables);

                    }
                    response = APIFactory.invokePost(path, headers, generateNameValuePairs, content, reqBody,
                            testCase.getName(), sslConnectionFactory, timeout);
                } else {
                    response = APIFactory.invokePost(path, headers, null, content, reqBody, testCase.getName(),
                            sslConnectionFactory, timeout);
                }
            } else if (testCase.getVerb().equalsIgnoreCase(API.PUT.toString())) {

                String contentType = null;
                if (testCase.getRequest() != null && (!CollectionUtils.isEmpty(testCase.getRequest().getFormParams())
                        || !CollectionUtils.isEmpty(testCase.getRequest().getFormURLEncoded()))) {

                    List<NameValuePair> generateNameValuePairs = null;

                    if (!CollectionUtils.isEmpty(testCase.getRequest().getFormParams())) {
                        contentType = "multi-part";
                        List<FormParam> formParamClone = mapper.readValue(
                                mapper.writeValueAsString(testCase.getRequest().getFormParams()),
                                new TypeReference<List<FormParam>>() {
                                });

                        generateNameValuePairs = generateNameValuePairs(formParamClone, globalVars);

                        maskingFields.getMaskingFields().forEach(s -> {
                            testCase.getRequest().getFormParams().forEach(m -> {
                                if (m.getValue().equals("{{" + s + "}}")) {
                                    m.setValue(MaskFieldUtil.getMaskedValue(globalVars.get(s)));
                                }
                            });
                        });

                        generateNameValuePairs(testCase.getRequest().getFormParams(), encryptedVariables);
                    } else {
                        contentType = "form-url-encoded";
                        List<FormParam> formParamClone = mapper.readValue(
                                mapper.writeValueAsString(testCase.getRequest().getFormURLEncoded()),
                                new TypeReference<List<FormParam>>() {
                                });

                        generateNameValuePairs = generateNameValuePairs(formParamClone, globalVars);

                        generateNameValuePairs = generateNameValuePairs(testCase.getRequest().getFormURLEncoded(),
                                encryptedVariables);

                        maskingFields.getMaskingFields().forEach(s -> {
                            testCase.getRequest().getFormParams().forEach(m -> {
                                if (m.getValue().equals("{{" + s + "}}")) {
                                    m.setValue(MaskFieldUtil.getMaskedValue(globalVars.get(s)));
                                }
                            });
                        });

                        generateNameValuePairs(testCase.getRequest().getFormParams(), encryptedVariables);

                    }

                    response = APIFactory.invokePut(path, headers, generateNameValuePairs, contentType, reqBody,
                            testCase.getName(), sslConnectionFactory, timeout);
                } else {
                    response = APIFactory.invokePut(path, headers, null, contentType, reqBody, testCase.getName(),
                            sslConnectionFactory, timeout);
                }

            } else if (testCase.getVerb().equalsIgnoreCase(API.DELETE.toString())) {
                response = APIFactory.invokeDelete(path, headers, reqBody, testCase.getName(), sslConnectionFactory,
                        timeout);
            } else if (testCase.getVerb().equalsIgnoreCase(API.OPTIONS.toString())) {
                response = APIFactory.invokeOptions(path, headers, reqBody, testCase.getName(), sslConnectionFactory,
                        timeout);
            } else if (testCase.getVerb().equalsIgnoreCase(API.PATCH.toString())) {
                response = APIFactory.invokePatch(path, headers, reqBody, testCase.getName(), sslConnectionFactory,
                        timeout);
            }

            // masking and encrypting path
            if (clonedQueryParams != null) {
                clonedPath = computeQueryParams(clonedPath, clonedQueryParams, encryptedVariables);
                testCase.getRequest().setQueryParams(clonedQueryParams);
                clonedPath = testCase.getSchemes() + "://" + testCase.getHost() + ":" + testCase.getPort() + clonedPath;
                if (clonedPath != null) {
                    testCase.setPath(clonedPath);
                }
            }

            // testCase.getResponse().setHeaders(response.getAllHeaders());
            validateResponse(response, testCase.getResponse(), testCase.getName(), globalVars, encryptedVariables,
                    testStatus, skipAssertion, maskingFields);
            replaceResponseVariables(testCase.getResponse(), response, maskingFields);
            // masking request header parameters and response variables
            computeMaskingFields(globalVars, maskingFields, header);

            // updating request header for isEncryptionFlag
            computeHeaders(header, encryptedVariables);

            testCase.getResponse().getHeaders().entrySet().forEach(k -> {
                if (maskingFields.getMaskingFields().contains(k.getKey())) {
                    k.setValue(MaskFieldUtil.getMaskedValue(k.getValue()));
                }
            });

        }
    }

    private void computeMaskingFields(Map<String, String> globalVars, MaskFields maskingFields, List<Header> header) {
        maskingFields.getMaskingFields().forEach(s -> {
            if(header != null && header.size() > 0) {
                header.forEach(m -> {
                    if (m.getValue().equals("{{" + s + "}}")) {
                        m.setValue(MaskFieldUtil.getMaskedValue(globalVars.get(s)));
                    }
                });
            }
        });
    }

    private void fillMustacheInfo(Response response, Map<String, String> globalVars) {
        if (response.getAssertions() != null && response.getAssertions().getBody() != null) {
            for (ResponseBodyValidation validation : response.getAssertions().getBody()) {
                validation.setValue(fillTemplate(validation.getValue().toString(), globalVars));
            }
        }
    }

    private List<NameValuePair> generateNameValuePairs(List<FormParam> formParams, Map<String, String> globalVars) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (formParams != null) {
            for (FormParam formParam : formParams) {
                formParam.setValue(fillTemplate(formParam.getValue(), globalVars));
                params.add(new BasicNameValuePair(formParam.getName(), formParam.getValue()));
            }
        }
        return params;
    }

    private Result validateResponse(HttpResponse actualResponse, Response expectedResponse, String testCaseId,
            Map<String, String> globalVars, Map<String, String> encryptedVariables, Map<String, Integer> testStatus,
            Boolean skipAssertions, MaskFields maskingFields) throws Exception {
        if (actualResponse == null) {
            throw new Exception("Response is null");
        }
        if (expectedResponse == null) {
            return new Result(testCaseId, "Passed", null);
        }

        ResponseManager responseManager = new ResponseManager();
        ResponseValidator validator = responseManager.gatherResponseData(actualResponse, expectedResponse, globalVars,
                encryptedVariables, maskingFields);

        fillMustacheInfo(expectedResponse, globalVars);

        if (skipAssertions) {
            return new Result(testCaseId, "Passed", null);
        }
        // validate status
        ResponseValidator.validateStatusAndMessage(actualResponse, expectedResponse, testStatus);

        // validate headers
        Map<String, String> responseHeaders = new HashMap<String, String>();

        if (expectedResponse != null && expectedResponse.getAssertions() != null
                && expectedResponse.getAssertions().getHeaders() != null) {
            expectedResponse.getAssertions().getHeaders()
                    .forEach((header) -> responseHeaders.put(header.getName(), header.getValue()));
            ResponseValidator.isValidHeaders(actualResponse.getAllHeaders(),
                    expectedResponse.getAssertions().getHeaders(), testStatus);
        }

        // Validate body
        if (actualResponse.getHeaders("Content-Type").length > 0
                || (expectedResponse.getBody() != null && expectedResponse.getBody().getType() != null)) {
            if (expectedResponse.getBody() != null && expectedResponse.getBody().getData() != null
                    && !expectedResponse.getBody().getData().isEmpty()) {
                if (actualResponse.getHeaders("Content-Type")[0].getValue().toLowerCase().contains("json")
                        || (expectedResponse.getBody() != null
                                && expectedResponse.getBody().getType().toLowerCase().contains("json"))) {
                    new JsonValidator(expectedResponse.getBody().getData()).validate(expectedResponse.getAssertions(),
                            testStatus);
                } else if (actualResponse.getHeaders("Content-Type")[0].getValue().toLowerCase().contains("xml")
                        || (expectedResponse.getBody() != null
                                && expectedResponse.getBody().getType().toLowerCase().contains("xml"))) {
                    new XmlValidator(expectedResponse.getBody().getData()).validate(expectedResponse.getAssertions(),
                            testStatus);
                }
            }
        }

        try {
            if(validator != null) {
                expectedResponse.getBody().setData(validator.getUpdatedObjectAsString());
                if (maskingFields != null && !CollectionUtils.isEmpty(maskingFields.getMaskingFields())) {
                    expectedResponse.getBody().setData(validator.getMaskedResponse(maskingFields.getMaskingFields()));
                }
            }

        } catch (HaltExecution e) {
            throw new HaltExecution(e.getMessage());
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // Check for Accept Headers to invoke xml or json validations
        return new Result(testCaseId, "Passed", null);
    }

    public Map<String, String> computeHeaders(List<Header> headers, Map<String, String> globalVars) {
        Map<String, String> headerMap = new HashMap<String, String>();
        if (null == headers) {
            return headerMap;
        }
        for (Header header : headers) {
            header.setValue(fillTemplate(header.getValue(), globalVars));
            headerMap.put(header.getName(), header.getValue());
        }
        return headerMap;
    }

    private String fillTemplate(String input, Map<String, String> vars) {
        if (input == null) {
            return null;
        }
        if (vars == null) {
            return input;
        }
        Writer writer = new StringWriter();
        Mustache mustache = mf.compile(new StringReader(input), "headers");
        mustache.execute(writer, vars);
        return writer.toString();
    }

    private String computeQueryParams(String path, List<QueryParam> queryParams, Map<String, String> globalVars) {
        if (path != null) {
            if (!path.contains("?")) {
                path = path + "?";
            }
        } else {
            path = "?";
        }
        if (queryParams != null) {
            for (QueryParam param : queryParams) {
                param.setValue(fillTemplate(param.getValue(), globalVars));
                path = path + "&" + param.getName() + "=" + param.getValue();
            }
        } else {
            if (path.endsWith("?")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    private void replaceResponseVariables(Response response, HttpResponse httpResponse, MaskFields maskingFields) {

        ResponseValidator validator = null;
        if (response.getBody() != null && response.getBody().getType() != null) {
            if (response.getBody() != null && response.getBody().getData() != null
                    && !response.getBody().getData().isEmpty()) {
                if (response.getBody().getType().equalsIgnoreCase("json")) {
                    validator = new JsonValidator(response.getBody().getData());
                }
                if (response.getBody().getType().equalsIgnoreCase("xml")) {
                    try {
                        validator = new XmlValidator(response.getBody().getData());
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        log.error("Exception occurred",e);
                    }
                }
            }
        }

        Map<String, String> headerMap = response.getHeaders();
        if (response.getVariables() != null) {
            for (Variable variable : response.getVariables()) {
                String value = null;
                if (variable.getReference() != null) {
                    if (headerMap != null && variable.getReference().equalsIgnoreCase("headers")) {
                        value = headerMap.get(variable.getValue());
                    } else if (variable.getReference().equalsIgnoreCase("body")) {
                        try {
                            if (validator != null) {
                                value = validator.getAttributeValue(variable.getValue()).toString();
                            }
                        } catch (Exception e) {
                            log.error("Exception occurred",e);
                        }
                    }
                    if (variable.getReference().equalsIgnoreCase("status")) {
                        if (variable.getValue().equals("code")) {
                            value = Integer.toString(httpResponse.getStatusLine().getStatusCode());
                        } else {
                            value = httpResponse.getStatusLine().getReasonPhrase();
                        }
                    }
                }

                if (!StringUtils.isEmpty(value)) {
                    if (variable.isEncryption()) {
                        try {
                            value = (new RSAEncryption()).encryptText(value);
                            variable.setRunTimevalue(value);
                        } catch (Exception e) {
                            logger.error("error while encrypting value");
                        }
                    } else if (maskingFields.getMaskingFields().contains(variable.getName())) {
                        variable.setRunTimevalue(MaskFieldUtil.getMaskedValue(value));
                    } else {
                        variable.setRunTimevalue(value);
                    }

                }
            }
        }

    }
}
