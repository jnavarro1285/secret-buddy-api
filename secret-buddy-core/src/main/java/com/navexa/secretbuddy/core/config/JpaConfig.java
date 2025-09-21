package com.navexa.secretbuddy.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.navexa.secretbuddy.core.repository")
@EntityScan(basePackages = "com.navexa.secretbuddy.core.model")
public class JpaConfig {}