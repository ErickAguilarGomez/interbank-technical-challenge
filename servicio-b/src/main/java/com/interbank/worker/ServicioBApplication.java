package com.interbank.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.interbank.worker", "com.interbank.common"})
@EntityScan(basePackages = "com.interbank.common.entity")
@EnableJpaRepositories(basePackages = "com.interbank.common.repository")
public class ServicioBApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioBApplication.class, args);
    }
}

