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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 *         &lt;element ref="{}Request"/>
 *         &lt;element ref="{}Response"/>
 *         &lt;element ref="{}Condition" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"request", "response", "condition"})
@XmlRootElement(name = "Flow")
public class Flow implements ToString {

	@XmlElement(name = "Request", required = true)
	protected Request request;

	@XmlElement(name = "Response", required = true)
	protected Response response;

	@XmlElement(name = "Condition")
	protected String condition;

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NCName")
	protected String name;

	/**
	 * Gets the value of the request property.
	 *
	 * @return possible object is {@link Request }
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Sets the value of the request property.
	 *
	 * @param value
	 *            allowed object is {@link Request }
	 */
	public void setRequest(Request value) {
		this.request = value;
	}

	/**
	 * Gets the value of the response property.
	 *
	 * @return possible object is {@link Response }
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * Sets the value of the response property.
	 *
	 * @param value
	 *            allowed object is {@link Response }
	 */
	public void setResponse(Response value) {
		this.response = value;
	}

	/**
	 * Gets the value of the condition property.
	 *
	 * @return possible object is {@link String }
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Sets the value of the condition property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setCondition(String value) {
		this.condition = value;
	}

	/**
	 * Gets the value of the name property.
	 *
	 * @return possible object is {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *            allowed object is {@link String }
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
			Request theRequest;
			theRequest = this.getRequest();
			strategy.appendField(locator, this, "request", buffer, theRequest);
		}
		{
			Response theResponse;
			theResponse = this.getResponse();
			strategy.appendField(locator, this, "response", buffer, theResponse);
		}
		{
			String theCondition;
			theCondition = this.getCondition();
			strategy.appendField(locator, this, "condition", buffer, theCondition);
		}
		{
			String theName;
			theName = this.getName();
			strategy.appendField(locator, this, "name", buffer, theName);
		}
		return buffer;
	}
}
