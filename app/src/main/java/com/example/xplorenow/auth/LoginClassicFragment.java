package com.example.xplorenow.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LoginClassicRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.network.dto.auth.AuthTokensResponse;
import com.example.xplorenow.data.session.TokenManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

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

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_classic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int dp24 = Math.round(24 * getResources().getDisplayMetrics().density);
            v.setPadding(dp24 + bars.left, bars.top, dp24 + bars.right, dp24 + bars.bottom);
            return insets;
        });

        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        progress = view.findViewById(R.id.progress);

        String initialEmail = "";
        if (getArguments() != null) {
            initialEmail = getArguments().getString("email", "");
        }
        if (tilEmail.getEditText() != null) {
            tilEmail.getEditText().setText(initialEmail);
        }

        view.findViewById(R.id.btnLogin).setOnClickListener(v -> doLogin(view));
    }

    private void doLogin(View rootView) {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = tilEmail.getEditText() != null
                ? tilEmail.getEditText().getText().toString().trim() : "";
        String password = tilPassword.getEditText() != null
                ? tilPassword.getEditText().getText().toString() : "";

        if (!isValidEmail(email)) {
            tilEmail.setError("Email inválido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es requerida");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        api.loginClassic(new LoginClassicRequest(email, password))
                .enqueue(new Callback<WrappedResponse<AuthTokensResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Response<WrappedResponse<AuthTokensResponse>> response) {
                        progress.setVisibility(View.GONE);
                        if (!isAdded()) return;

                        if (response.code() == 400 || response.code() == 401) {
                            tilPassword.setError("Contraseña incorrecta");
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Snackbar.make(rootView, "Error al iniciar sesión", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        AuthTokensResponse tokens = response.body().getData();
                        tokenManager.saveTokens(tokens.access, tokens.refresh);

                        navigateToHome(rootView);
                    }

                    @Override
                    public void onFailure(@NonNull Call<WrappedResponse<AuthTokensResponse>> call, @NonNull Throwable t) {
                        progress.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }



    private void navigateToHome(View rootView) {
        rootView.post(() -> {
            if (!isAdded()) return;
            Navigation.findNavController(rootView).navigate(R.id.action_loginClassic_to_home);
        });
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}
