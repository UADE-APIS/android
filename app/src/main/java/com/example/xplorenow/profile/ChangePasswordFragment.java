package com.example.xplorenow.profile;

import android.content.res.ColorStateList;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.User;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.ChangePasswordRequest;
import com.example.xplorenow.data.network.dto.LoginOtpRequest;
import com.example.xplorenow.data.network.dto.MeResponseData;
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
public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "CHANGE_PASSWORD";

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private TextView tvOtpHint;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etOtpCode;
    private TextView tvOtpCooldown;
    private Button btnSendOtp;
    private String email;
    private CountDownTimer otpCooldownTimer;
    private ColorStateList defaultSendOtpTint;

    public ChangePasswordFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        tvOtpHint = view.findViewById(R.id.tvOtpHint);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etOtpCode = view.findViewById(R.id.etOtpCode);
        tvOtpCooldown = view.findViewById(R.id.tvOtpCooldown);

        Button btnBack = view.findViewById(R.id.btnBackChangePassword);
        btnSendOtp = view.findViewById(R.id.btnSendOtp);
        Button btnConfirmChangePassword = view.findViewById(R.id.btnConfirmChangePassword);

        defaultSendOtpTint = btnSendOtp.getBackgroundTintList();

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        btnSendOtp.setOnClickListener(v -> requestOtp());
        btnConfirmChangePassword.setOnClickListener(v -> verifyOtpAndChangePassword());

        loadCurrentUserEmail();

        return view;
    }

    private void loadCurrentUserEmail() {
        apiService.getMe().enqueue(new Callback<WrappedResponse<MeResponseData>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<MeResponseData>> call,
                                   @NonNull Response<WrappedResponse<MeResponseData>> response) {
                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    tvOtpHint.setText(getString(R.string.profile_change_password_email_missing));
                    return;
                }

                User user = response.body().getData().getUser();
                email = user != null ? user.getEmail() : null;

                if (TextUtils.isEmpty(email)) {
                    tvOtpHint.setText(getString(R.string.profile_change_password_email_missing));
                } else {
                    tvOtpHint.setText(getString(R.string.profile_change_password_otp_hint, email));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<MeResponseData>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error cargando email", t);
                tvOtpHint.setText(getString(R.string.profile_change_password_email_missing));
            }
        });
    }

    private void requestOtp() {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), R.string.profile_change_password_email_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        startOtpCooldown();

        apiService.requestLoginOtp(new RequestOtpRequest(email)).enqueue(new Callback<WrappedResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<Void>> call,
                                   @NonNull Response<WrappedResponse<Void>> response) {
                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.profile_change_password_otp_sent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.profile_change_password_otp_error, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error enviando OTP", t);
                Toast.makeText(getContext(), R.string.error_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startOtpCooldown() {
        if (otpCooldownTimer != null) {
            otpCooldownTimer.cancel();
        }

        btnSendOtp.setEnabled(false);
        btnSendOtp.setAlpha(0.75f);
        btnSendOtp.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
        tvOtpCooldown.setVisibility(View.VISIBLE);

        otpCooldownTimer = new CountDownTimer(8000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (long) Math.ceil(millisUntilFinished / 1000.0);
                tvOtpCooldown.setText(getString(R.string.profile_change_password_otp_cooldown, seconds));
            }

            @Override
            public void onFinish() {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setAlpha(1f);
                if (defaultSendOtpTint != null) {
                    btnSendOtp.setBackgroundTintList(defaultSendOtpTint);
                }
                tvOtpCooldown.setVisibility(View.GONE);
            }
        };

        otpCooldownTimer.start();
    }

    @Override
    public void onDestroyView() {
        if (otpCooldownTimer != null) {
            otpCooldownTimer.cancel();
            otpCooldownTimer = null;
        }
        super.onDestroyView();
    }

    private void verifyOtpAndChangePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String otpCode = etOtpCode.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(otpCode)) {
            Toast.makeText(getContext(), R.string.profile_change_password_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), R.string.profile_change_password_email_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.verifyLoginOtp(new LoginOtpRequest(email, otpCode))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call,
                                           @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        if (response.code() == 401) {
                            handleUnauthorized();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Toast.makeText(getContext(), getString(R.string.profile_change_password_invalid_otp, response.code()), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AuthTokensResponse tokens = response.body().getData();
                        if (tokens.access != null && tokens.refresh != null) {
                            tokenManager.saveTokens(tokens.access, tokens.refresh);
                        }

                        changePassword(oldPassword, newPassword);
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error verificando OTP", t);
                        Toast.makeText(getContext(), R.string.error_connection, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changePassword(String oldPassword, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);

        apiService.changePassword(request).enqueue(new Callback<WrappedResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<Void>> call,
                                   @NonNull Response<WrappedResponse<Void>> response) {
                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.profile_change_password_success, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(ChangePasswordFragment.this).navigateUp();
                } else {
                    Toast.makeText(getContext(), getString(R.string.profile_change_password_error, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error cambiando contraseña", t);
                Toast.makeText(getContext(), R.string.error_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUnauthorized() {
        tokenManager.clear();
        if (!isAdded()) {
            return;
        }
        Toast.makeText(getContext(), "Sesión vencida. Iniciá sesión de nuevo.", Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(this).navigate(R.id.authStartFragment);
    }
}

