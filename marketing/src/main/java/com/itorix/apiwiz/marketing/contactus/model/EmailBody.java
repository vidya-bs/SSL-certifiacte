package com.itorix.apiwiz.marketing.contactus.model;

public class EmailBody {

	private String name;
	private String email;
	private String company;
	private String jobTitle;
	private String message;
	private String planId;

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


	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	@Override
	public String toString() {
		return "EmailBody [name=" + name + ",\n email=" + email + ",\n company=" + company + ",\n jobTitle=" + jobTitle
				+ ",\n planId=" + planId + ",\n message=" + message + "]";
	}

	public String toHTML() {
		String html =
				"<table style='border-collapse: collapse; width: 60%; border:2px double #b3adad; padding:5px;' border='2px'><tbody>"
						+ "<tr><td style='width: 30%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'><p class='p1'>Name</p></td><td style='width: 70%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'>" + name
						+ "</td></tr>" + "<tr><td style='width: 30%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'><p class='p1'>Email</p></td><td style='width: 70%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'>"
						+ email + "</td></tr>"
						+ "<tr><td style='width: 30%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'><p class='p1'>Plan id</p></td><td style='width: 70%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'>" + planId
						+ "</td></tr>"
						+ "<tr><td style='width: 30%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'><p class='p1'>Company</p></td><td style='width: 70%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'>" + company
						+ "</td></tr>" + "<tr><td style='width: 30%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'><p class='p1'>Job Title</p></td><td style='width: 70%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'>"
						+ jobTitle + "</td></tr>"
						+ "<tr><td style='width: 30%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'><p class='p1'>Message</p></td><td style='width: 70%; text-align:center; background: #ffffff; color: #2b2b2b; padding:5px; border:2px double #b3adad;'>" + message
						+ "</td></tr>" + "</tbody></table>";
		return html;
	}
}
