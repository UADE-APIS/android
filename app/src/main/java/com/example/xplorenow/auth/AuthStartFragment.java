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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.session.SessionStore;
import com.example.xplorenow.network.ApiService;
import com.example.xplorenow.network.RetrofitProvider;
import com.example.xplorenow.network.dto.LoginClassicRequest;
import com.example.xplorenow.network.dto.WrappedResponse;
import com.example.xplorenow.network.dto.auth.AuthTokensResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthStartFragment extends Fragment {

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private ProgressBar progress;
    private ApiService api;

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

        api = RetrofitProvider
                .getRetrofit(RetrofitProvider.buildDefaultClient())
                .create(ApiService.class);

        SessionStore.getInstance(requireContext())
                .isLoggedIn()
                .subscribe(isLoggedIn -> {
                    if (!isLoggedIn) return;
                    view.post(() -> {
                        if (!isAdded()) return;
                        Navigation.findNavController(view).navigate(R.id.action_authStart_to_home);
                    });
                }, throwable -> { /* ignore */ });

        view.findViewById(R.id.btnIngresar).setOnClickListener(v -> doLogin(view));
        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_requestOtp));
    }

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
                        if (!response.isSuccessful() || response.body() == null || response.body().data == null) {
                            tvError.setText("No se pudo iniciar sesión (" + response.code() + ").");
                            return;
                        }
                        AuthTokensResponse tokens = response.body().data;
                        SessionStore.getInstance(requireContext())
                                .saveTokens(tokens.access, tokens.refresh)
                                .subscribe(() -> rootView.post(() -> {
                                            if (!isAdded()) return;
                                            Navigation.findNavController(rootView).navigate(R.id.action_authStart_to_home);
                                        }),
                                        throwable -> rootView.post(() ->
                                                tvError.setText("Error guardando sesión.")));
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call,
                                          @NonNull Throwable t) {
                        progress.setVisibility(View.GONE);
                        tvError.setText("Error de red: " + t.getMessage());
                    }
                });
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}
