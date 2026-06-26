package com.dev.ultron.controller;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HealthGraphQLController {

    @QueryMapping
    public String status() {
        return "OK";
    }
}
