package com.itorix.apiwiz.design.studio.model;

public enum OPERATION {
  CREATE("Create"),
  UPDATE("Update"),
  DELETE("Delete");

  private final String value;

  OPERATION(String value) {
    this.value = value;
  }

  public static OPERATION fromValue(String value) {
    OPERATION operation = null;
    for (OPERATION operatiON : OPERATION.values()) {
      if (operatiON.getValue().equals(value)) {
        operation = operatiON;
      }
    }
    return operation;
  }

  public String getValue() {
    return value;
  }
}
