package com.example.xplorenow;

import org.junit.Test;

import static org.junit.Assert.*;

public class OtpValidationTest {
    @Test
    public void otp_mustBeSixDigits() {
        assertTrue(isValidOtp("123456"));
        assertFalse(isValidOtp(""));
        assertFalse(isValidOtp("12345"));
        assertFalse(isValidOtp("1234567"));
        assertFalse(isValidOtp("12a456"));
    }

    private boolean isValidOtp(String code) {
        if (code == null || code.length() != 6) return false;
        for (int i = 0; i < code.length(); i++) {
            if (!Character.isDigit(code.charAt(i))) return false;
        }
        return true;
    }
}

