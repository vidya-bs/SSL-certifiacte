//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.24 at 04:51:41 PM IST 
//


package com.itorix.apiwiz.performance.coverge.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;


/**
 * <p>Java class for EndpointStatType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EndpointStatType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="UsageProxyEndpoint"/>
 *               &lt;enumeration value="Ericsson_UsageWSIL_POLARIS"/>
 *               &lt;enumeration value="Ericsson_UsageWSIL_TITAN"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="coverage">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="41"/>
 *               &lt;enumeration value="74"/>
 *               &lt;enumeration value="0"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="totalPolicies">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="61"/>
 *               &lt;enumeration value="31"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="executedPolicies">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="25"/>
 *               &lt;enumeration value="23"/>
 *               &lt;enumeration value="0"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="stats" type="{}statsType"/>
 *         &lt;element name="endpointType">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Proxy"/>
 *               &lt;enumeration value="Target"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndpointStat", propOrder = {
    "name",
    "coverage",
    "totalPolicies",
    "executedPolicies",
    "stats",
    "endpointType"
})
public class EndpointStat implements ToString
{

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String coverage;
    @XmlElement(required = true)
    protected String totalPolicies;
    @XmlElement(required = true)
    protected String executedPolicies;
    @XmlElement(required = true)
    protected Stats stats;
    @XmlElement(required = true)
    protected String endpointType;

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

    /**
     * Gets the value of the coverage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoverage() {
        return coverage;
    }

    /**
     * Sets the value of the coverage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoverage(String value) {
        this.coverage = value;
    }

    /**
     * Gets the value of the totalPolicies property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotalPolicies() {
        return totalPolicies;
    }

    /**
     * Sets the value of the totalPolicies property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalPolicies(String value) {
        this.totalPolicies = value;
    }

    /**
     * Gets the value of the executedPolicies property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExecutedPolicies() {
        return executedPolicies;
    }

    /**
     * Sets the value of the executedPolicies property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExecutedPolicies(String value) {
        this.executedPolicies = value;
    }

    /**
     * Gets the value of the stats property.
     * 
     * @return
     *     possible object is
     *     {@link Stats }
     *     
     */
    public Stats getStats() {
        return stats;
    }

    /**
     * Sets the value of the stats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Stats }
     *     
     */
    public void setStats(Stats value) {
        this.stats = value;
    }

    /**
     * Gets the value of the endpointType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpointType() {
        return endpointType;
    }

    /**
     * Sets the value of the endpointType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpointType(String value) {
        this.endpointType = value;
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
            String theName;
            theName = this.getName();
            strategy.appendField(locator, this, "name", buffer, theName);
        }
        {
            String theCoverage;
            theCoverage = this.getCoverage();
            strategy.appendField(locator, this, "coverage", buffer, theCoverage);
        }
        {
            String theTotalPolicies;
            theTotalPolicies = this.getTotalPolicies();
            strategy.appendField(locator, this, "totalPolicies", buffer, theTotalPolicies);
        }
        {
            String theExecutedPolicies;
            theExecutedPolicies = this.getExecutedPolicies();
            strategy.appendField(locator, this, "executedPolicies", buffer, theExecutedPolicies);
        }
        {
            Stats theStats;
            theStats = this.getStats();
            strategy.appendField(locator, this, "stats", buffer, theStats);
        }
        {
            String theEndpointType;
            theEndpointType = this.getEndpointType();
            strategy.appendField(locator, this, "endpointType", buffer, theEndpointType);
        }
        return buffer;
    }

}
