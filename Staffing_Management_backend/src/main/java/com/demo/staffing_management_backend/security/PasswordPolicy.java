package com.demo.staffing_management_backend.security;

/**
 * Single source of truth for the login-password rules, shared by every flow that sets a password
 * (user creation, employee provisioning, password reset) so the requirement can never drift apart.
 */
public final class PasswordPolicy {

    /** Minimum length for an admin- or user-chosen login password. */
    public static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }
}
