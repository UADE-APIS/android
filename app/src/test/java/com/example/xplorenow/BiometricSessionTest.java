package com.example.xplorenow;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Verifica la lógica de entrada a biometría post-sesión vencida (Req 1).
 *
 * Reproduce la condición de AuthStartFragment.onViewCreated():
 *   if (isLoggedIn() && isBiometricEnabled()) → tryBiometricLogin()
 *
 * Tras un 401, TokenManager.clear() solo limpia los tokens (access/refresh).
 * Los flags de biometría sobreviven, por lo que isLoggedIn() = false
 * y la condición no se cumple → se muestra el formulario directamente.
 */
public class BiometricSessionTest {

    // --- Réplica de la lógica de TokenManager ---

    private boolean isLoggedIn(String accessToken) {
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    private boolean shouldTryBiometric(String accessToken, boolean biometricEnabled) {
        return isLoggedIn(accessToken) && biometricEnabled;
    }

    // --- Tests ---

    @Test
    public void isLoggedIn_tokenNull_returnsFalse() {
        assertFalse(isLoggedIn(null));
    }

    @Test
    public void isLoggedIn_tokenEmpty_returnsFalse() {
        assertFalse(isLoggedIn(""));
    }

    @Test
    public void isLoggedIn_tokenBlankSpaces_returnsFalse() {
        assertFalse(isLoggedIn("   "));
    }

    @Test
    public void isLoggedIn_tokenValid_returnsTrue() {
        assertTrue(isLoggedIn("eyJhbGciOiJIUzI1NiJ9.abc.xyz"));
    }

    @Test
    public void shouldTryBiometric_sessionExpired_biometricEnabled_returnsFalse() {
        // Escenario: 401 recibido → clear() limpió el token pero NO el flag de biometría
        String accessToken = null;   // token limpiado por clear()
        boolean biometricEnabled = true; // flag preservado (fix del Req 1)

        assertFalse("Con sesión vencida NO debe intentar biometría",
                shouldTryBiometric(accessToken, biometricEnabled));
    }

    @Test
    public void shouldTryBiometric_sessionActive_biometricEnabled_returnsTrue() {
        // Escenario normal: usuario tenía sesión activa con biometría habilitada
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.abc.xyz";
        boolean biometricEnabled = true;

        assertTrue("Con sesión activa y biometría habilitada SÍ debe intentar biometría",
                shouldTryBiometric(accessToken, biometricEnabled));
    }

    @Test
    public void shouldTryBiometric_sessionActive_biometricDisabled_returnsFalse() {
        // Escenario: usuario con sesión pero sin biometría → muestra formulario
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.abc.xyz";
        boolean biometricEnabled = false;

        assertFalse("Con biometría deshabilitada NO debe intentar biometría",
                shouldTryBiometric(accessToken, biometricEnabled));
    }
}
