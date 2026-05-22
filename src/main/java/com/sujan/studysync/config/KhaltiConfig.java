package com.sujan.studysync.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// All Khalti config in one place
// Values come from application.yml / environment variables
@Configuration
@ConfigurationProperties(prefix = "app.khalti")
@Getter
@Setter
public class KhaltiConfig {

    // Your Khalti merchant secret key
    // Sandbox: get from https://test-admin.khalti.com
    private String secretKey;

    // Sandbox: https://dev.khalti.com/api/v2/epayment/initiate/
    // Production: https://khalti.com/api/v2/epayment/initiate/
    private String initiateUrl;

    // Sandbox: https://dev.khalti.com/api/v2/epayment/lookup/
    // Production: https://khalti.com/api/v2/epayment/lookup/
    private String lookupUrl;

    // Our backend URL — where Khalti redirects after payment
    // Must be EXACTLY registered in your Khalti merchant settings
    private String returnUrl;

    // Our website URL — shown on Khalti's payment page
    private String websiteUrl;
}
