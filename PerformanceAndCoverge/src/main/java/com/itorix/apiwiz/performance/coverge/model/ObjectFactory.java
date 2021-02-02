//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.24 at 04:51:41 PM IST 
//


package com.itorix.apiwiz.performance.coverge.model;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.itorix.apigee.model.proxystats package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ProxyStat_QNAME = new QName("", "ProxyStat");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.itorix.apigee.model.proxystats
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProxyStat }
     * 
     */
    public ProxyStat createProxyStatType() {
        return new ProxyStat();
    }

    /**
     * Create an instance of {@link FlowStat }
     * 
     */
    public FlowStat createFlowStatType() {
        return new FlowStat();
    }

    /**
     * Create an instance of {@link Stats }
     * 
     */
    public Stats createStatsType() {
        return new Stats();
    }

    /**
     * Create an instance of {@link EndpointStat }
     * 
     */
    public EndpointStat createEndpointStatType() {
        return new EndpointStat();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProxyStat }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ProxyStat")
    public JAXBElement<ProxyStat> createProxyStat(ProxyStat value) {
        return new JAXBElement<ProxyStat>(_ProxyStat_QNAME, ProxyStat.class, null, value);
    }

}