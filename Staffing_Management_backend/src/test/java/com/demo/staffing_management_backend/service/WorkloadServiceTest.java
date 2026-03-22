package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.model.Allocation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadServiceTest {

    private final WorkloadService workloadService = new WorkloadService(null);

    @Test
    void overlaps_dateInsideWindow_counts() {
        Allocation a = Allocation.builder()
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .build();
        assertTrue(WorkloadService.overlaps(a, LocalDate.now()));
    }

    @Test
    void overlaps_dateAfterEnd_doesNotCount() {
        Allocation a = Allocation.builder()
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(1))
                .build();
        assertFalse(WorkloadService.overlaps(a, LocalDate.now()));
    }

    @Test
    void overlaps_nullBounds_treatedAsOpenEnded() {
        Allocation a = Allocation.builder().build();
        assertTrue(WorkloadService.overlaps(a, LocalDate.now()));
    }

    @Test
    void round1_roundsToOneDecimal() {
        assertEquals(125.0, workloadService.round1(125.04));
        assertEquals(57.1, workloadService.round1(57.142857));
    }
}
