package me.mikael.awstesting.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 */
@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloController {

    @Value("${app.message}")
    private String appMessage;

    @Value("${key1:not-set}")
    private String key1;

    @Value("${key2:not-set}")
    private String key2;

    @GetMapping("aws")
    public Map<String, String> awsGet(){
        return Map.of("key1", key1, "key2", key2);
    }
}
