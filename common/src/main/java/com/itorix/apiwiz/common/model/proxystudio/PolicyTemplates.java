package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PolicyTemplates
{
    private List<Category> categories;

    public List<Category> getCategories ()
    {
        return categories;
    }

    public void setCategories (List<Category> categories)
    {
        this.categories = categories;
    }
 
}
