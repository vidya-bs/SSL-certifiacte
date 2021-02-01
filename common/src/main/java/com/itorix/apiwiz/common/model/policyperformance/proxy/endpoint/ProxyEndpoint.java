//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.31 at 06:53:28 PM IST 
//


package com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Description"/>
 *         &lt;element ref="{}PostClientFlow"/>
 *         &lt;element ref="{}FaultRules"/>
 *         &lt;element ref="{}DefaultFaultRule"/>
 *         &lt;element ref="{}PreFlow"/>
 *         &lt;element ref="{}Flows"/>
 *         &lt;element ref="{}PostFlow"/>
 *         &lt;element ref="{}HTTPProxyConnection"/>
 *         &lt;element ref="{}RouteRule" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "postClientFlow",
    "faultRules",
    "defaultFaultRule",
    "preFlow",
    "flows",
    "postFlow",
    "httpProxyConnection",
    "routeRule"
})
@XmlRootElement(name = "ProxyEndpoint")
public class ProxyEndpoint
    implements ToString
{

    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "PostClientFlow", required = true)
    protected PostClientFlow postClientFlow;
    @XmlElement(name = "FaultRules", required = true,nillable = true)
    protected FaultRules faultRules;
    @XmlElement(name = "DefaultFaultRule", required = true)
    protected DefaultFaultRule defaultFaultRule;
    @XmlElement(name = "PreFlow", required = true)
    protected PreFlow preFlow;
    @XmlElement(name = "Flows", required = true)
    protected Flows flows;
    @XmlElement(name = "PostFlow", required = true)
    protected PostFlow postFlow;
    @XmlElement(name = "HTTPProxyConnection", required = true)
    protected HTTPProxyConnection httpProxyConnection;
    @XmlElement(name = "RouteRule", required = true)
    protected List<RouteRule> routeRule;
    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the postClientFlow property.
     * 
     * @return
     *     possible object is
     *     {@link PostClientFlow }
     *     
     */
    public PostClientFlow getPostClientFlow() {
        return postClientFlow;
    }

    /**
     * Sets the value of the postClientFlow property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostClientFlow }
     *     
     */
    public void setPostClientFlow(PostClientFlow value) {
        this.postClientFlow = value;
    }

    /**
     * Gets the value of the faultRules property.
     * 
     * @return
     *     possible object is
     *     {@link FaultRules }
     *     
     */
    public FaultRules getFaultRules() {
        return faultRules;
    }

    /**
     * Sets the value of the faultRules property.
     * 
     * @param value
     *     allowed object is
     *     {@link FaultRules }
     *     
     */
    public void setFaultRules(FaultRules value) {
        this.faultRules = value;
    }

    /**
     * Gets the value of the defaultFaultRule property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultFaultRule }
     *     
     */
    public DefaultFaultRule getDefaultFaultRule() {
        return defaultFaultRule;
    }

    /**
     * Sets the value of the defaultFaultRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultFaultRule }
     *     
     */
    public void setDefaultFaultRule(DefaultFaultRule value) {
        this.defaultFaultRule = value;
    }

    /**
     * Gets the value of the preFlow property.
     * 
     * @return
     *     possible object is
     *     {@link PreFlow }
     *     
     */
    public PreFlow getPreFlow() {
        return preFlow;
    }

    /**
     * Sets the value of the preFlow property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreFlow }
     *     
     */
    public void setPreFlow(PreFlow value) {
        this.preFlow = value;
    }

    /**
     * Gets the value of the flows property.
     * 
     * @return
     *     possible object is
     *     {@link Flows }
     *     
     */
    public Flows getFlows() {
        return flows;
    }

    /**
     * Sets the value of the flows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Flows }
     *     
     */
    public void setFlows(Flows value) {
        this.flows = value;
    }

    /**
     * Gets the value of the postFlow property.
     * 
     * @return
     *     possible object is
     *     {@link PostFlow }
     *     
     */
    public PostFlow getPostFlow() {
        return postFlow;
    }

    /**
     * Sets the value of the postFlow property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostFlow }
     *     
     */
    public void setPostFlow(PostFlow value) {
        this.postFlow = value;
    }

    /**
     * Gets the value of the httpProxyConnection property.
     * 
     * @return
     *     possible object is
     *     {@link HTTPProxyConnection }
     *     
     */
    public HTTPProxyConnection getHTTPProxyConnection() {
        return httpProxyConnection;
    }

    /**
     * Sets the value of the httpProxyConnection property.
     * 
     * @param value
     *     allowed object is
     *     {@link HTTPProxyConnection }
     *     
     */
    public void setHTTPProxyConnection(HTTPProxyConnection value) {
        this.httpProxyConnection = value;
    }

    /**
     * Gets the value of the routeRule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routeRule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRouteRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RouteRule }
     * 
     * 
     */
    public List<RouteRule> getRouteRule() {
        if (routeRule == null) {
            routeRule = new ArrayList<RouteRule>();
        }
        return this.routeRule;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public String toString() {
        final ToStringStrategy strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        {
            String theDescription;
            theDescription = this.getDescription();
            strategy.appendField(locator, this, "description", buffer, theDescription);
        }
        {
            PostClientFlow thePostClientFlow;
            thePostClientFlow = this.getPostClientFlow();
            strategy.appendField(locator, this, "postClientFlow", buffer, thePostClientFlow);
        }
        {
            FaultRules theFaultRules;
            theFaultRules = this.getFaultRules();
            strategy.appendField(locator, this, "faultRules", buffer, theFaultRules);
        }
        {
            DefaultFaultRule theDefaultFaultRule;
            theDefaultFaultRule = this.getDefaultFaultRule();
            strategy.appendField(locator, this, "defaultFaultRule", buffer, theDefaultFaultRule);
        }
        {
            PreFlow thePreFlow;
            thePreFlow = this.getPreFlow();
            strategy.appendField(locator, this, "preFlow", buffer, thePreFlow);
        }
        {
        	Flows theFlows;
            theFlows = this.getFlows();
            strategy.appendField(locator, this, "flows", buffer, theFlows);
        }
        {
            PostFlow thePostFlow;
            thePostFlow = this.getPostFlow();
            strategy.appendField(locator, this, "postFlow", buffer, thePostFlow);
        }
        {
            HTTPProxyConnection theHTTPProxyConnection;
            theHTTPProxyConnection = this.getHTTPProxyConnection();
            strategy.appendField(locator, this, "httpProxyConnection", buffer, theHTTPProxyConnection);
        }
        {
            List<RouteRule> theRouteRule;
            theRouteRule = (((this.routeRule!= null)&&(!this.routeRule.isEmpty()))?this.getRouteRule():null);
            strategy.appendField(locator, this, "routeRule", buffer, theRouteRule);
        }
        {
            String theName;
            theName = this.getName();
            strategy.appendField(locator, this, "name", buffer, theName);
        }
        return buffer;
    }

}
