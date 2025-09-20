package com.navexa.secretbuddy.api;

import com.navexa.secretbuddy.core.service.TokenService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication(scanBasePackages = "com.navexa.secretbuddy")
public class SecretBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretBuddyApplication.class, args);
    }


    @Bean
    public TokenService tokenService(@Value("${secretbuddy.token.secret}") String secret) {
        return new TokenService(secret);
    }
}