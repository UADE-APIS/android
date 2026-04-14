package com.example.xplorenow.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.OtpRequest;
import com.example.xplorenow.data.model.VerifyOtpData;
import com.example.xplorenow.data.model.VerifyOtpRequest;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpFragment extends Fragment {

    private static final String TAG = "VerifyOtpFragment";
    // tiempo de espera entre reenvíos (30 segundos)
    private static final long RESEND_COOLDOWN_MS = 30_000;

    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // el email viene del fragment anterior
        String email = getArguments() != null
                ? getArguments().getString("email", "")
                : "";

        TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
        EditText etCodigo = view.findViewById(R.id.etCodigo);
        Button btnVerificar = view.findViewById(R.id.btnVerificar);
        TextView btnReenviar = view.findViewById(R.id.btnReenviar);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        tvSubtitle.setText("We sent a code to " + email);

        // arrancamos el cooldown apenas entra, el código ya fue enviado
        startResendCooldown(btnReenviar);

        btnVerificar.setOnClickListener(v -> {
            String code = etCodigo.getText().toString().trim();

            if (code.length() != 6) {
                showError(tvError, "The code must be 6 digits");
                return;
            }

            verifyOtp(view, email, code, progressBar, btnVerificar, tvError);
        });

        btnReenviar.setOnClickListener(v -> {
            if (!btnReenviar.isEnabled()) return;
            resendOtp(email, btnReenviar, tvError);
        });
    }

    // bloquea el botón de reenvío y hace el countdown visible
    private void startResendCooldown(TextView btnReenviar) {
        btnReenviar.setEnabled(false);

        countDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                btnReenviar.setText("Resend code in " + seconds + "s");
                btnReenviar.setAlpha(0.5f);
            }

            @Override
            public void onFinish() {
                btnReenviar.setEnabled(true);
                btnReenviar.setText("Didn't receive the code? Resend");
                btnReenviar.setAlpha(1f);
            }
        }.start();
    }

    private void verifyOtp(View view, String email, String code,
                           ProgressBar progressBar,
                           Button btnVerificar,
                           TextView tvError) {

        progressBar.setVisibility(View.VISIBLE);
        btnVerificar.setEnabled(false);
        tvError.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.verifyOtp(new VerifyOtpRequest(email, code))
                .enqueue(new Callback<ApiResponse<VerifyOtpData>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<VerifyOtpData>> call,
                                           @NonNull Response<ApiResponse<VerifyOtpData>> response) {
                        progressBar.setVisibility(View.GONE);
                        btnVerificar.setEnabled(true);

                        if (response.isSuccessful()) {
                            // código válido, navegamos al registro con el email
                            Bundle args = new Bundle();
                            args.putString("email", email);
                            Navigation.findNavController(view)
                                    .navigate(R.id.action_verifyOtp_to_register, args);
                        } else {
                            showError(tvError, "Incorrect or expired code. Please try again.");
                            Log.e(TAG, "verifyOtp error HTTP: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<VerifyOtpData>> call,
                                          @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnVerificar.setEnabled(true);
                        showError(tvError, "Connection error: " + t.getMessage());
                        Log.e(TAG, "verifyOtp onFailure: " + t.getMessage());
                    }
                });
    }

    private void resendOtp(String email, TextView btnReenviar, TextView tvError) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        // deshabilitamos de entrada para evitar doble tap
        btnReenviar.setEnabled(false);
        tvError.setVisibility(View.GONE);

        apiService.resendOtp(new OtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    tvError.setTextColor(
                            requireContext().getColor(android.R.color.holo_green_dark));
                    tvError.setText("Code resent successfully");
                    tvError.setVisibility(View.VISIBLE);
                    // reiniciamos el cooldown después de cada reenvío exitoso
                    startResendCooldown(btnReenviar);
                } else {
                    showError(tvError, "Could not resend the code. Please try again.");
                    btnReenviar.setEnabled(true);
                    btnReenviar.setAlpha(1f);
                    Log.e(TAG, "resendOtp error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                showError(tvError, "Connection error: " + t.getMessage());
                btnReenviar.setEnabled(true);
                btnReenviar.setAlpha(1f);
                Log.e(TAG, "resendOtp onFailure: " + t.getMessage());
            }
        });
    }

    private void showError(TextView tvError, String message) {
        tvError.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // cancelamos el timer para no tener memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}