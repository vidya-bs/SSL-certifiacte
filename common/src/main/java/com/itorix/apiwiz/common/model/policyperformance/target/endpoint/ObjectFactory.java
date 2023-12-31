//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.05.31 at 06:53:28 PM IST
//

package com.itorix.apiwiz.common.model.policyperformance.target.endpoint;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the model package.
 *
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

	private static final QName _Condition_QNAME = new QName("", "Condition");
	private static final QName _Description_QNAME = new QName("", "Description");
	private static final QName _BasePath_QNAME = new QName("", "BasePath");
	private static final QName _VirtualHost_QNAME = new QName("", "VirtualHost");
	private static final QName _TargetEndpoint_QNAME = new QName("", "AEndpoint");
	private static final QName _Name_QNAME = new QName("", "Name");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: model
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of
	 * {@link com.itorix.apigee.model.endpoint1.DefaultFaultRule }
	 */
	public DefaultFaultRule createDefaultFaultRule() {
		return new DefaultFaultRule();
	}

	/**
	 * Create an instance of {@link com.itorix.apigee.model.endpoint1.Flows }
	 */
	public Flows createFlows() {
		return new Flows();
	}

	/** Create an instance of {@link com.itorix.apigee.model.endpoint1.Flow } */
	public Flow createFlow() {
		return new Flow();
	}

	/**
	 * Create an instance of {@link com.itorix.apigee.model.endpoint1.Request }
	 */
	public Request createRequest() {
		return new Request();
	}

	/** Create an instance of {@link com.itorix.apigee.model.endpoint1.Step } */
	public Step createStep() {
		return new Step();
	}

	/** Create an instance of {@link FaultRules } */
	public FaultRules createFaultRules() {
		return new FaultRules();
	}

	/**
	 * Create an instance of
	 * {@link com.itorix.apigee.model.endpoint1.FaultRule }
	 */
	public FaultRule createFaultRule() {
		return new FaultRule();
	}

	/** Create an instance of {@link Response } */
	public Response createResponse() {
		return new Response();
	}

	/** Create an instance of {@link RouteRule } */
	public RouteRule createRouteRule() {
		return new RouteRule();
	}

	/** Create an instance of {@link HTTPProxyConnection } */
	public HTTPProxyConnection createHTTPProxyConnection() {
		return new HTTPProxyConnection();
	}

	/** Create an instance of {@link PostClientFlow } */
	public PostClientFlow createPostClientFlow() {
		return new PostClientFlow();
	}

	/**
	 * Create an instance of {@link com.itorix.apigee.model.endpoint1.PreFlow }
	 */
	public PreFlow createPreFlow() {
		return new PreFlow();
	}

	/**
	 * Create an instance of
	 * {@link com.itorix.apigee.model.endpoint1.TargetEndpoint }
	 */
	public TargetEndpoint createProxyEndpoint() {
		return new TargetEndpoint();
	}

	/** Create an instance of {@link PostFlow } */
	public PostFlow createPostFlow() {
		return new PostFlow();
	}

	/**
	 * Create an instance of
	 * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
	 */
	@XmlElementDecl(namespace = "", name = "Condition")
	public JAXBElement<String> createCondition(String value) {
		return new JAXBElement<String>(_Condition_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of
	 * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
	 */
	@XmlElementDecl(namespace = "", name = "Description")
	public JAXBElement<String> createDescription(String value) {
		return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of
	 * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
	 */
	@XmlElementDecl(namespace = "", name = "BasePath")
	public JAXBElement<String> createBasePath(String value) {
		return new JAXBElement<String>(_BasePath_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of
	 * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
	 */
	@XmlElementDecl(namespace = "", name = "VirtualHost")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	public JAXBElement<String> createVirtualHost(String value) {
		return new JAXBElement<String>(_VirtualHost_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of
	 * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
	 */
	@XmlElementDecl(namespace = "", name = "AEndpoint")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	public JAXBElement<String> createTargetEndpoint(String value) {
		return new JAXBElement<String>(_TargetEndpoint_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of
	 * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
	 */
	@XmlElementDecl(namespace = "", name = "Name")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	public JAXBElement<String> createName(String value) {
		return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
	}
}
