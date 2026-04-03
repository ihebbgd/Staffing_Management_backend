package com.demo.staffing_management_backend.exception;

/**
 * Thrown when an authenticated caller is not allowed to perform a specific business
 * operation (e.g. an admin editing their own account). Unlike Spring Security's
 * {@code AccessDeniedException} — which is kept deliberately generic so it never leaks
 * why access was refused — this carries a user-facing message that IS meant to be shown.
 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
