//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.31 at 06:53:28 PM IST 
//


package com.itorix.apiwiz.common.model.policyperformance.target.endpoint;

import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}Condition"/>
 *         &lt;element ref="{}FaultRules"/>
 *         &lt;element ref="{}Name"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "faultRules",
    "condition",
    "name"
})
@XmlRootElement(name = "Step")
public class Step implements ToString
{

		@XmlElement(name = "FaultRules", required = true)
	    protected String faultRules;
	    @XmlElement(name = "Condition", required = true)
	    protected String condition;
	    @XmlElement(name = "Name", required = true)
	    protected String name;
	    @XmlAttribute(name = "executed")
	    protected String executed= "false";

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.itorix.apigee.model.endpoint1.FaultRules }
     * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     * {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     * {@link String }
     * 
     * 
     */
  

    public String toString() {
        final ToStringStrategy strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public String getFaultRules() {
		return faultRules;
	}

	public void setFaultRules(String faultRules) {
		this.faultRules = faultRules;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

		

	public String getExecuted() {
		return executed;
	}

	public void setExecuted(String executed) {
		this.executed = executed;
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
            theName = this.getFaultRules();
            strategy.appendField(locator, this, "faultRules", buffer, theName);
        }
    	{
            String theName;
            theName = this.getName();
            strategy.appendField(locator, this, "condition", buffer, theName);
        }
    	{
            String theName;
            theName = this.getName();
            strategy.appendField(locator, this, "name", buffer, theName);
        }
        return buffer;
    }

}