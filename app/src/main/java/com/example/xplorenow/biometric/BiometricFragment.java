package com.example.xplorenow.biometric;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.xplorenow.R;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executor;

public class BiometricFragment extends Fragment {

    private TextView tvStatus;
    private MaterialButton btnAuthenticate;
    private MaterialButton btnGoToSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_biometric, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatus = view.findViewById(R.id.tvStatus);
        btnAuthenticate = view.findViewById(R.id.btnAuthenticate);
        btnGoToSettings = view.findViewById(R.id.btnGoToSettings);

        checkBiometricAvailability();

        btnAuthenticate.setOnClickListener(v -> showBiometricPrompt());

        btnGoToSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            startActivity(intent);
        });
    }

    private void checkBiometricAvailability() {
        BiometricManager manager = BiometricManager.from(requireContext());
        int result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        switch (result) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                tvStatus.setText("Estado: Listo para autenticar");
                btnAuthenticate.setEnabled(true);
                btnGoToSettings.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                tvStatus.setText("Estado: Sin sensor biométrico");
                btnAuthenticate.setEnabled(false);
                btnGoToSettings.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                tvStatus.setText("Estado: Sensor no disponible");
                btnAuthenticate.setEnabled(false);
                btnGoToSettings.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                tvStatus.setText("Estado: No hay biometría enrolada");
                btnAuthenticate.setEnabled(false);
                btnGoToSettings.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        tvStatus.setText("Estado: Autenticación exitosa!");
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        tvStatus.setText("Estado: Intento fallido. Reintenta.");
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        tvStatus.setText("Estado: Error - " + errString);
                    }
                }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Práctica Biometría")
                .setSubtitle("Confirmá tu identidad")
                .setDescription("Usá tu huella digital o el PIN del dispositivo")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
