package com.itorix.apiwiz.testsuite.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.xml.Transform;
import com.itorix.apiwiz.testsuite.model.TestSuiteResponse;

@Component
public class TestSuiteReport {
	@Autowired
	ApplicationProperties app;

	public OutputStream getReport(TestSuiteResponse testresponse, String format)
			throws IOException, TransformerException {

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(testresponse);
		ObjectMapper jsonMapper = new ObjectMapper();
		JsonNode node = jsonMapper.readValue(json, JsonNode.class);
		return getXml(node, format);

	}

	public OutputStream getXml(JsonNode node, String format)
			throws JsonGenerationException, JsonMappingException, IOException, TransformerException {
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_NULLS_AS_XSI_NIL, true);
		StringWriter sw = new StringWriter();
		xmlMapper.writeValue(sw, node);
		String xmlString = sw.toString();
		return getHtml(xmlString, format);

	}

	public OutputStream getHtml(String xmlString, String format) throws TransformerException, IOException {
		StreamSource xmlDoc = new StreamSource(new StringReader(xmlString));
		InputStream xslt = this.getClass().getClassLoader().getResourceAsStream("report-html.xsl");
		StreamSource htmlXsl = new StreamSource(xslt);
		StreamResult result = Transform.simpleTransform(xmlDoc, htmlXsl);
		String html = result.getWriter().toString();
		Document document = Jsoup.parse(html, "UTF-8");
		document.outputSettings().syntax(Document.OutputSettings.Syntax.html);
		if (format.equalsIgnoreCase("html")) {
			String html1 = document.toString();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] array = html1.getBytes();
			out.write(array);
			return out;
		} else {
			return getPdf(document);
		}

	}

	public OutputStream getPdf(Document document) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ITextRenderer renderer = new ITextRenderer();
		SharedContext sharedContext = renderer.getSharedContext();
		sharedContext.setPrint(true);
		sharedContext.setInteractive(false);
		renderer.setDocumentFromString(document.toString());
		renderer.layout();
		renderer.createPDF(out);
		return out;

	}
}
