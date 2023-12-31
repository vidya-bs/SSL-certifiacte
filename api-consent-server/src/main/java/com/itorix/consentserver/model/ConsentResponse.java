package com.itorix.consentserver.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConsentResponse {

    private List<Consent> consentList;
    private Pagination pagination;
}
