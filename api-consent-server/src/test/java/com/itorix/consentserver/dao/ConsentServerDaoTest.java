package com.itorix.consentserver.dao;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ConsentServerDaoTest {

    @Test
    public void checkListMatch() {

        List<String> scopes = new ArrayList<>();
        scopes.add("firstName");
        scopes.add("lastName");

        List<String> scopeCategory = new ArrayList<>();
        scopeCategory.add("firstName");
        scopeCategory.add("lastName");

        scopes.forEach(s -> {
            if(!scopeCategory.contains(s)) {
                System.out.println("No Match " + s);
            }
        });

    }

}