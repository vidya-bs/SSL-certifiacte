//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.05.31 at 06:53:28 PM IST
//

package com.itorix.apiwiz.common.model.policyperformance.target.endpoint;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

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
 *         &lt;element ref="{}Flow" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"flow"})
@XmlRootElement(name = "Flows")
public class Flows implements ToString {

	@XmlElement(name = "Flow", required = true)
	protected List<Flow> flow;

	/**
	 * Gets the value of the flow property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the flow property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getFlow().add(newItem);
	 * </pre>
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Flow }
	 */
	public List<Flow> getFlow() {
		if (flow == null) {
			flow = new ArrayList<Flow>();
		}
		return this.flow;
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
			List<Flow> theFlow;
			theFlow = (((this.flow != null) && (!this.flow.isEmpty())) ? this.getFlow() : null);
			strategy.appendField(locator, this, "flow", buffer, theFlow);
		}
		return buffer;
	}
}
