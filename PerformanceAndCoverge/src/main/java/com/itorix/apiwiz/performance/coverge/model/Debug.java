package com.itorix.apiwiz.performance.coverge.model;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "Debug")
@XmlAccessorType (XmlAccessType.FIELD)
public class Debug {
	@XmlElement( name = "stepName" )
	private List<StepName> stepList;

	public List<StepName> getStepList() {
		return stepList;
	}

	public void setStepList(List<StepName> stepList) {
		this.stepList = stepList;
	}
	

}
