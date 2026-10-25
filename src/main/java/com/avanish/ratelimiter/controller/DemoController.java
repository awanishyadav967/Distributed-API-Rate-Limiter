package com.avanish.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {
    @GetMapping("/hello")
    public Map<String,String> hello(){
        return Map.of("Message","Hello, Your request was successfull");
    }
}
