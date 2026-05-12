package com.sujan.studysync.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String frontendUrl;

    private Jwt jwt = new Jwt();
    private Cloudinary cloudinary = new Cloudinary();
    private Stripe stripe = new Stripe();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpiry;
        private long refreshTokenExpiry;
    }

    @Getter
    @Setter
    public static class Cloudinary {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
    }

    @Getter
    @Setter
    public static class Stripe {
        private String secretKey;
        private String webhookSecret;
        private String proPriceId;
    }
}
