package com.itorix.apiwiz.common.model.proxystudio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyConfig {
	public static final String tmplApiFragflows = "CoreAPI/Proxy/flowfragments/";
	public static final String tmplPolicies = "CoreAPI/Proxy/policies/";
	public static final String tmplProxies = "CoreAPI/Proxy/proxies/";
	public static final String tmplResourcesXSL = "CoreAPI/Proxy/resources/xsl/";
	public static final String tmplResourcesJSC = "CoreAPI/Proxy/resources/jsc/";
	public static final String tmplProxy = "CoreAPI/Proxy/";
	public static final String STR_NAME = "name";
	public static final String STR_UNDERSCORE = "_";
	public static final String STR_API = "api";
	public static final String FTL_FILE_EXT = ".ftl";
	public static final String XSLT_FILE_EXT = ".xslt";
	public static final String XML_FILE_EXT = ".xml";
	public static final String JS_FILE_EXT = ".js";
	public static final String EMPTY_STRING = "";
	public static final String ENDPOINT_XML_SUFFIX = "Endpoint.xml";
	public static final String FLDR_RESOURCES = "resources";
	public static final String FLDR_XSL = "xsl";
	public static final String FLDR_XSD = "xsd";
	public static final String FLDR_JSC = "jsc";
	public static final String FLDR_JAVA = "java";
	public static final String FLDR_WSDL = "wsdl";
	public static final String FLDR_POLICIES = "policies";
	public static final String STR_ALL = "ALL_";
	public static final String STR_GET = "GET_";
	public static final String TARGET_LOC = "C:\\CodeGen\\GeneratedCode";
}
