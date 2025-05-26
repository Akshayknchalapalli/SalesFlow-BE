package com.salesflow.registry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Service Registry Test Controller is working!";
    }

    @GetMapping("/status")
    public String status() {
        return "Service Registry is up and running";
    }
}