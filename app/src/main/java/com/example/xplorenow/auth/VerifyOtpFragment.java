package com.example.xplorenow.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class VerifyOtpFragment extends Fragment {

    private static final String TAG = "VerifyOtpFragment";

    @Inject
    ApiService apiService;

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

        String email = getArguments() != null
                ? getArguments().getString("email", "")
                : "";

        TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
        TextInputLayout tilCodigo = view.findViewById(R.id.tilCodigo);
        MaterialButton btnVerificar = view.findViewById(R.id.btnVerificar);
        MaterialButton btnReenviar = view.findViewById(R.id.btnReenviar);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        tvSubtitle.setText("Enviamos un código a " + email);

        startResendCooldown(btnReenviar);

        btnVerificar.setOnClickListener(v -> {
            tilCodigo.setError(null);
            String code = tilCodigo.getEditText() != null
                    ? tilCodigo.getEditText().getText().toString().trim() : "";

            if (code.length() != 6) {
                tilCodigo.setError("El código debe tener 6 dígitos");
                return;
            }

            verifyOtp(view, email, code, progressBar, btnVerificar);
        });

        btnReenviar.setOnClickListener(v -> {
            if (!btnReenviar.isEnabled()) return;
            resendOtp(view, email, btnReenviar);
        });
    }

    private void startResendCooldown(MaterialButton btnReenviar) {
        btnReenviar.setEnabled(false);

        countDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                btnReenviar.setText("Reenviar en " + seconds + "s");
                btnReenviar.setAlpha(0.5f);
            }

            @Override
            public void onFinish() {
                btnReenviar.setEnabled(true);
                btnReenviar.setText("¿No recibiste el código? Reenviar");
                btnReenviar.setAlpha(1f);
            }
        }.start();
    }

    private void verifyOtp(View view, String email, String code,
                           ProgressBar progressBar,
                           MaterialButton btnVerificar) {

        progressBar.setVisibility(View.VISIBLE);
        btnVerificar.setEnabled(false);

        apiService.verifyOtp(new VerifyOtpRequest(email, code))
                .enqueue(new Callback<ApiResponse<VerifyOtpData>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<VerifyOtpData>> call,
                                           @NonNull Response<ApiResponse<VerifyOtpData>> response) {
                        progressBar.setVisibility(View.GONE);
                        btnVerificar.setEnabled(true);

                        if (response.isSuccessful()) {
                            Bundle args = new Bundle();
                            args.putString("email", email);
                            Navigation.findNavController(view)
                                    .navigate(R.id.action_verifyOtp_to_register, args);
                        } else {
                            Snackbar.make(view, "Código incorrecto o expirado", Snackbar.LENGTH_SHORT).show();
                            Log.e(TAG, "verifyOtp error HTTP: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<VerifyOtpData>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnVerificar.setEnabled(true);
                        Snackbar.make(view, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                        Log.e(TAG, "verifyOtp onFailure: " + t.getMessage());
                    }
                });
    }

    private void resendOtp(View view, String email, MaterialButton btnReenviar) {
        btnReenviar.setEnabled(false);

        apiService.resendOtp(new OtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(view, "Código reenviado", Snackbar.LENGTH_SHORT).show();
                    startResendCooldown(btnReenviar);
                } else {
                    Snackbar.make(view, "No se pudo reenviar el código", Snackbar.LENGTH_SHORT).show();
                    btnReenviar.setEnabled(true);
                    btnReenviar.setAlpha(1f);
                    Log.e(TAG, "resendOtp error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Snackbar.make(view, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                btnReenviar.setEnabled(true);
                btnReenviar.setAlpha(1f);
                Log.e(TAG, "resendOtp onFailure: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}