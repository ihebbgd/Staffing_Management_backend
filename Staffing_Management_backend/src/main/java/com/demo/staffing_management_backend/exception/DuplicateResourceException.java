package com.demo.staffing_management_backend.exception;

public class DuplicateResourceException extends  RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
