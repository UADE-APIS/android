package com.example.xplorenow.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LoginClassicRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;
import com.example.xplorenow.data.session.TokenManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

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

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
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

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int dp24 = Math.round(24 * getResources().getDisplayMetrics().density);
            v.setPadding(dp24 + bars.left, bars.top, dp24 + bars.right, dp24 + bars.bottom);
            return insets;
        });

        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        progress = view.findViewById(R.id.progress);

        if (tokenManager.isLoggedIn() && tokenManager.isBiometricEnabled()) {
            tryBiometricLogin(view);
            return;
        }

        if (tokenManager.isLoggedIn()) {
            view.post(() -> {
                if (!isAdded()) return;
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_home);
            });
            return;
        }

        if (getArguments() != null && getArguments().getBoolean("register_success", false)) {
            Snackbar.make(view, R.string.register_success, Snackbar.LENGTH_LONG).show();
        }

        view.findViewById(R.id.btnIngresar).setOnClickListener(v -> doLogin(view));
        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_requestOtp));
        view.findViewById(R.id.btnLoginOtp).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_loginEmail));
    }

    private void tryBiometricLogin(View rootView) {
        int canAuth = BiometricManager.from(requireContext())
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            tokenManager.setBiometricEnabled(false);
            Log.w(TAG, "Biometric not available, fallback to password. Code: " + canAuth);
            showLoginForm(rootView);
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (!isAdded()) return;
                        Navigation.findNavController(rootView).navigate(R.id.action_authStart_to_home);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        showLoginForm(rootView);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
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

    private void showLoginForm(View view) {
        if (!isAdded()) return;
        view.findViewById(R.id.btnIngresar).setOnClickListener(v -> doLogin(view));
        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_requestOtp));
        view.findViewById(R.id.btnLoginOtp).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_loginEmail));
    }

    private void doLogin(View rootView) {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = tilEmail.getEditText() != null
                ? tilEmail.getEditText().getText().toString().trim() : "";
        String password = tilPassword.getEditText() != null
                ? tilPassword.getEditText().getText().toString() : "";

        if (!isValidEmail(email)) {
            tilEmail.setError("Correo inválido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es obligatoria");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        api.loginClassic(new LoginClassicRequest(email, password))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call,
                                           @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        progress.setVisibility(View.GONE);
                        if (!isAdded()) return;

                        if (response.code() == 400 || response.code() == 401) {
                            tilPassword.setError("Contraseña incorrecta");
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getData() == null) {
                            Snackbar.make(rootView, "Error al iniciar sesión", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        AuthTokensResponse tokens = response.body().getData();
                        tokenManager.saveTokens(tokens.access, tokens.refresh);

                        if (!tokenManager.hasAskedBiometric() && !tokenManager.isBiometricEnabled()) {
                            offerBiometricEnrollment(rootView);
                        } else {
                            navigateToHome(rootView);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call,
                                          @NonNull Throwable t) {
                        progress.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }



    private void offerBiometricEnrollment(View rootView) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.biometric_enable_title))
                .setMessage(getString(R.string.biometric_enable_message))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    tokenManager.setBiometricEnabled(true);
                    tokenManager.setAskedBiometric(true);
                    navigateToHome(rootView);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    tokenManager.setBiometricEnabled(false);
                    tokenManager.setAskedBiometric(true);
                    navigateToHome(rootView);
                })
                .setCancelable(false)
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
