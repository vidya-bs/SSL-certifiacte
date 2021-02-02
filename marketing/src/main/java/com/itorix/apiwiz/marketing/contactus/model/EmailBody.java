package com.itorix.apiwiz.marketing.contactus.model;

public class EmailBody {
	
	private String name;
    private String email;
    private String company;
    private String jobTitle;
    private String message;
    
	public EmailBody() {
		super();
	}
	public EmailBody(String name, String email, String company, String jobTitle, String message) {
		super();
		this.name = name;
		this.email = email;
		this.company = company;
		this.jobTitle = jobTitle;
		this.message = message;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getJobTitle() {
		return jobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "EmailBody [name=" + name + ",\n email=" + email + ",\n company=" + company + ",\n jobTitle=" + jobTitle
				+ ",\n message=" + message + "]";
	}
    
}
