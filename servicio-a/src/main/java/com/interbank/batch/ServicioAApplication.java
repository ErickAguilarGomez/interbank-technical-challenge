package com.interbank.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.interbank.batch", "com.interbank.common"})
@EntityScan(basePackages = "com.interbank.common.entity")
@EnableJpaRepositories(basePackages = "com.interbank.common.repository")
public class ServicioAApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioAApplication.class, args);
    }
}

