package com.sujan.studysync;

import com.sujan.studysync.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync          // ← allows @Async on EmailService methods
@EnableConfigurationProperties(AppProperties.class)
public class StudysyncBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudysyncBackendApplication.class, args);
    }

}
