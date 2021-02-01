package com.itorix.apiwiz.performance.coverge.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlRootElement(name = "stepName")
@XmlAccessorType (XmlAccessType.FIELD)
public class StepName {
	String value;
	double timeTaken;
	String stepType;
	
	public String getStepType() {
		return stepType;
	}
	public void setStepType(String stepType) {
		this.stepType = stepType;
	}
	public StepName() {}  
	public StepName( String value, int timeTaken) {  
	    super();  
	    this.timeTaken = timeTaken;  
	    this.value = value;  
	    
	} 
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setTimeTaken(double timeTaken) {
		this.timeTaken = timeTaken;
	}
	
	public double getTimeTaken() {
		return timeTaken;
	}
	public void setTimeTaken(String timeTaken) {
		this.timeTaken = Double.parseDouble(timeTaken);
	}
	
}
