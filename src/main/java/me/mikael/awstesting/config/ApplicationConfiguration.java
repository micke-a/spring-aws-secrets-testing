package me.mikael.awstesting.config;

import io.awspring.cloud.secretsmanager.AwsSecretsManagerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 *
 */
@Slf4j
@Configuration
public class ApplicationConfiguration {
    @Value("${key1:not-set}")
    private String key1;

    @Value("${key2:not-set}")
    private String key2;

    @Value("${app.message:not-set}")
    private String appMessage;


    @Value("${app.other-message:not-set}")
    private String appOtherMessage;

    @Bean
    Map<String,String> testingMap() {
        log.info("Loaded secrets - key1:{}, key2:{}, app.message:{}, app.other-message: {}",
            key1, key2, appMessage, appOtherMessage);
        return Map.of("key1", key1, "key2", key2, "appMessage", appMessage);
    }
}
