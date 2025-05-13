package com.salesflow.auth.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.salesflow.auth.dto.RegisterRequest;

@Component
public class TenantValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RegisterRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegisterRequest request = (RegisterRequest) target;
        
        if (request.getTenantId() != null) {
            // Validate tenant ID format (you can customize this based on your requirements)
            if (!request.getTenantId().matches("^[a-zA-Z0-9-_]+$")) {
                errors.rejectValue("tenantId", "invalid.tenantId", 
                    "Tenant ID can only contain letters, numbers, hyphens, and underscores");
            }
            
            // Validate tenant ID length
            if (request.getTenantId().length() < 3 || request.getTenantId().length() > 50) {
                errors.rejectValue("tenantId", "invalid.tenantId.length", 
                    "Tenant ID must be between 3 and 50 characters");
            }
        }
    }
} 