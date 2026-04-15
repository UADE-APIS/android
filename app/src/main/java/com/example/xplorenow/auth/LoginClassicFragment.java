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
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LoginClassicRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;
import com.example.xplorenow.data.session.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginClassicFragment extends Fragment {

    @Inject
    ApiService api;
    @Inject
    TokenManager tokenManager;

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvError;
    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_classic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvError = view.findViewById(R.id.tvError);
        progress = view.findViewById(R.id.progress);

        String initialEmail = "";
        if (getArguments() != null) {
            initialEmail = getArguments().getString("email", "");
        }
        etEmail.setText(initialEmail);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> doLogin(view));
    }

    private void doLogin(View rootView) {
        tvError.setText("");

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!isValidEmail(email)) {
            tvError.setText("Email inválido.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tvError.setText("La contraseña es requerida.");
            return;
        }

        setLoading(true);

        api.loginClassic(new LoginClassicRequest(email, password))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        setLoading(false);
                        if (!response.isSuccessful() || response.body() == null || response.body().data == null) {
                            tvError.setText("No se pudo iniciar sesión (" + response.code() + ").");
                            return;
                        }

                        AuthTokensResponse tokens = response.body().data;
                        tokenManager.saveTokens(tokens.access, tokens.refresh);
                        rootView.post(() -> {
                            if (!isAdded()) return;
                            Navigation.findNavController(rootView).navigate(R.id.action_loginClassic_to_home);
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Throwable t) {
                        setLoading(false);
                        tvError.setText("Error de red: " + t.getMessage());
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}

