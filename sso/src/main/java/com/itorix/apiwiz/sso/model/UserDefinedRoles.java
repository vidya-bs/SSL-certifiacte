package com.itorix.apiwiz.sso.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDefinedRoles {
    String developer;
    String admin;
    String portal;
    String analyst;
    String projectAdmin;
    String qa;
    String operation;
    String test;
}
