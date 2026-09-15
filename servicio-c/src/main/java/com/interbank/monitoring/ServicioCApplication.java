package com.interbank.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication(scanBasePackages = {"com.interbank.monitoring", "com.interbank.common"})
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class ServicioCApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioCApplication.class, args);
    }
}

