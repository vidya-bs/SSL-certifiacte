package com.itorix.apiwiz.portfolio.model.db.proxy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class PolicyCategory
{
    private String type;

    private String name;

    private String description;

    private List<Policies> policies;

}



