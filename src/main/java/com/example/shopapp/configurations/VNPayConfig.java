package com.example.shopapp.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")// map các cấu hình trong file application
@Data
public class VNPayConfig {
    private String vnpUrl;
    private String vnpReturnUrl;
    private String vnpTmnCode;
    private String vnpHashSecret;
    private String vnpApiUrl;
    private String vnpVersion = "2.1.0";
    private String vnpCommand = "pay";
    private String orderType = "other";
}
