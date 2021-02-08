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
    
	public String toHTML() {
		String html = "<table style='border-collapse: collapse; width: 60%;' border='1'><tbody>"
				+ "<tr><td style='width: 30%;'><p class='p1'>name</p></td><td style='width: 70%;'>"+name+"</td></tr>"
				+ "<tr><td style='width: 30%;'><p class='p1'>email</p></td><td style='width: 70%;'>"+email+"</td></tr>"
				+ "<tr><td style='width: 30%;'><p class='p1'>company</p></td><td style='width: 70%;'>"+company+"</td></tr>"
				+ "<tr><td style='width: 30%;'><p class='p1'>jobTitle</p></td><td style='width: 70%;'>"+jobTitle+"</td></tr>"
				+ "<tr><td style='width: 30%;'><p class='p1'>message</p></td><td style='width: 70%;'>"+message+"</td></tr>"
				+ "</tbody></table>";
		return html;
	}
}
