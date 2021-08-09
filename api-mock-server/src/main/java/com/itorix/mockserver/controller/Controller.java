package com.itorix.mockserver.controller;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.itorix.mockserver.common.model.MockLog;
import com.itorix.mockserver.common.model.MockRequest;
import com.itorix.mockserver.common.model.MockResponse;
import com.itorix.mockserver.common.model.expectation.*;
import com.itorix.mockserver.common.model.expectation.Body.Type;
import com.itorix.mockserver.dao.MockServerDao;
import com.itorix.mockserver.helper.MockValidator;
import com.itorix.mockserver.logging.MockLogger;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.*;

@CrossOrigin
@RestController
@Component
@Slf4j
public class Controller {

    private static final MustacheFactory mf = new DefaultMustacheFactory();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    MockValidator mockValidator;

    @Autowired
    private Tracer tracer;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockServerDao mockServerDao;

    @Autowired
    MockLogger mockLogger;

    private String EXPECTATION_NOT_FOUND = "No Expectations Available";

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/**", consumes = "multipart/form-data")
    public ResponseEntity serviceFormParams(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            @RequestParam(required = false) MultiValueMap<String, String> requestParams,
            @PathVariable(required = false) String pathVariables,
            @RequestHeader(required = false) MultiValueMap<String, String> headerVariables) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
        Collection<Part> parts;
        try {
            parts = httpServletRequest.getParts();
            for (Part part : parts) {
                if (!formData.containsKey(part.getName())) {
                    formData.put(part.getName(), new ArrayList<>());
                }

                List<String> list = formData.get(part.getName());
                list.add(getPartValue(part.getInputStream()));
                formData.put(part.getName(), list);
            }

        } catch (IOException | ServletException e) {
            log.error("could not get parts from multi-part", e);
        }
        MultiValueMap<String, String> queryParamsForLog = getQueryParams(httpServletRequest);
        return validateRequest(httpServletRequest, requestParams, "", headerVariables, formData, null,
                queryParamsForLog);
    }

    private MultiValueMap<String, String> getQueryParams(HttpServletRequest httpServletRequest) {

        if (StringUtils.isEmpty(httpServletRequest.getQueryString())) {
            return null;
        }

        MultiValueMap<String, String> queryParamsForLog = new LinkedMultiValueMap<String, String>();
        final String[] splitQuery = httpServletRequest.getQueryString().split("&");
        for (final String query : splitQuery) {

            final String[] arrTempParameter = query.split("=");

            if (arrTempParameter.length >= 2) {
                final String parameterKey = arrTempParameter[0];
                final String parameterValue = arrTempParameter[1];
                queryParamsForLog.add(parameterKey, parameterValue);
            } else {
                final String parameterKey = arrTempParameter[0];
                queryParamsForLog.add(parameterKey, "");
            }
        }
        return queryParamsForLog;
    }

    private String getPartValue(InputStream stream) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("could not get parts value", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("could not get parts value", e);
                }
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/**", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity serviceURLEncode(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            @RequestParam(required = false) MultiValueMap<String, String> requestParams,
            @PathVariable(required = false) String pathVariables,
            @RequestBody(required = false) MultiValueMap<String, String> formParam,
            @RequestHeader(required = false) MultiValueMap<String, String> headerVariables) {
        MultiValueMap<String, String> urlForms = null;
        if (!CollectionUtils.isEmpty(formParam)) {
            urlForms = formParam;
            if (!CollectionUtils.isEmpty(requestParams)) {
                urlForms.putAll(requestParams);
            }
        } else {
            urlForms = requestParams;
        }

        return validateRequest(httpServletRequest, requestParams, "", headerVariables, null, urlForms, null);
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/**")
    public ResponseEntity service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            @RequestParam(required = false) MultiValueMap<String, String> requestParams,
            @RequestBody(required = false) String requestBody, @PathVariable(required = false) String pathVariables,
            @RequestHeader(required = false) MultiValueMap<String, String> headerVariables) {

        return validateRequest(httpServletRequest, requestParams, requestBody, headerVariables, null, null, null);
    }

    private ResponseEntity validateRequest(HttpServletRequest httpServletRequest,
            MultiValueMap<String, String> requestParams, String requestBody,
            MultiValueMap<String, String> headerVariables, MultiValueMap<String, String> formParam,
            MultiValueMap<String, String> urlEncodedParam, MultiValueMap<String, String> queryParamsForLog) {
        Expectation matchedExpectation = null;
        String path = null;
        ResponseEntity<String> response = null;
        HttpHeaders responseHeaders = new HttpHeaders();
        String responseBody = null;
        List<ExpectationResponsePathFound> expectationResponsePathFound = new ArrayList<>();
        try {
            path = httpServletRequest.getPathInfo() != null && httpServletRequest.getContextPath() != null
                    ? httpServletRequest.getPathInfo()
                    : java.net.URLDecoder.decode(httpServletRequest.getRequestURI(), "UTF-8");

            List<Criteria> listOfOr = new ArrayList<>();
            String[] pathSplit = Arrays.stream(path.split("/")).filter(s -> !StringUtils.isEmpty(s))
                    .toArray(String[]::new);
            ;

            for (int i = 0; i < pathSplit.length; i++) {
                if (StringUtils.isEmpty(pathSplit[i])) {
                    continue;
                }
                listOfOr.add(new Criteria().orOperator(Criteria.where("pathArray." + i).is(pathSplit[i]),
                        Criteria.where("pathArray." + i).is("*"), Criteria.where("pathArray." + i).exists(false)));
            }

            Criteria criteria = new Criteria().andOperator(listOfOr.toArray(new Criteria[listOfOr.size()]));
            Query query = new Query(criteria);

            log.debug("connected to db {}", mongoTemplate.getDb().getName());
            log.debug("query formed {}", query.toString());
            List<Expectation> expectations = mongoTemplate.find(query, Expectation.class);
            List<Expectation> orderedExpectation = getExpectationByPriority(expectations, path);

            log.debug("matched expectation count {}", orderedExpectation.size());

            for (Expectation expectation : orderedExpectation) {
                List<String> errorResponse = new ArrayList<>();
                boolean matchFound = true;
                if (mockValidator.chechPath(expectation, path)) {

                    if (!mockValidator.checkMethod(expectation, httpServletRequest.getMethod())) {
                        matchFound = false;
                        errorResponse.add("method didn't match");
                    } else {
                        errorResponse.add("method matched");
                    }

                    if (!mockValidator.checkQueryString(expectation, requestParams)) {
                        matchFound = false;
                        errorResponse.add("query didn't match");
                    } else {
                        errorResponse.add("query matched");
                    }

                    if (!mockValidator.checkHeader(expectation, headerVariables)) {
                        matchFound = false;
                        errorResponse.add("header didn't match");
                    } else {
                        errorResponse.add("header matched");
                    }

                    if (!mockValidator.checkCookie(expectation, httpServletRequest.getCookies())) {
                        matchFound = false;
                        errorResponse.add("cookie didn't match");
                    } else {
                        errorResponse.add("cookie matched");
                    }

                    if (!mockValidator.checkBody(expectation, requestBody, formParam, urlEncodedParam)) {
                        matchFound = false;
                        errorResponse.add("body didn't match");
                    } else {
                        errorResponse.add("body matched");
                    }

                    if (matchFound) {
                        matchedExpectation = expectation;
                        break;
                    } else {
                        ExpectationResponsePathFound mismatchExp = new ExpectationResponsePathFound();
                        mismatchExp.setExpectationId(expectation.getId());
                        mismatchExp.setExpectationName(expectation.getName());
                        mismatchExp.setGroupName(mockServerDao.getGroupName(expectation.getGroupId()));
                        mismatchExp.setReason(String.join(",", errorResponse));
                        expectationResponsePathFound.add(mismatchExp);
                    }

                }
            }

            // collect variables
            if (matchedExpectation != null) {
                List<Variable> variables = matchedExpectation.getRequest().getVariables();
                Map<String, String> variableExtract = extractVariables(requestBody, headerVariables, requestParams,
                        matchedExpectation, path, variables, formParam);

                responseBody = fillTemplate(matchedExpectation.getResponse().getBody(), variableExtract);

                matchedExpectation.getResponse().getHeaders().forEach((k, v) -> {
                    responseHeaders.add(fillTemplate(k, variableExtract), fillTemplate(v, variableExtract));
                });

                if (StringUtils.hasText(matchedExpectation.getResponse().getCookies())) {
                    responseHeaders.add("Set-Cookie", matchedExpectation.getResponse().getCookies());
                }

                response = ResponseEntity.status(matchedExpectation.getResponse().getStatusCode())
                        .headers(responseHeaders).body(responseBody);

                return response;
            }

        } catch (UnsupportedEncodingException e) {
            log.error("error thrown when getting the path", e);
        } finally {
            MockLog mockLog = new MockLog();
            mockLog.setClientIp(httpServletRequest.getRemoteAddr());
            mockLog.setPath(path);
            mockLog.setTraceId(tracer.currentSpan().context().traceId());

            try {
                if (matchedExpectation != null) {
                    mockLog.setWasMatched(true);
                    mockLog.setExpectationId(matchedExpectation.getId());
                    mockLog.setExpectationName(matchedExpectation.getName());
                    mockLog.setGroupId(matchedExpectation.getGroupId());
                    mockLog.setGroupName(mockServerDao.getGroupName(matchedExpectation.getGroupId()));
                }
                try {
                    if (matchedExpectation != null) {
                        mockLog.setHttpResponse(objectMapper.writeValueAsString(new MockResponse(responseHeaders,
                                responseBody, matchedExpectation.getResponse().getStatusCode(),
                                matchedExpectation.getResponse().getStatusMessage())));
                        mockLog.setLoggedTime(System.currentTimeMillis());
                    } else {
                        String responseBodyStr = objectMapper.writeValueAsString(expectationResponsePathFound);
                        mockLog.setHttpResponse(objectMapper.writeValueAsString(new MockResponse(responseHeaders,
                                responseBodyStr, HttpStatus.NOT_FOUND.value(), "notfound")));
                        mockLog.setLoggedTime(System.currentTimeMillis());
                    }
                } catch (Exception ex) {
                    log.error("exception in mock server", ex);
                }
                MockRequest mockRequest = new MockRequest();
                mockRequest.setCookie(httpServletRequest.getCookies());
                mockRequest.setHeaders(headerVariables);
                if (formParam != null) {
                    mockRequest.setRequestParams(queryParamsForLog);
                    mockRequest.setFormParams(formParam);
                } else {
                    mockRequest.setRequestParams(requestParams);
                }

                mockRequest.setMethod(httpServletRequest.getMethod());
                mockRequest.setPath(path);
                mockRequest.setBody(requestBody);
                mockLog.setHttpRequest(objectMapper.writeValueAsString(mockRequest));

                mockLogger.info(mockLogger.getLogData(mockLog));
                mockServerDao.addLogEntry(mockLog);
            } catch (Exception e) {
                log.error("error when storing mocklog", e);
            }
        }

        if (!expectationResponsePathFound.isEmpty()) {
            return new ResponseEntity(expectationResponsePathFound, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(new ExpectationResPathNotFound(EXPECTATION_NOT_FOUND), HttpStatus.NOT_FOUND);
        }

    }

    private Map<String, String> extractVariables(String requestBody, MultiValueMap<String, String> headerVariables,
            MultiValueMap<String, String> requestParams, Expectation matchedExpectation, String path,
            List<Variable> variables, MultiValueMap<String, String> formParam) {

        Map<String, String> variableExtract = new HashMap<>();
        for (Variable variable : variables) {
            try {
                if (variable.getRef().equals(Variable.Ref.header)) {
                    List<String> variableValueList = headerVariables.get(variable.getPath());
                    if (!CollectionUtils.isEmpty(variableValueList)) {
                        variableExtract.put(variable.getName(), String.join(",", variableValueList));
                    }
                } else if (variable.getRef().equals(Variable.Ref.path)) {

                    String expectationPath = matchedExpectation.getRequest().getPath().getValue();
                    expectationPath = expectationPath.endsWith("/")
                            ? expectationPath.substring(0, expectationPath.length() - 1) : expectationPath;
                    String actualPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

                    Map<String, String> extractUriTemplateVariables = new AntPathMatcher()
                            .extractUriTemplateVariables(expectationPath, actualPath);
                    if (extractUriTemplateVariables.containsKey(variable.getPath())) {
                        variableExtract.put(variable.getName(), extractUriTemplateVariables.get(variable.getPath()));
                    } else if (extractUriTemplateVariables.containsKey("{" + variable.getPath() + "}")) {
                        variableExtract.put(variable.getName(),
                                extractUriTemplateVariables.get("{" + variable.getPath() + "}"));
                    } else if (extractUriTemplateVariables.containsKey("{{" + variable.getPath() + "}}")) {
                        variableExtract.put(variable.getName(),
                                extractUriTemplateVariables.get("{{" + variable.getPath() + "}}"));
                    }

                } else if (variable.getRef().equals(Variable.Ref.queryParams)) {
                    List<String> variableValueList = requestParams.get(variable.getPath());
                    if (!CollectionUtils.isEmpty(variableValueList)) {
                        variableExtract.put(variable.getName(), String.join(",", variableValueList));
                    }
                } else if (variable.getRef().equals(Variable.Ref.body)) {
                    Type type = null;
                    if (matchedExpectation.getRequest().getBody() != null
                            && matchedExpectation.getRequest().getBody().getType() != null) {
                        type = matchedExpectation.getRequest().getBody().getType();
                    } else {
                        if (headerVariables.containsKey("content-type")) {
                            if (headerVariables.get("content-type").contains("application/json")) {
                                type = Body.Type.json;
                            } else if (headerVariables.get("content-type").contains("application/xml")) {
                                type = Body.Type.xml;
                            } else if (!CollectionUtils.isEmpty(formParam)) {
                                type = Body.Type.formParams;
                            }
                        }
                    }
                    if (type != null) {
                        if (type.equals(Body.Type.json)) {
                            addJsonVariable(requestBody, variableExtract, variable);
                        } else if (type.equals(Body.Type.xml)) {
                            addXMLVariable(requestBody, variableExtract, variable);
                        } else if (type.equals(Body.Type.formParams) || type.equals(Body.Type.formURLEncoded)) {
                            addFormParamVariables(formParam, variableExtract, variable);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception occured while extracting variable ", e);
            }
        }
        return variableExtract;
    }

    private void addFormParamVariables(MultiValueMap<String, String> formParam, Map<String, String> variableExtract,
            Variable variable) {
        if (formParam.containsKey(variable.getPath()) && !CollectionUtils.isEmpty(formParam.get(variable.getName()))) {
            variableExtract.put(variable.getName(), String.join(",", formParam.get(variable.getName())));
        }
    }

    private void addXMLVariable(String requestBody, Map<String, String> variableExtract, Variable variable) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String value;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(requestBody)));
            value = XPathFactory.newInstance().newXPath().evaluate(variable.getPath() + "/text()", document);
            variableExtract.put(variable.getName(), value);
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
            log.info("error occured during xml path reading");
        }
    }

    private void addJsonVariable(String requestBody, Map<String, String> variableExtract, Variable variable) {
        DocumentContext context = JsonPath.parse(requestBody);
        Object readValue = context.read(variable.getPath());
        variableExtract.put(variable.getName(), "" + readValue);
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

    private List<Expectation> getExpectationByPriority(List<Expectation> expectations, String path) {
        List<Expectation> expectationByPriority = new ArrayList<>();

        List<Expectation> exactMatch = new ArrayList<>();
        Map<Integer, List<Expectation>> matches = new TreeMap<>(Collections.reverseOrder());

        for (Iterator iterator = expectations.iterator(); iterator.hasNext();) {
            Expectation expectation = (Expectation) iterator.next();
            if (path.equals(expectation.getRequest().getPath().getValue())) {
                exactMatch.add(expectation);
                iterator.remove();
            }
        }

        for (Iterator iterator = expectations.iterator(); iterator.hasNext();) {
            Expectation expectation = (Expectation) iterator.next();
            String[] split = expectation.getRequest().getPath().getValue().split("/");
            List<Expectation> list = matches.get(split.length);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(expectation);
            matches.put(split.length, list);
            iterator.remove();
        }

        expectationByPriority.addAll(exactMatch);
        matches.forEach((k, v) -> {
            expectationByPriority.addAll(v);
        });
        expectationByPriority.addAll(expectations);
        return expectationByPriority;
    }

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
            JsonSchema schema = schemaGen.generateSchema(Expectation.class);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
