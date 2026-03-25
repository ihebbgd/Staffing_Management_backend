package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.model.enums.UtilizationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilizationStatusTest {

    @Test
    void zeroUtilization_isAvailable() {
        assertEquals(UtilizationStatus.AVAILABLE, UtilizationStatus.of(0.0));
    }

    @Test
    void betweenZeroAnd100_isPartiallyAllocated() {
        assertEquals(UtilizationStatus.PARTIALLY_ALLOCATED, UtilizationStatus.of(0.1));
        assertEquals(UtilizationStatus.PARTIALLY_ALLOCATED, UtilizationStatus.of(57.5));
        assertEquals(UtilizationStatus.PARTIALLY_ALLOCATED, UtilizationStatus.of(99.9));
    }

    @Test
    void exactly100_isFullyAllocated() {
        assertEquals(UtilizationStatus.FULLY_ALLOCATED, UtilizationStatus.of(100.0));
    }

    @Test
    void above100_isOverallocated() {
        assertEquals(UtilizationStatus.OVERALLOCATED, UtilizationStatus.of(100.1));
        assertEquals(UtilizationStatus.OVERALLOCATED, UtilizationStatus.of(150.0));
    }
}
