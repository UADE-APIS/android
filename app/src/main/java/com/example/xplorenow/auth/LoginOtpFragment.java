package com.example.xplorenow.auth;

import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LoginOtpRequest;
import com.example.xplorenow.data.network.dto.RequestOtpRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;
import com.example.xplorenow.data.session.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginOtpFragment extends Fragment {

    @Inject
    ApiService api;
    @Inject
    TokenManager tokenManager;
    private EditText etCode;
    private TextView tvError;
    private ProgressBar progress;

    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
        etCode = view.findViewById(R.id.etCode);
        tvError = view.findViewById(R.id.tvError);
        progress = view.findViewById(R.id.progress);

        email = getArguments() != null ? getArguments().getString("email", "") : "";
        tvSubtitle.setText("Enviamos un código a " + email);

        Button btnRequest = view.findViewById(R.id.btnRequestOtp);
        Button btnVerify = view.findViewById(R.id.btnVerify);

        btnRequest.setOnClickListener(v -> requestOtp());
        btnVerify.setOnClickListener(v -> verifyOtp(view));

        if (!TextUtils.isEmpty(email)) {
            requestOtp();
        }
    }

    private void requestOtp() {
        tvError.setText("");
        if (TextUtils.isEmpty(email)) {
            tvError.setText("Email faltante.");
            return;
        }
        setLoading(true);
        api.requestLoginOtp(new RequestOtpRequest(email)).enqueue(new Callback<WrappedResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<Void>> call, @NonNull Response<WrappedResponse<Void>> response) {
                setLoading(false);
                if (!response.isSuccessful()) {
                    tvError.setText("No se pudo enviar OTP (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                setLoading(false);
                tvError.setText("Error de red: " + t.getMessage());
            }
        });
    }

    private void verifyOtp(View rootView) {
        tvError.setText("");
        String code = etCode.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() != 6) {
            tvError.setText("El código debe tener 6 dígitos.");
            return;
        }
        setLoading(true);
        api.verifyLoginOtp(new LoginOtpRequest(email, code))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        setLoading(false);
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            tvError.setText("OTP inválido o expirado (" + response.code() + ").");
                            return;
                        }
                        AuthTokensResponse tokens = response.body().getData();
                        tokenManager.saveTokens(tokens.access, tokens.refresh);

                        if (!tokenManager.isBiometricEnabled()) {
                            offerBiometricEnrollment(rootView);
                        } else {
                            navigateToHome(rootView);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Throwable t) {
                        setLoading(false);
                        tvError.setText("Error de red: " + t.getMessage());
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
                    tokenManager.saveEncryptedToken(tokenManager.getAccessToken());
                    navigateToHome(rootView);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> navigateToHome(rootView))
                .setCancelable(false)
                .show();
    }

    private void navigateToHome(View rootView) {
        rootView.post(() -> {
            if (!isAdded()) return;
            Navigation.findNavController(rootView).navigate(R.id.action_loginOtp_to_home);
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
