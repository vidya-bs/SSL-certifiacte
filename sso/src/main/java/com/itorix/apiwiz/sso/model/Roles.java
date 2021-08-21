package com.itorix.apiwiz.sso.model;

public enum Roles {

    DEVELOPER("Developer"), ADMIN("Admin"), PORTAL("Portal"), ANALYST("Analyst"), PROJECT_ADMIN("Project-Admin"),
    QA("QA"), OPERATION("Operation"), TEST("Test"), DEFAULT("Default");
    ;

    private String value;

    private Roles(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getStatus(Roles status) {

        String userRoles = null;
        for (Roles role : Roles.values()) {
            if (role.equals(status)) {
                userRoles = role.getValue();
            }
        }
        return userRoles;
    }

}
