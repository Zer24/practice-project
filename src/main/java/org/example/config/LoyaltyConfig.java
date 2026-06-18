package org.example.config;

import org.example.domain.DiscountContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoyaltyConfig {

    @Bean
    public DiscountContext discountContext() {
        return new DiscountContext();
    }
}