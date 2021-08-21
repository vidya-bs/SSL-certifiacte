package com.itorix.apiwiz.design.studio.businessimpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaDocumentation;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMinLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.itorix.apiwiz.design.studio.model.XmlSchemaVo;

/**
 * Classname : XPathGen Description : author : Anil Date of creation : Nov 9,
 * 2015
 *
 * <p>
 * Change History ------------------------------------------------------------
 * Date Changed By Description
 * ------------------------------------------------------------
 *
 * <p>
 * ------------------------------------------------------------
 *
 * <p>
 * Copyright notice :
 */
public class XPathGen {

	private static final Logger logger = LoggerFactory.getLogger(XPathGen.class);

	private XmlSchema schema = null;
	private StringBuffer buffer = new StringBuffer("");

	/**
	 * Classname / Method Name : XPathGen/init()
	 *
	 * @param fileName
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	public void init(final String fileName) throws Exception {
		InputStream is = new FileInputStream(fileName);
		XmlSchemaCollection schemaCol = new XmlSchemaCollection();
		schema = schemaCol.read(new StreamSource(is), null);
	}

	/**
	 * Classname / Method Name : XPathGen/generateXPathForElement()
	 *
	 * @param fileName
	 * @param elementname
	 * @param outPutFileName
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	public void generateXPathForElement(final String fileName, final String elementname, final String outPutFileName)
			throws Exception {
		init(fileName);
		XmlSchemaElement elm = schema.getElementByName(elementname);
		if (elm != null) {
			buffer.append(
					"Include| Element Path | Min | Max | XSD Type |JSON Type|JSON Format| Enum | Min Length | Max Length | Length | Pattern | Documentation")
					.append("\n");
			XPathForXMLSchemaElement(elm, "");
			File file = new File(outPutFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(String.valueOf(buffer));
			bw.close();
		} else {
			System.out.println("No Element with name:" + elementname);
		}
	}

	/**
	 * Classname / Method Name : XPathGen/generateXPathForGroup()
	 *
	 * @param fileName
	 * @param elementname
	 * @param outPutFileName
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	public void generateXPathForGroup(final String fileName, final String elementname, final String outPutFileName)
			throws Exception {
		init(fileName);
		XmlSchemaObjectTable elm = schema.getGroups();

		if (elm != null) {
			buffer.append(
					"Include| Element Path | Min | Max | XSD Type |JSON Type|JSON Format| Enum | Min Length | Max Length | Length | Pattern | Documentation")
					.append("\n");
			File file = new File(outPutFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			XmlSchemaGroup xmlSchemaGroup = (XmlSchemaGroup) elm
					.getItem(new QName(schema.getTargetNamespace(), elementname));

			XmlSchemaVo xmlSchemaVo = new XmlSchemaVo();
			xmlSchemaVo.setXpath(elementname);
			XmlSchemaParticle p = xmlSchemaGroup.getParticle();
			if (p != null) {
				if (null != p.getAnnotation()) {
					String documentation = getElementDocumentation(p.getAnnotation());
					xmlSchemaVo.setDocumentation(documentation);
				}
				xmlSchemaVo.setXsdType("Group");
				xmlSchemaVo.setMinOccurs(getCardinalityString(p.getMinOccurs()));
				xmlSchemaVo.setMaxOccurs(getCardinalityString(p.getMaxOccurs()));

				populateJsonValues(xmlSchemaVo);
				buffer.append(xmlSchemaVo.toString()).append("\n");
				if (p instanceof XmlSchemaSequence) {
					XmlSchemaSequence seq = (XmlSchemaSequence) p;
					XmlSchemaObjectCollection items = seq.getItems();
					for (int i = 0; i < items.getCount(); i++) {
						XmlSchemaObject obj = items.getItem(i);
						if (obj instanceof XmlSchemaElement) {
							XPathForXMLSchemaElement((XmlSchemaElement) obj, elementname);
						}
						if (obj instanceof XmlSchemaChoice) {
							XPathForXmlSchemaChoice((XmlSchemaChoice) obj, elementname);
						}
						if (obj instanceof XmlSchemaSequence) {
							XPathForXmlSchemaSequence((XmlSchemaSequence) obj, elementname);
						}
						if (obj instanceof XmlSchemaGroupRef) {
							XPathForXmlSchemaGroupRef((XmlSchemaGroupRef) obj, elementname);
						}
					}
				}
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(String.valueOf(buffer));
			bw.close();
		} else {
			System.out.println("No Element with name:" + elementname);
		}
	}

	/**
	 * Classname / Method Name : XPathGen/getComplexTypeNames()
	 *
	 * @param fileName
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	public void getComplexTypeNames(final String fileName) throws Exception {
		init(fileName);
		XmlSchemaObjectTable objTable = schema.getSchemaTypes();
		Iterator<?> itr = objTable.getNames();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o instanceof QName) {
				System.out.println(((QName) o).getLocalPart());
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/getElementNames()
	 *
	 * @param fileName
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	public void getElementNames(final String fileName) throws Exception {
		init(fileName);
		XmlSchemaObjectTable objTable = schema.getElements();
		Iterator<?> itr = objTable.getNames();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o instanceof QName) {
				System.out.println(((QName) o).getLocalPart());
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/getCardinalityString()
	 *
	 * @param occurs
	 * 
	 * @return @Description : Method is used to
	 */
	private String getCardinalityString(long occurs) {
		if (occurs == 1) {
			return "1";
		}
		if (occurs == 0) {
			return "0";
		}
		return "*";
	}

	/**
	 * Classname / Method Name : XPathGen/XPathForXMLSchemaElement()
	 *
	 * @param elm
	 * @param elementname
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private void XPathForXMLSchemaElement(final XmlSchemaElement elm, String elementname) throws Exception {
		XmlSchemaType st = elm.getSchemaType();
		if (st != null) {
			elementname = elementname + "/" + elm.getName();
			XmlSchemaVo xmlSchemaVo = new XmlSchemaVo();
			String elementDocumentation = null;
			if (null != elm.getAnnotation()) {
				elementDocumentation = getElementDocumentation(elm.getAnnotation());
			}
			xmlSchemaVo.setXpath(elementname);
			xmlSchemaVo.setMinOccurs(getCardinalityString(elm.getMinOccurs()));
			xmlSchemaVo.setMaxOccurs(getCardinalityString(elm.getMaxOccurs()));
			if (st instanceof XmlSchemaComplexType) {
				XPathForXMLSchemaComplexType((XmlSchemaComplexType) st, elementname, xmlSchemaVo);
			}
			if (st instanceof XmlSchemaSimpleType) {
				xmlSchemaVo = XPathForXMLSchemaSimpleType((XmlSchemaSimpleType) st, xmlSchemaVo);
				if (null != elementDocumentation && null == xmlSchemaVo.getDocumentation()) {
					xmlSchemaVo.setDocumentation(elementDocumentation);
				}
			}
			if (!String.valueOf(buffer).contains(xmlSchemaVo.toString())) {
				buffer.append(xmlSchemaVo.toString()).append("\n");
			}

		} else {
			QName refName = elm.getRefName();
			if (refName == null) {
				// System.out.println("Review XSD Error with Elemnt: " +
				// elementname + "/" +
				// elm.getName());
			} else {
				XPathForXMLSchemaElement(schema.getElementByName(refName.getLocalPart()), elementname);
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/XPathForXMLSchemaSimpleType()
	 *
	 * @param simpleType
	 * @param xmlSchemaVo
	 * 
	 * @return
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private XmlSchemaVo XPathForXMLSchemaSimpleType(final XmlSchemaSimpleType simpleType, XmlSchemaVo xmlSchemaVo)
			throws Exception {
		String documentation = null;
		XmlSchemaSimpleTypeContent restriction = simpleType.getContent();
		if (restriction instanceof XmlSchemaSimpleTypeRestriction) {
			xmlSchemaVo.setXsdType(((XmlSchemaSimpleTypeRestriction) restriction).getBaseTypeName().getLocalPart());
			populateJsonValues(xmlSchemaVo);
			XmlSchemaObjectCollection fcts = ((XmlSchemaSimpleTypeRestriction) restriction).getFacets();
			if (fcts != null) {
				String enumFacet = "";
				String minLengthFacet = "";
				String maxLengthFacet = "";
				String lengthFacet = "";
				String patternFacet = "";
				StringBuffer sb = new StringBuffer("");
				for (int i = 0; i < fcts.getCount(); i++) {
					Object o = fcts.getItem(i);
					if (o instanceof XmlSchemaEnumerationFacet) {
						sb.append(((XmlSchemaEnumerationFacet) o).getValue());
						if (i < fcts.getCount() - 1) {
							sb.append(",");
						}
					} else if (o instanceof XmlSchemaMinLengthFacet) {
						XmlSchemaMinLengthFacet schemaPattern = (XmlSchemaMinLengthFacet) o;
						minLengthFacet = (String) schemaPattern.getValue();
					} else if (o instanceof XmlSchemaMaxLengthFacet) {
						XmlSchemaMaxLengthFacet schemaPattern = (XmlSchemaMaxLengthFacet) o;
						maxLengthFacet = (String) schemaPattern.getValue();
					} else if (o instanceof XmlSchemaLengthFacet) {
						XmlSchemaLengthFacet schemaPattern = (XmlSchemaLengthFacet) o;
						lengthFacet = (String) schemaPattern.getValue();
					} else if (o instanceof XmlSchemaPatternFacet) {
						XmlSchemaPatternFacet schemaPattern = (XmlSchemaPatternFacet) o;
						patternFacet = (String) schemaPattern.getValue();
					}
				}
				if (String.valueOf(sb).length() > 0) {
					enumFacet = String.valueOf(sb);
				}
				xmlSchemaVo.setEnums(enumFacet);
				xmlSchemaVo.setMinLength(minLengthFacet);
				xmlSchemaVo.setMaxLength(maxLengthFacet);
				xmlSchemaVo.setLength(lengthFacet);
				xmlSchemaVo.setPattern(patternFacet);
				if (null != simpleType.getAnnotation()) {
					documentation = getElementDocumentation(simpleType.getAnnotation());
					xmlSchemaVo.setDocumentation(documentation);
				} else {
					xmlSchemaVo.setDocumentation(null);
				}
			}

		} else {
			if (simpleType.getName().equalsIgnoreCase("string")) {
				xmlSchemaVo.setXsdType("String");
			} else {
				xmlSchemaVo.setXsdType(simpleType.getName());
			}
			populateJsonValues(xmlSchemaVo);
		}
		return xmlSchemaVo;
	}

	private void XPathForXMLSchemaComplexType(final XmlSchemaComplexType complexType, final String elementname,
			final XmlSchemaVo xmlSchemaVo) throws Exception {
		XPathForXMLSchemaComplexType(complexType, elementname, true, xmlSchemaVo);
	}

	/**
	 * Classname / Method Name : XPathGen/XPathForXMLSchemaComplexType()
	 *
	 * @param complexType
	 * @param elementname
	 * @param bNotARef
	 * @param xmlSchemaVo
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private void XPathForXMLSchemaComplexType(final XmlSchemaComplexType complexType, final String elementname,
			boolean bNotARef, XmlSchemaVo xmlSchemaVo) throws Exception {
		XmlSchemaParticle p = complexType.getParticle();
		if (p != null) {
			if (bNotARef && null != complexType.getAnnotation()) {
				String documentation = getElementDocumentation(complexType.getAnnotation());
				xmlSchemaVo.setDocumentation(documentation);
			}
			xmlSchemaVo.setXsdType("Complex");
			populateJsonValues(xmlSchemaVo);
			buffer.append(xmlSchemaVo.toString()).append("\n");
			// Get attributes
			XmlSchemaObjectCollection attrs = complexType.getAttributes();
			if (attrs != null) {
				for (int i = 0; i < attrs.getCount(); i++) {
					XmlSchemaObject o = attrs.getItem(i);
					if (o instanceof XmlSchemaAttribute) {
						XmlSchemaAttribute schemaAttribute = (XmlSchemaAttribute) o;
						XmlSchemaVo attributesVo = new XmlSchemaVo();
						attributesVo.setXpath(elementname + "/@" + ((XmlSchemaAttribute) o).getName());
						attributesVo.setXsdType("String");
						populateJsonValues(attributesVo);
						attributesVo.setMinOccurs("0");
						attributesVo.setMaxOccurs("1");
						if (null != schemaAttribute.getAnnotation()) {
							String documentation = getElementDocumentation(schemaAttribute.getAnnotation());
							attributesVo.setDocumentation(documentation);
						}
						buffer.append(attributesVo.toString()).append("\n");
					}
				}
			}
			if (p instanceof XmlSchemaSequence) {
				XmlSchemaSequence seq = (XmlSchemaSequence) p;
				XmlSchemaObjectCollection items = seq.getItems();
				for (int i = 0; i < items.getCount(); i++) {
					XmlSchemaObject obj = items.getItem(i);
					if (obj instanceof XmlSchemaElement) {
						XPathForXMLSchemaElement((XmlSchemaElement) obj, elementname);
					}
					if (obj instanceof XmlSchemaChoice) {
						XPathForXmlSchemaChoice((XmlSchemaChoice) obj, elementname);
					}
					if (obj instanceof XmlSchemaSequence) {
						XPathForXmlSchemaSequence((XmlSchemaSequence) obj, elementname);
					}
					if (obj instanceof XmlSchemaGroupRef) {
						XPathForXmlSchemaGroupRef((XmlSchemaGroupRef) obj, elementname);
					}
				}
			}
			if (p instanceof XmlSchemaChoice) {
				XPathForXmlSchemaChoice((XmlSchemaChoice) p, elementname);
			}
		}
		XmlSchemaContentModel cc = complexType.getContentModel();
		if (cc != null) {
			if (cc instanceof XmlSchemaComplexContent) {
				// System.out.println(",Object");
				XmlSchemaContent ext = ((XmlSchemaComplexContent) cc).getContent();
				if (ext instanceof XmlSchemaComplexContentExtension) {
					XPathForXmlSchemaComplexContentExtension((XmlSchemaComplexContentExtension) ext, elementname,
							xmlSchemaVo);
				}
			}
			if (cc instanceof XmlSchemaSimpleContent) {
				XPathForXmlSchemaSimpleContent((XmlSchemaSimpleContent) cc, elementname, bNotARef, xmlSchemaVo);
			}
		}
	}

	private void XPathForXmlSchemaGroupRef(XmlSchemaGroupRef groupRef, String elementname) throws Exception {

		QName refName = groupRef.getRefName();
		if (refName == null) {
			// System.out.println("Review XSD Error with Elemnt: " + elementname
			// + "/" + elm.getName());
		} else {
			// XmlSchemaVo xmlSchemaVo = new XmlSchemaVo();
			// elementname = elementname + "/" + refName.getLocalPart();
			// xmlSchemaVo.setXpath(elementname);
			XmlSchemaObjectTable obTable = schema.getGroups();
			XmlSchemaGroup xmlSchemaGroup = (XmlSchemaGroup) obTable.getItem(refName);
			XmlSchemaParticle p = xmlSchemaGroup.getParticle();
			if (p != null) {
				if (null != groupRef.getAnnotation()) {
					String documentation = getElementDocumentation(groupRef.getAnnotation());
					// xmlSchemaVo.setDocumentation(documentation);
				}
				// xmlSchemaVo.setXsdType("Group");
				// xmlSchemaVo.setMinOccurs(getCardinalityString(p.getMinOccurs()));
				// xmlSchemaVo.setMaxOccurs(getCardinalityString(p.getMaxOccurs()));

				// populateJsonValues(xmlSchemaVo);
				// buffer.append(xmlSchemaVo.toString()).append("\n");
				if (p instanceof XmlSchemaSequence) {
					XmlSchemaSequence seq = (XmlSchemaSequence) p;
					XmlSchemaObjectCollection items = seq.getItems();
					for (int i = 0; i < items.getCount(); i++) {
						XmlSchemaObject obj = items.getItem(i);
						if (obj instanceof XmlSchemaElement) {
							XPathForXMLSchemaElement((XmlSchemaElement) obj, elementname);
						}
						if (obj instanceof XmlSchemaChoice) {
							XPathForXmlSchemaChoice((XmlSchemaChoice) obj, elementname);
						}
						if (obj instanceof XmlSchemaSequence) {
							XPathForXmlSchemaSequence((XmlSchemaSequence) obj, elementname);
						}
						if (obj instanceof XmlSchemaGroupRef) {
							XPathForXmlSchemaGroupRef((XmlSchemaGroupRef) obj, elementname);
						}
					}
				}
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/getElementDocumentation()
	 *
	 * @param xmlSchemaAnnotation
	 * 
	 * @return @Description : Method is used to wrap the documentation into
	 *         single line.
	 */
	private static String getElementDocumentation(final XmlSchemaAnnotation xmlSchemaAnnotation) {
		String documentation = null;
		XmlSchemaObjectCollection schemaObjectCollection = xmlSchemaAnnotation.getItems();
		for (int i = 0; i < schemaObjectCollection.getCount(); i++) {
			XmlSchemaDocumentation object = (XmlSchemaDocumentation) schemaObjectCollection.getItem(i);
			NodeList nodeList = object.getMarkup();
			Node node = nodeList.item(0);
			documentation = node.getNodeValue();
			documentation = documentation.replaceAll("[\r\n]+", " ");
		}
		return documentation;
	}

	/**
	 * Classname / Method Name : XPathGen/XPathForXmlSchemaChoice()
	 *
	 * @param choice
	 * @param elementname
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private void XPathForXmlSchemaChoice(final XmlSchemaChoice choice, final String elementname) throws Exception {
		XmlSchemaObjectCollection items = choice.getItems();
		for (int i = 0; i < items.getCount(); i++) {
			XmlSchemaObject obj = items.getItem(i);
			if (obj instanceof XmlSchemaElement) {
				XPathForXMLSchemaElement((XmlSchemaElement) obj, elementname);
			}
			if (obj instanceof XmlSchemaChoice) {
				XPathForXmlSchemaChoice((XmlSchemaChoice) obj, elementname);
			}
			if (obj instanceof XmlSchemaSequence) {
				XPathForXmlSchemaSequence((XmlSchemaSequence) obj, elementname);
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/XPathForXmlSchemaSequence()
	 *
	 * @param sequence
	 * @param elementname
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private void XPathForXmlSchemaSequence(final XmlSchemaSequence sequence, final String elementname)
			throws Exception {
		XmlSchemaObjectCollection items = sequence.getItems();
		for (int i = 0; i < items.getCount(); i++) {
			XmlSchemaObject obj = items.getItem(i);
			if (obj instanceof XmlSchemaElement) {
				XPathForXMLSchemaElement((XmlSchemaElement) obj, elementname);
			}
			if (obj instanceof XmlSchemaChoice) {
				XPathForXmlSchemaChoice((XmlSchemaChoice) obj, elementname);
			}
			if (obj instanceof XmlSchemaSequence) {
				XPathForXmlSchemaSequence((XmlSchemaSequence) obj, elementname);
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/XPathForXmlSchemaSimpleContent()
	 *
	 * @param simpleExtType
	 * @param elementname
	 * @param bNotARef
	 * @param xmlSchemaVo
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private void XPathForXmlSchemaSimpleContent(final XmlSchemaSimpleContent simpleExtType, final String elementname,
			boolean bNotARef, XmlSchemaVo xmlSchemaVo) throws Exception {

		XmlSchemaContent sc = simpleExtType.getContent();
		if (sc instanceof XmlSchemaSimpleContentExtension) {
			// Get attributes
			XmlSchemaObjectCollection attrs = ((XmlSchemaSimpleContentExtension) sc).getAttributes();
			QName refName = ((XmlSchemaSimpleContentExtension) sc).getBaseTypeName();
			if (refName != null) {
				XmlSchemaType sct = schema.getTypeByName(refName.getLocalPart());
				if (sct == null) {
					if ((attrs != null) && (bNotARef)) {
						xmlSchemaVo.setXsdType("Complex");
						populateJsonValues(xmlSchemaVo);
						buffer.append(xmlSchemaVo.toString()).append("\n");
						xmlSchemaVo.setXpath(elementname + "/#text");
						xmlSchemaVo.setMinOccurs("1");
						xmlSchemaVo.setMaxOccurs("1");
						xmlSchemaVo.setXsdType(refName.getLocalPart());
						populateJsonValues(xmlSchemaVo);
						buffer.append(xmlSchemaVo.toString()).append("\n");
					}

				} else {
					if (sct instanceof XmlSchemaComplexType) {
						if ((bNotARef)) {
							xmlSchemaVo.setXsdType("Complex");
							populateJsonValues(xmlSchemaVo);
							xmlSchemaVo.setMinOccurs("0");
							xmlSchemaVo.setMaxOccurs("1");
							buffer.append(xmlSchemaVo.toString()).append("\n");
							xmlSchemaVo.setXpath(elementname + "/#text");
							buffer.append(xmlSchemaVo.toString()).append("\n");
						}
						XPathForXMLSchemaComplexType((XmlSchemaComplexType) sct, elementname, false, xmlSchemaVo);
					}
					if (sct instanceof XmlSchemaSimpleType) {
						if ((attrs != null) && (bNotARef)) {
							xmlSchemaVo.setXsdType("Complex");
							populateJsonValues(xmlSchemaVo);
							buffer.append(xmlSchemaVo.toString()).append("\n");
							xmlSchemaVo.setXpath(elementname + "/#text");
							xmlSchemaVo.setMinOccurs("0");
							xmlSchemaVo.setMaxOccurs("1");
						}
						XmlSchemaVo schemaDoc = XPathForXMLSchemaSimpleType((XmlSchemaSimpleType) sct, xmlSchemaVo);
						XmlSchemaSimpleType schemaSimple = (XmlSchemaSimpleType) sct;
						if (null != schemaSimple.getAnnotation() && null == schemaDoc.getDocumentation()) {
							String documentation = getElementDocumentation(schemaSimple.getAnnotation());
							xmlSchemaVo.setDocumentation(documentation);
						}
						buffer.append(xmlSchemaVo.toString()).append("\n");
					}
				}
			} else {
				if ((attrs != null) && (bNotARef)) {
					System.out.println("|Complex");
					System.out.print(elementname + "/#text|1|1|String");
				} else {
					System.out.println("|String");
				}
			}
			if (attrs != null) {
				for (int i = 0; i < attrs.getCount(); i++) {
					XmlSchemaObject o = attrs.getItem(i);
					if (o instanceof XmlSchemaAttribute) {
						XmlSchemaAttribute schemaAttribute = (XmlSchemaAttribute) o;
						XmlSchemaVo attributesVo = new XmlSchemaVo();
						attributesVo.setXpath(elementname + "/@" + ((XmlSchemaAttribute) o).getName());
						attributesVo.setXsdType("String");
						populateJsonValues(attributesVo);
						attributesVo.setMinOccurs("0");
						attributesVo.setMaxOccurs("1");
						if (null != schemaAttribute.getAnnotation()) {
							String documentation = getElementDocumentation(schemaAttribute.getAnnotation());
							attributesVo.setDocumentation(documentation);
						}
						buffer.append(attributesVo.toString()).append("\n");
					}
				}
			}
		} else {
			System.out.println("|String");
		}
	}

	/**
	 * Classname / Method Name :
	 * XPathGen/XPathForXmlSchemaComplexContentExtension()
	 *
	 * @param complexExtType
	 * @param elementname
	 * @param xmlSchemaVo
	 * 
	 * @throws Exception
	 * 
	 * @Description : Method is used to
	 */
	private void XPathForXmlSchemaComplexContentExtension(final XmlSchemaComplexContentExtension complexExtType,
			final String elementname, final XmlSchemaVo xmlSchemaVo) throws Exception {
		// Handle base type
		XmlSchemaType st = schema.getTypeByName(complexExtType.getBaseTypeName().getLocalPart());
		if (st != null) {
			if (st instanceof XmlSchemaComplexType) {
				XPathForXMLSchemaComplexType((XmlSchemaComplexType) st, elementname, xmlSchemaVo);
			}
			if (st instanceof XmlSchemaSimpleType) {
				XPathForXMLSchemaSimpleType((XmlSchemaSimpleType) st, xmlSchemaVo);
			}
		}

		// Handle Sequence
		XmlSchemaParticle p = complexExtType.getParticle();
		if (p instanceof XmlSchemaSequence) {
			XmlSchemaSequence seq = (XmlSchemaSequence) p;
			XmlSchemaObjectCollection items = seq.getItems();
			for (int i = 0; i < items.getCount(); i++) {
				XmlSchemaObject obj = items.getItem(i);
				if (obj instanceof XmlSchemaElement) {
					XPathForXMLSchemaElement((XmlSchemaElement) obj, elementname);
				}
			}
		}
		// Hamdle Attributes
		XmlSchemaObjectCollection attrs = complexExtType.getAttributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.getCount(); i++) {
				XmlSchemaObject o = attrs.getItem(i);
				if (o instanceof XmlSchemaAttribute) {
					XmlSchemaAttribute schemaAttribute = (XmlSchemaAttribute) o;
					XmlSchemaVo attributesVo = new XmlSchemaVo();
					attributesVo.setXpath(elementname + "/@" + ((XmlSchemaAttribute) o).getName());
					attributesVo.setMinOccurs("0");
					attributesVo.setMaxOccurs("1");
					attributesVo.setXsdType("String");
					populateJsonValues(attributesVo);
					if (null != schemaAttribute.getAnnotation()) {
						String documentation = getElementDocumentation(schemaAttribute.getAnnotation());
						attributesVo.setDocumentation(documentation);
					}
					buffer.append(attributesVo.toString()).append("\n");
				}
			}
		}
	}

	/**
	 * Classname / Method Name : XPathGen/populateJsonValues()
	 *
	 * @param xmlSchemaVo
	 * 
	 * @return @Description : Method is used to populate JSON Type and JSON
	 *         Format values to XmlSchemaVo.
	 */
	private XmlSchemaVo populateJsonValues(XmlSchemaVo xmlSchemaVo) {
		PropertyReader reader = new PropertyReader();
		Properties properties = reader.readClassPathPropertyFile("jsonType.properties");
		String jsonValue = (String) properties.get(xmlSchemaVo.getXsdType());
		if (null != jsonValue && jsonValue.contains("|")) {
			String jsonArray[] = jsonValue.split(Pattern.quote("|"));
			xmlSchemaVo.setJsonType(jsonArray[0]);
			xmlSchemaVo.setJsonFormat(jsonArray[1].trim());
		}
		return xmlSchemaVo;
	}
}
