package com.demo.staffing_management_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // use test-profile config; excludes seeders so tests never mutate data
class StaffingManagementBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
