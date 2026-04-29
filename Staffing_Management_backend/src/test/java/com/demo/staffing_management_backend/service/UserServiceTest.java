package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.UserDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ForbiddenOperationException;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, employeeRepository, passwordEncoder, auditService);
    }

    @Test
    @DisplayName("create uses and encodes the chosen password and does not reveal it")
    void create_withChosenPassword_encodesAndHidesIt() {
        var request = new UserDtos.CreateUserRequest("jdoe", "j@d.com", UserRole.EMPLOYEE, "chosenPass1");
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("j@d.com")).thenReturn(false);
        when(passwordEncoder.encode("chosenPass1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = userService.create(request);

        assertThat(response.temporaryPassword()).isNull();
        verify(passwordEncoder).encode("chosenPass1");
    }

    @Test
    @DisplayName("create rejects a chosen password shorter than the minimum length")
    void create_withShortPassword_throws() {
        var request = new UserDtos.CreateUserRequest("jdoe", "j@d.com", UserRole.EMPLOYEE, "short");
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("j@d.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("an admin cannot change their own role")
    void changeRole_onSelf_throwsForbidden() {
        User self = User.builder().id("u1").role(UserRole.ADMIN).build();
        when(userRepository.findById("u1")).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> userService.changeRole("u1", UserRole.MANAGER, "u1"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    @DisplayName("an admin CAN change another admin's role")
    void changeRole_onAnotherAdmin_succeeds() {
        User target = User.builder().id("u2").username("other").role(UserRole.ADMIN).build();
        when(userRepository.findById("u2")).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.findByUserId("u2")).thenReturn(Optional.empty());

        var response = userService.changeRole("u2", UserRole.MANAGER, "u1");

        assertThat(response.role()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    @DisplayName("an admin cannot delete their own account")
    void delete_self_throwsForbidden() {
        User self = User.builder().id("u1").role(UserRole.ADMIN).build();
        when(userRepository.findById("u1")).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> userService.delete("u1", "u1"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    @DisplayName("deleting the last remaining administrator is rejected")
    void delete_lastAdmin_throwsForbidden() {
        User target = User.builder().id("u2").role(UserRole.ADMIN).build();
        when(userRepository.findById("u2")).thenReturn(Optional.of(target));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.delete("u2", "u1"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    @DisplayName("an admin CAN delete another admin when others remain")
    void delete_anotherAdmin_whenOthersRemain_succeeds() {
        User target = User.builder().id("u2").role(UserRole.ADMIN).build();
        when(userRepository.findById("u2")).thenReturn(Optional.of(target));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(2L);
        when(employeeRepository.findByUserId("u2")).thenReturn(Optional.empty());

        userService.delete("u2", "u1");

        verify(userRepository).delete(target);
    }

    @Test
    @DisplayName("reset encodes the chosen password and revokes existing tokens")
    void resetPassword_withChosenPassword_encodesAndBumpsTokenVersion() {
        User target = User.builder().id("u2").role(UserRole.EMPLOYEE).tokenVersion(3).build();
        when(userRepository.findById("u2")).thenReturn(Optional.of(target));
        when(passwordEncoder.encode("newSecret1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String revealed = userService.resetPassword("u2", "newSecret1", "u1");

        assertThat(revealed).isNull();
        assertThat(target.getPassword()).isEqualTo("hashed");
        assertThat(target.getTokenVersion()).isEqualTo(4);
    }
}
