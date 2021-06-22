package com.springraft.simplegetservlet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final String response = "Hello World! From Servlet";

    @GetMapping("/hello")
    public String hello() {
        return response;
    }

}
