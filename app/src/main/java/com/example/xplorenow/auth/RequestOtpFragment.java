package com.example.xplorenow.auth;

import android.os.Bundle;
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
import com.example.xplorenow.data.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class RequestOtpFragment extends Fragment {

    private static final String TAG = "RequestOtpFragment";

    @Inject
    ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etEmail = view.findViewById(R.id.etEmail);
        Button btnContinuar = view.findViewById(R.id.btnContinuar);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        btnContinuar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                showError(tvError, "Please enter your email");
                return;
            }

            // primero verificamos si el mail ya tiene cuenta, después enviamos el código
            checkEmailAndRequestOtp(view, email, progressBar, btnContinuar, tvError);
        });
    }

    // paso 1: chequear si el email ya está registrado
    private void checkEmailAndRequestOtp(View view, String email,
                                         ProgressBar progressBar,
                                         Button btnContinuar,
                                         TextView tvError) {
        progressBar.setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(false);
        tvError.setVisibility(View.GONE);

        apiService.checkEmail(new OtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {

                // 409 significa que ya existe una cuenta con ese email
                if (response.code() == 409) {
                    progressBar.setVisibility(View.GONE);
                    btnContinuar.setEnabled(true);
                    showError(tvError, "An account already exists with this email.");
                    return;
                }

                if (response.isSuccessful()) {
                    // email libre, enviamos el código OTP
                    requestOtp(view, email, progressBar, btnContinuar, tvError);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnContinuar.setEnabled(true);
                    showError(tvError, "Could not verify email. Please try again.");
                    Log.e(TAG, "checkEmail error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnContinuar.setEnabled(true);
                showError(tvError, "Connection error: " + t.getMessage());
                Log.e(TAG, "checkEmail onFailure: " + t.getMessage());
            }
        });
    }

    // paso 2: email libre, pedimos el código al servidor
    private void requestOtp(View view, String email,
                            ProgressBar progressBar,
                            Button btnContinuar,
                            TextView tvError) {

        apiService.requestOtp(new OtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                progressBar.setVisibility(View.GONE);
                btnContinuar.setEnabled(true);

                if (response.isSuccessful()) {
                    // pasamos el email a la siguiente pantalla para usarlo en la verificación
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_requestOtp_to_verifyOtp, args);
                } else {
                    showError(tvError, "Could not send the code. Please try again.");
                    Log.e(TAG, "requestOtp error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnContinuar.setEnabled(true);
                showError(tvError, "Connection error: " + t.getMessage());
                Log.e(TAG, "requestOtp onFailure: " + t.getMessage());
            }
        });
    }

    private void showError(TextView tvError, String message) {
        tvError.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}