package com.example.xplorenow.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;

public class AuthStartFragment extends Fragment {

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

        Button btnGoLogin = view.findViewById(R.id.btnGoLogin);
        Button btnGoRegister = view.findViewById(R.id.btnGoRegister);

        btnGoLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_loginEmail));

        btnGoRegister.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_authStart_to_requestOtp));
    }
}

