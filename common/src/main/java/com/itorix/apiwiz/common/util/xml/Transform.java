package com.itorix.apiwiz.common.util.xml;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class Transform {
	private static final Logger logger = LoggerFactory.getLogger(Transform.class);
	/**
	 * Simple transformation method.
	 *
	 * @param sourcePath
	 *            - Absolute path to source xml file.
	 * @param xsltPath
	 *            - Absolute path to xslt file.
	 * 
	 * @throws TransformerException
	 */
	public static StreamResult simpleTransform(String sourcePath, String xsltPath) throws TransformerException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new StreamSource(new File(xsltPath)));
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(new StreamSource(new File(sourcePath)), result);
		// log.info(((StringWriter)result.getWriter()).getBuffer().toString());
		return result;
	}

	public static StreamResult simpleTransform(StreamSource source, StreamSource xslt) throws TransformerException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(xslt);
		transformer.setOutputProperty("method", "html");
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(source, result);
		// log.info(((StringWriter) result.getWriter()).getBuffer().toString());
		return result;
	}

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		try {
			simpleTransform("C:/Sudhakar/codeGen/debug.xml", "C:/Sudhakar/codeGen/codecoverage_debug.xslt");
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}

		long endtTime = System.nanoTime();
		logger.info(String.valueOf((endtTime - startTime) / 10000000));
	}
}
