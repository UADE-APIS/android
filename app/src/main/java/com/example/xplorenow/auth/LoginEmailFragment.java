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

public class LoginEmailFragment extends Fragment {
    private EditText etEmail;
    private TextView tvError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        tvError = view.findViewById(R.id.tvError);

        Button btnSendOtp = view.findViewById(R.id.btnSendOtp);
        Button btnGoClassic = view.findViewById(R.id.btnGoClassic);

        // Quick session check: if logged in, go to Home.
        SessionStore.getInstance(requireContext())
                .isLoggedIn()
                .subscribe(isLoggedIn -> {
                    if (isLoggedIn) {
                        Navigation.findNavController(view).navigate(R.id.action_loginEmail_to_home);
                    }
                }, throwable -> {
                    // ignore; user will login manually
                });

        btnSendOtp.setOnClickListener(v -> {
            tvError.setText("");
            String email = getEmail();
            if (!isValidEmail(email)) {
                tvError.setText("Email inválido.");
                return;
            }

            Bundle args = new Bundle();
            args.putString("email", email);
            Navigation.findNavController(view).navigate(R.id.action_loginEmail_to_loginOtp, args);
        });

        btnGoClassic.setOnClickListener(v -> {
            tvError.setText("");
            String email = getEmail();
            if (!isValidEmail(email)) {
                tvError.setText("Email inválido.");
                return;
            }
            Bundle args = new Bundle();
            args.putString("email", email);
            Navigation.findNavController(view).navigate(R.id.action_loginEmail_to_loginClassic, args);
        });
    }

    private String getEmail() {
        return etEmail == null ? "" : etEmail.getText().toString().trim();
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}

