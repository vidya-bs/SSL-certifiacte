package com.itorix.apiwiz.design.studio.model;

public enum Status {

    Draft("Draft"),
    Publish("Publish"),
    Review("Review"),
    Change_Required("Change Required"),
    Approved("Approved"),
    Deprecate("Deprecate"),
    Retired("Retired");
    private String status;

    Status(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }

  }