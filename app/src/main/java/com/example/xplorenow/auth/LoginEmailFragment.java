package com.example.xplorenow.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.di.TokenManagerAccessor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class LoginEmailFragment extends Fragment {

    private TextInputLayout tilEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilEmail = view.findViewById(R.id.tilEmail);

        MaterialButton btnSendOtp = view.findViewById(R.id.btnSendOtp);
        MaterialButton btnGoClassic = view.findViewById(R.id.btnGoClassic);

        if (TokenManagerAccessor.from(requireContext()).isLoggedIn()) {
            view.post(() -> {
                if (!isAdded()) return;
                Navigation.findNavController(view).navigate(R.id.action_loginEmail_to_home);
            });
        }

        btnSendOtp.setOnClickListener(v -> {
            tilEmail.setError(null);
            String email = getEmail();
            if (!isValidEmail(email)) {
                tilEmail.setError("Email inválido");
                return;
            }

            Bundle args = new Bundle();
            args.putString("email", email);
            Navigation.findNavController(view).navigate(R.id.action_loginEmail_to_loginOtp, args);
        });

        btnGoClassic.setOnClickListener(v -> {
            tilEmail.setError(null);
            String email = getEmail();
            if (!isValidEmail(email)) {
                tilEmail.setError("Email inválido");
                return;
            }
            Bundle args = new Bundle();
            args.putString("email", email);
            Navigation.findNavController(view).navigate(R.id.action_loginEmail_to_loginClassic, args);
        });
    }

    private String getEmail() {
        if (tilEmail == null || tilEmail.getEditText() == null) return "";
        return tilEmail.getEditText().getText().toString().trim();
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}
