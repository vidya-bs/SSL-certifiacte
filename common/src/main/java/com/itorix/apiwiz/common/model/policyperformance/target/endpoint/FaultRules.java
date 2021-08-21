//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.05.31 at 06:53:28 PM IST
//

package com.itorix.apiwiz.common.model.policyperformance.target.endpoint;

import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}FaultRule" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"faultRule"})
@XmlRootElement(name = "FaultRules")
public class FaultRules implements ToString {

	@XmlElement(name = "FaultRule", nillable = true)
	protected List<FaultRule> faultRule;

	/**
	 * Gets the value of the faultRule property.
	 *
	 * @return possible object is {@link FaultRule }
	 */
	public List<FaultRule> getFaultRule() {
		if (faultRule == null) {
			faultRule = new ArrayList<>();
		}
		return this.faultRule;
	}

	/**
	 * Sets the value of the faultRule property.
	 *
	 * @param value
	 *            allowed object is {@link FaultRule }
	 */
	public void setFaultRule(List<FaultRule> value) {
		this.faultRule = value;
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
			List<FaultRule> theFaultRule;
			theFaultRule = (((this.faultRule != null) && (!this.faultRule.isEmpty())) ? this.getFaultRule() : null);
			this.getFaultRule();
			strategy.appendField(locator, this, "faultRule", buffer, theFaultRule);
		}
		return buffer;
	}
}
