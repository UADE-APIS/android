package com.example.xplorenow.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.OtpRequest;
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

        TextInputLayout tilEmail = view.findViewById(R.id.tilEmail);
        MaterialButton btnContinuar = view.findViewById(R.id.btnContinuar);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        btnContinuar.setOnClickListener(v -> {
            tilEmail.setError(null);
            String email = tilEmail.getEditText() != null
                    ? tilEmail.getEditText().getText().toString().trim() : "";

            if (email.isEmpty()) {
                tilEmail.setError("Ingresá tu email");
                return;
            }

            checkEmailAndRequestOtp(view, email, progressBar, btnContinuar, tilEmail);
        });
    }

    private void checkEmailAndRequestOtp(View view, String email,
                                         ProgressBar progressBar,
                                         MaterialButton btnContinuar,
                                         TextInputLayout tilEmail) {
        progressBar.setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(false);

        apiService.checkEmail(new OtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {

                if (response.code() == 409) {
                    progressBar.setVisibility(View.GONE);
                    btnContinuar.setEnabled(true);
                    tilEmail.setError("Ya existe una cuenta con este email");
                    return;
                }

                if (response.isSuccessful()) {
                    requestOtp(view, email, progressBar, btnContinuar);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnContinuar.setEnabled(true);
                    Snackbar.make(view, "No se pudo verificar el email", Snackbar.LENGTH_SHORT).show();
                    Log.e(TAG, "checkEmail error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnContinuar.setEnabled(true);
                Snackbar.make(view, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "checkEmail onFailure: " + t.getMessage());
            }
        });
    }

    private void requestOtp(View view, String email,
                            ProgressBar progressBar,
                            MaterialButton btnContinuar) {

        apiService.requestOtp(new OtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                progressBar.setVisibility(View.GONE);
                btnContinuar.setEnabled(true);

                if (response.isSuccessful()) {
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_requestOtp_to_verifyOtp, args);
                } else {
                    Snackbar.make(view, "No se pudo enviar el código", Snackbar.LENGTH_SHORT).show();
                    Log.e(TAG, "requestOtp error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnContinuar.setEnabled(true);
                Snackbar.make(view, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "requestOtp onFailure: " + t.getMessage());
            }
        });
    }
}