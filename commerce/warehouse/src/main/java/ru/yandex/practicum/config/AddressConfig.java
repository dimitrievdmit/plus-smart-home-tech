package ru.yandex.practicum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class AddressConfig {
    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};

    @Bean
    public String currentAddress() {
        return ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];
    }
}