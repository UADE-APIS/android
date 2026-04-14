package com.example.xplorenow;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmailValidationTest {
    @Test
    public void email_basicValidation() {
        assertTrue(isValidEmail("a@b.com"));
        assertFalse(isValidEmail(""));
        assertFalse(isValidEmail("no-email"));
        assertFalse(isValidEmail("a@b"));
    }

    private boolean isValidEmail(String email) {
        // Minimal validation we will also use in UI layer
        return email != null && email.contains("@") && email.contains(".") && email.indexOf('@') < email.lastIndexOf('.');
    }
}

