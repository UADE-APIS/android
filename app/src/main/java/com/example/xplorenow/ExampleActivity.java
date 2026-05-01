package com.example.xplorenow;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xplorenow.databinding.ActivityExampleBinding;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExampleActivity extends AppCompatActivity {

    private ActivityExampleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // View Binding
        binding = ActivityExampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 6. TOOLBAR - Configuración con setSupportActionBar
        setSupportActionBar(binding.topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 10. EXTRA - Manejo de errores en TextInputLayout
        setupListeners();
    }

    private void setupListeners() {
        binding.btnReserve.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            
            if (validateEmail(email)) {
                binding.emailInputLayout.setError(null); // Limpiar error
                Toast.makeText(this, "Reserva exitosa para: " + email, Toast.LENGTH_SHORT).show();
            } else {
                binding.emailInputLayout.setError("Introduce un email válido");
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            finish();
        });

        binding.fab.setOnClickListener(v -> {
            Toast.makeText(this, "¿Necesitas ayuda?", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
