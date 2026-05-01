package com.example.xplorenow.auth;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LoginOtpRequest;
import com.example.xplorenow.data.network.dto.RequestOtpRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;
import com.example.xplorenow.data.session.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

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

    private TextInputLayout tilCode;
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
        tilCode = view.findViewById(R.id.tilCode);
        progress = view.findViewById(R.id.progress);

        email = getArguments() != null ? getArguments().getString("email", "") : "";
        tvSubtitle.setText("Enviamos un código a " + email);

        MaterialButton btnRequest = view.findViewById(R.id.btnRequestOtp);
        MaterialButton btnVerify = view.findViewById(R.id.btnVerify);

        btnRequest.setOnClickListener(v -> requestOtp(view));
        btnVerify.setOnClickListener(v -> verifyOtp(view));

        if (!TextUtils.isEmpty(email)) {
            requestOtp(view);
        }
    }

    private void requestOtp(View rootView) {
        if (TextUtils.isEmpty(email)) {
            Snackbar.make(rootView, "Email faltante", Snackbar.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        api.requestLoginOtp(new RequestOtpRequest(email)).enqueue(new Callback<WrappedResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<Void>> call, @NonNull Response<WrappedResponse<Void>> response) {
                setLoading(false);
                if (!response.isSuccessful()) {
                    Snackbar.make(rootView, "No se pudo enviar el OTP", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                setLoading(false);
                Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp(View rootView) {
        tilCode.setError(null);
        String code = tilCode.getEditText() != null
                ? tilCode.getEditText().getText().toString().trim() : "";

        if (TextUtils.isEmpty(code) || code.length() != 6) {
            tilCode.setError("El código debe tener 6 dígitos");
            return;
        }
        setLoading(true);
        api.verifyLoginOtp(new LoginOtpRequest(email, code))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        setLoading(false);
                        if (!isAdded()) return;

                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Snackbar.make(rootView, "OTP inválido o expirado", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        AuthTokensResponse tokens = response.body().getData();
                        tokenManager.saveTokens(tokens.access, tokens.refresh);

                        navigateToHome(rootView);
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Throwable t) {
                        setLoading(false);
                        Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                    }
                });
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
