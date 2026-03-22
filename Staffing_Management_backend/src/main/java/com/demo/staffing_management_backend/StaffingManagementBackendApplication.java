package com.demo.staffing_management_backend;

import com.demo.staffing_management_backend.config.RecommendationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
@EnableConfigurationProperties(RecommendationProperties.class)
public class StaffingManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaffingManagementBackendApplication.class, args);
    }

}
