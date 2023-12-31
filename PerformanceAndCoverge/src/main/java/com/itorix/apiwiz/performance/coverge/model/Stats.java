//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.07.24 at 04:51:41 PM IST
//

package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

/**
 * Java class for statsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="statsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="FlowStat" type="{}FlowStatType"/>
 *         &lt;element name="EndpointStat" type="{}EndpointStatType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stats", propOrder = {"endpointStat", "flowStat"})
public class Stats implements ToString {

	@XmlElement(name = "EndpointStat", required = false)
	protected List<EndpointStat> endpointStat;

	@XmlElement(name = "FlowStat", required = false)
	protected List<FlowStat> flowStat;

	/**
	 * Gets the value of the flowStatOrEndpointStat property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the flowStatOrEndpointStat property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getFlowStatOrEndpointStat().add(newItem);
	 * </pre>
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FlowStat
	 * } {@link EndpointStat }
	 */
	public String toString() {
		final ToStringStrategy strategy = JAXBToStringStrategy.INSTANCE;
		final StringBuilder buffer = new StringBuilder();
		append(null, buffer, strategy);
		return buffer.toString();
	}

	public List<EndpointStat> getEndpointStatType() {
		return endpointStat;
	}

	public void setEndpointStatType(List<EndpointStat> endpointStatType) {
		this.endpointStat = endpointStatType;
	}

	public List<FlowStat> getFlowStat() {
		return flowStat;
	}

	public void setFlowStat(List<FlowStat> flowStat) {
		this.flowStat = flowStat;
	}

	public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
		strategy.appendStart(locator, this, buffer);
		appendFields(locator, buffer, strategy);
		strategy.appendEnd(locator, this, buffer);
		return buffer;
	}

	@Override
	public StringBuilder appendFields(ObjectLocator arg0, StringBuilder arg1, ToStringStrategy arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * public StringBuilder appendFields(ObjectLocator locator, StringBuilder
	 * buffer, ToStringStrategy strategy) { { List<Object>
	 * theFlowStatOrEndpointStat; endpointStatType = ((this.endpointStatType!=
	 * null)?this.getEndpointStatType():null); strategy.appendField(locator,
	 * this, "flowStatOrEndpointStat", buffer, theFlowStatOrEndpointStat); }
	 * return buffer; }
	 */

}
