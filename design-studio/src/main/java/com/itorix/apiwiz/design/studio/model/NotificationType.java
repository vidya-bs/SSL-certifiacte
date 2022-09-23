package com.itorix.apiwiz.design.studio.model;

public enum NotificationType {

    SWAGGER("Swagger"), DATA_DICTIONARY("Data Dictionary"), MODEL("Model");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public static NotificationType fromValue(String value) {
        NotificationType notify = null;
        for (NotificationType notification : NotificationType.values()) {
            if (notification.getValue().equals(value)) {
                notify = notification;
            }
        }
        return notify;
    }

    public String getValue() {
        return value;
    }


}
