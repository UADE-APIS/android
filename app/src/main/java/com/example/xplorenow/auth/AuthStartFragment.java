package com.example.xplorenow.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LoginClassicRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;
import com.example.xplorenow.data.session.TokenManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AuthStartFragment extends Fragment {

    private static final String TAG = "AuthStartFragment";

    @Inject
    ApiService api;
    @Inject
    TokenManager tokenManager;

    // Las vistas del fragment se guardan como campos porque
    // doLogin y el callback de biometría también las necesitan
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvError = view.findViewById(R.id.tvError);
        progress = view.findViewById(R.id.progress);

        // Si ya tiene sesión y biometría activada, intentar autenticar con huella
        if (tokenManager.isLoggedIn() && tokenManager.isBiometricEnabled()) {
            tryBiometricLogin(view);
            return;
        }

        // Si ya tiene sesión pero sin biometría, ir directo a Home
        if (tokenManager.isLoggedIn()) {
            view.post(() -> {
                if (!isAdded()) return;
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_home);
            });
            return;
        }

        // BUG-01: mostrar mensaje de éxito si viene del registro
        if (getArguments() != null && getArguments().getBoolean("register_success", false)) {
            tvError.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
            tvError.setText(R.string.register_success);
            tvError.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.btnIngresar).setOnClickListener(v -> doLogin(view));
        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_requestOtp));

        // BUG-03: botón para acceder al login por OTP
        view.findViewById(R.id.btnLoginOtp).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_loginEmail));
    }

    // ─── Biometría ────────────────────────────────────────────────────────────

    private void tryBiometricLogin(View rootView) {
        int canAuth = BiometricManager.from(requireContext())
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // El dispositivo no puede autenticar (no tiene biometría configurada)
            // Ir al login normal y desactivar biometría
            tokenManager.setBiometricEnabled(false);
            Log.w(TAG, "Biometric not available, fallback to password. Code: " + canAuth);
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (!isAdded()) return;
                        Log.d(TAG, "Biometric authentication succeeded");
                        Navigation.findNavController(rootView).navigate(R.id.action_authStart_to_home);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // El usuario canceló o hubo un error — mostrar login normal
                        Log.w(TAG, "Biometric error: " + errString);
                        showLoginForm(rootView);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.w(TAG, "Biometric authentication failed");
                        // No hacer nada — el prompt sigue visible para reintentar
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    // Muestra el formulario de usuario/contraseña (fallback del biométrico)
    private void showLoginForm(View view) {
        if (!isAdded()) return;
        view.findViewById(R.id.btnIngresar).setOnClickListener(v -> doLogin(view));
        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_requestOtp));
        view.findViewById(R.id.btnLoginOtp).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_loginEmail));
    }

    // ─── Login clásico ────────────────────────────────────────────────────────

    private void doLogin(View rootView) {
        tvError.setText("");

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!isValidEmail(email)) {
            tvError.setText("Correo inválido.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tvError.setText("La contraseña es obligatoria.");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        api.loginClassic(new LoginClassicRequest(email, password))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call,
                                           @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        progress.setVisibility(View.GONE);
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getData() == null) {
                            tvError.setText("No se pudo iniciar sesión (" + response.code() + ").");
                            return;
                        }
                        AuthTokensResponse tokens = response.body().getData();
                        tokenManager.saveTokens(tokens.access, tokens.refresh);

                        // Preguntar si quiere activar biometría (solo si no estaba activada)
                        if (!tokenManager.isBiometricEnabled()) {
                            offerBiometricEnrollment(rootView);
                        } else {
                            navigateToHome(rootView);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call,
                                          @NonNull Throwable t) {
                        progress.setVisibility(View.GONE);
                        tvError.setText("Error de red: " + t.getMessage());
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    // Después del login exitoso: ofrecer activar biometría
    private void offerBiometricEnrollment(View rootView) {
        if (!isAdded()) return;

        int canAuth = BiometricManager.from(requireContext())
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // El dispositivo no soporta biometría → ir directo a Home
            navigateToHome(rootView);
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.biometric_enable_title))
                .setMessage(getString(R.string.biometric_enable_message))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    tokenManager.setBiometricEnabled(true);
                    navigateToHome(rootView);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> navigateToHome(rootView))
                .show();
    }

    private void navigateToHome(View rootView) {
        rootView.post(() -> {
            if (!isAdded()) return;
            Navigation.findNavController(rootView).navigate(R.id.action_authStart_to_home);
        });
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}
