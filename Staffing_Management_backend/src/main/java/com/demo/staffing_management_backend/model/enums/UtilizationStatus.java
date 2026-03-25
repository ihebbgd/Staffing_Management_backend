package com.demo.staffing_management_backend.model.enums;

public enum UtilizationStatus {
    AVAILABLE,
    PARTIALLY_ALLOCATED,
    FULLY_ALLOCATED,
    OVERALLOCATED;

    public static UtilizationStatus of(double utilizationPercent) {
        if (utilizationPercent <= 0.0) {
            return AVAILABLE;
        }
        if (utilizationPercent < 100.0) {
            return PARTIALLY_ALLOCATED;
        }
        if (utilizationPercent == 100.0) {
            return FULLY_ALLOCATED;
        }
        return OVERALLOCATED;
    }
}
