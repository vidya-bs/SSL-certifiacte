package com.itorix.apiwiz.test.executor.validators;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itorix.apiwiz.test.util.MaskFieldUtil;
@Slf4j
public class XmlValidator extends ResponseValidator {

    private DocumentBuilderFactory factory;

    private DocumentBuilder builder;

    private Document document;

    private String response;

    public XmlValidator() {
    }

    public XmlValidator(String response) throws ParserConfigurationException, SAXException, IOException {
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
        // factory.setNamespaceAware(true);
        document = builder.parse(new InputSource(new StringReader(response)));
        this.response = response;
    }

    /**
     * Provide an XPath to get the value
     *
     * @param Xpath::String
     * 
     * @return value
     * 
     * @throws XPathExpressionException
     */
    @Override
    public Object getAttributeValue(String path) throws XPathExpressionException {
        String value = null;
        try {
            value = XPathFactory.newInstance().newXPath().evaluate(path + "/text()", document);
        } catch (Exception ex) {
            log.error("Exception occurred",ex);
        }
        return value;
    }

    public static void main(String args[])
            throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v3=\"http://svc.kp.org/PtCareSupport/PHISharing/KPHCADTOrders/v3\">\n"
                + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n" + "      <v3:getEncounterADTOrders>\n"
                + "         <v3:encounterUCI>UCI-1234</v3:encounterUCI>\n"
                + "         <v3:patientIDType>OP</v3:patientIDType>\n"
                + "         <v3:regionCode>SCAL</v3:regionCode>\n" + "         <v3:userID>TestUser</v3:userID>\n"
                + "         <v3:orderTypesOverride>YES</v3:orderTypesOverride>\n"
                + "         <v3:maxEncounters>100</v3:maxEncounters>\n"
                + "         <v3:continuationPointer>10</v3:continuationPointer>\n"
                + "         <v3:reserved1>NO</v3:reserved1>\n" + "         <v3:reserved2>NO</v3:reserved2>\n"
                + "      </v3:getEncounterADTOrders>\n" + "   </soapenv:Body>\n" + "</soapenv:Envelope>";

        System.out
                .println(new XmlValidator(xml).getAttributeValue("/Envelope/Body/getEncounterADTOrders/patientIDType"));
    }

    public void setAttributeValue(String path, String value) throws XPathExpressionException {
    }

    public String getUpdatedObjectAsString() throws Exception {
        return response;
    }

    public String getMaskedResponse(List<String> fields) throws Exception {
        return MaskFieldUtil.getMaskedResponseForXml(fields, response);
    }
}