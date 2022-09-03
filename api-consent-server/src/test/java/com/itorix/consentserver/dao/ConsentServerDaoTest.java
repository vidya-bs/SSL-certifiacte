package com.itorix.consentserver.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
@Slf4j
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
                log.info("No Match " + s);
            }
        });

    }

}