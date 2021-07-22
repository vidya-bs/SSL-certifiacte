package com.itorix.apiwiz.performance.coverge.businessimpl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.SoapUIException;
import com.itorix.apiwiz.performance.coverge.model.SoapUIProject;
import com.itorix.apiwiz.performance.coverge.model.SoapUITestCase;
import com.itorix.apiwiz.performance.coverge.model.SoapUITestSuite;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class SOAPUITestReportExecutor {

	private static final String REPORT_TEMPLATE = "firstClientReport.ftl";
	// private static final String PROJECT_XML_PATH =
	// "src/main/resources/project.xml";
	private static final String PROJECT_XML_PATH = "project.xml";

	public Map<String, String> soapUIProjectTestReportExecutor(String xmlString, String env) {
		Map<String, String> reportMap = null;
		try {

			SoapUIProject soupUITest = getTestSuite(xmlString);

			Configuration cfg = new Configuration(new Version("2.3.23"));

			cfg.setClassForTemplateLoading(SOAPUITestReportExecutor.class, "/templates/");
			cfg.setDefaultEncoding("UTF-8");
			cfg.setLocale(Locale.US);
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			Template template = cfg.getTemplate(REPORT_TEMPLATE);

			Map<String, Object> input = new HashMap<String, Object>();
			input.put("total", soupUITest.getTotal());
			input.put("passed", soupUITest.getPassed());
			input.put("failed", soupUITest.getFailed());
			input.put("testSuiteList", soupUITest.getTestSuiteList());

			StringWriter stringWriter = new StringWriter();
			template.process(input, stringWriter);

			String templateOut = stringWriter.toString();

			reportMap = new HashMap<String, String>();
			reportMap.put("total", soupUITest.getTotal().toString());
			reportMap.put("html", templateOut);
			reportMap.put("failed", soupUITest.getFailed().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportMap;
	}

	public SoapUIProject getTestSuite(String stringXML) throws Exception {

		String suiteName = "";
		long startTime = 0;
		long duration = 0;
		int total = 0;
		int passed = 0;
		int failed = 0;
		TestRunner runner = null;

		List<TestSuite> suiteList = new ArrayList<TestSuite>();
		List<TestCase> caseList = new ArrayList<TestCase>();
		List<SoapUITestSuite> testSuiteList = new ArrayList<SoapUITestSuite>();
		// SoapUI.setSoapUICore(new StandaloneSoapUICore(true));
		SoapUI.setSoapUICore(new StandaloneSoapUICore("src/main/resources/soapui-settings.xml"));

		WsdlProject project = stringToSoapUIProject(stringXML);

		suiteList = project.getTestSuiteList();

		for (int i = 0; i < suiteList.size(); i++) {

			suiteName = suiteList.get(i).getName();

			caseList = suiteList.get(i).getTestCaseList();
			total = total + caseList.size();
			int totalTestCases = caseList.size();
			int passedTestCasescount = 0;
			int failedTestCasesCount = 0;
			List<SoapUITestCase> testCaseList = new ArrayList<SoapUITestCase>();
			for (int k = 0; k < totalTestCases; k++) {
				startTime = System.currentTimeMillis();
				runner = project.getTestSuiteByName(suiteName).getTestCaseByName(caseList.get(k).getName())
						.run(new PropertiesMap(), false);
				duration = System.currentTimeMillis() - startTime;
				if (runner.getStatus().equals(Status.FAILED)) {
					failedTestCasesCount++;
					failed++;
				} else {
					passedTestCasescount++;
					passed++;
				}
				testCaseList.add(new SoapUITestCase(caseList.get(k).getName(), runner.getStatus().toString(),
						runner.getReason(), duration));
			}
			testSuiteList.add(new SoapUITestSuite(suiteName, testCaseList, totalTestCases, failedTestCasesCount,
					passedTestCasescount));
		}
		SoapUIProject test = new SoapUIProject(testSuiteList, total, failed, passed);

		return test;
	}

	public WsdlProject stringToSoapUIProject(String xmlSource) throws SAXException, ParserConfigurationException,
			IOException, TransformerException, XmlException, SoapUIException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		File projectFile = new File(PROJECT_XML_PATH);
		StreamResult result = new StreamResult(projectFile);
		transformer.transform(source, result);
		WsdlProject wsdlProject = new WsdlProject(PROJECT_XML_PATH);
		projectFile.delete();
		return wsdlProject;
	}

	public static void main(String[] args) throws IOException {
		// System.out.println(FileUtils.readFileToString(new
		// File("/Users/sudhakar/Desktop/REST-Itorix-soapui-project.xml")));
		// Map<String, String> map =
		// soapUIProjectTestReportExecutor(FileUtils.readFileToString(new
		// File("/Users/sudhakar/Desktop/REST-Itorix-soapui-project.xml")),null);
		// output = new ObjectMapper().writeValueAsString(map);
		// System.out.println(map);
	}
}
