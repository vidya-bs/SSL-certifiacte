package com.itorix.apiwiz.marketing.events.model;

import java.util.List;

public class UserEventDTO {
    private String email;
    private String event;
    private String name;
    private String firstName;
    private String lastName;
    private String company;
    private String role;
    private String message;
    private String plan;
    private List<String> interestedFeatures;

    public UserEventDTO(String email, String event, String name, String firstName, String lastName, String company, String role, String message, String plan, List<String> interestedFeatures) {
        this.email = email;
        this.event = event;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.role = role;
        this.message = message;
        this.plan = plan;
        this.interestedFeatures = interestedFeatures;
    }

    public UserEventDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public List<String> getInterestedFeatures() {
        return interestedFeatures;
    }

    public void setInterestedFeatures(List<String> interestedFeatures) {
        this.interestedFeatures = interestedFeatures;
    }
}