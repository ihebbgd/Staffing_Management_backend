package com.demo.staffing_management_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recommendation")
public record RecommendationProperties(Weights weights, double experienceCapYears) {

    public RecommendationProperties {
        if (weights == null) {
            weights = new Weights(0.40, 0.25, 0.20, 0.10, 0.05);
        }
        if (experienceCapYears <= 0) {
            experienceCapYears = 10.0;
        }
    }

    public record Weights(double skillMatch, double skillLevel, double availability, double cert, double experience) {
    }
}
