package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.AuditLogDtos;
import com.demo.staffing_management_backend.model.AuditLog;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.repository.AuditLogRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {
    private static final String TARGET_USER = "USER";
    private static final String TARGET_EMPLOYEE = "EMPLOYEE";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    /** Records an audit entry attributed to the current principal (or "system"). Never log secrets in details. */
    public void record(String action, String targetType, String targetId, String details) {
        auditLogRepository.save(AuditLog.builder()
                .username(currentUsername())
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .timestamp(Instant.now())
                .build());
    }

    public Page<AuditLogDtos.AuditLogResponse> getAll(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        NameResolver names = resolveNamesFor(page.getContent());
        return page.map(log -> toResponse(log, names.nameFor(log)));
    }

    /**
     * Batch-loads the display names for every target referenced on this page in at most two
     * queries (one per target type), avoiding an N+1 lookup per audit row.
     */
    private NameResolver resolveNamesFor(List<AuditLog> logs) {
        Set<String> userIds = idsOfType(logs, TARGET_USER);
        Set<String> employeeIds = idsOfType(logs, TARGET_EMPLOYEE);

        Map<String, String> userNames = userIds.isEmpty() ? Map.of()
                : index(userRepository.findAllById(userIds), User::getId, User::getUsername);
        Map<String, String> employeeNames = employeeIds.isEmpty() ? Map.of()
                : index(employeeRepository.findAllById(employeeIds), Employee::getId,
                        e -> (e.getFirstName() + " " + e.getLastName()).trim());

        return new NameResolver(userNames, employeeNames);
    }

    private Set<String> idsOfType(List<AuditLog> logs, String targetType) {
        return logs.stream()
                .filter(log -> targetType.equals(log.getTargetType()) && log.getTargetId() != null)
                .map(AuditLog::getTargetId)
                .collect(Collectors.toSet());
    }

    private <T> Map<String, String> index(List<T> entities, Function<T, String> keyFn, Function<T, String> nameFn) {
        return entities.stream().collect(Collectors.toMap(keyFn, nameFn, (a, b) -> a));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            return "system";
        }
        return authentication.getName();
    }

    private AuditLogDtos.AuditLogResponse toResponse(AuditLog log, String targetName) {
        return new AuditLogDtos.AuditLogResponse(log.getId(), log.getUsername(), log.getAction(),
                log.getTargetType(), log.getTargetId(), targetName, log.getTimestamp(), log.getDetails());
    }

    /**
     * Resolves an audit entry's target to a human-readable name. Falls back to the raw id when the
     * referenced record no longer exists (e.g. after a delete) so the entry stays traceable.
     */
    private record NameResolver(Map<String, String> userNames, Map<String, String> employeeNames) {
        String nameFor(AuditLog log) {
            String id = log.getTargetId();
            if (id == null) {
                return null;
            }
            String resolved = switch (log.getTargetType() == null ? "" : log.getTargetType()) {
                case TARGET_USER -> userNames.get(id);
                case TARGET_EMPLOYEE -> employeeNames.get(id);
                default -> null;
            };
            return resolved != null && !resolved.isBlank() ? resolved : id;
        }
    }
}
