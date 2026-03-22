package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.CertificationMapper;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CertificationStatusTest {

    @Test
    void nullExpiry_isActive() {
        assertEquals(CertificationStatus.ACTIVE, CertificationMapper.computeStatus(null));
    }

    @Test
    void pastExpiry_isExpired() {
        assertEquals(CertificationStatus.EXPIRED, CertificationMapper.computeStatus(LocalDate.now().minusDays(1)));
    }

    @Test
    void withinThirtyDays_isExpiringSoon() {
        assertEquals(CertificationStatus.EXPIRING_SOON, CertificationMapper.computeStatus(LocalDate.now().plusDays(10)));
        assertEquals(CertificationStatus.EXPIRING_SOON, CertificationMapper.computeStatus(LocalDate.now().plusDays(30)));
    }

    @Test
    void beyondThirtyDays_isActive() {
        assertEquals(CertificationStatus.ACTIVE, CertificationMapper.computeStatus(LocalDate.now().plusDays(31)));
    }
}
