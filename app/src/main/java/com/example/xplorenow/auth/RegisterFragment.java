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
import com.example.xplorenow.data.model.CheckUsernameRequest;
import com.example.xplorenow.data.model.RegisterData;
import com.example.xplorenow.data.model.RegisterRequest;
import com.example.xplorenow.data.network.ApiService;

import org.json.JSONObject;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    @Inject
    ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // el email viene verificado del paso anterior
        String email = getArguments() != null
                ? getArguments().getString("email", "")
                : "";

        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnCrearCuenta = view.findViewById(R.id.btnCrearCuenta);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        btnCrearCuenta.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // validaciones locales antes de tocar la API
            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
                showError(tvError, "Please fill in all fields");
                return;
            }

            if (password.isEmpty()) {
                showError(tvError, "Please enter a password");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError(tvError, "Passwords do not match");
                return;
            }

            checkUsernameAndRegister(view, email, password, confirmPassword,
                    username, firstName, lastName,
                    progressBar, btnCrearCuenta, tvError);
        });
    }

    // primero verificamos que el nombre de usuario no esté en uso
    private void checkUsernameAndRegister(View rootView, String email, String password, String confirmPassword,
                                          String username, String firstName, String lastName,
                                          ProgressBar progressBar, Button btnCrearCuenta,
                                          TextView tvError) {
        progressBar.setVisibility(View.VISIBLE);
        btnCrearCuenta.setEnabled(false);
        tvError.setVisibility(View.GONE);

        apiService.checkUsername(new CheckUsernameRequest(username))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                           @NonNull Response<ApiResponse<Void>> response) {
                        if (response.code() == 409) {
                            progressBar.setVisibility(View.GONE);
                            btnCrearCuenta.setEnabled(true);
                            showError(tvError, "Username is already taken.");
                            return;
                        }

                        if (response.isSuccessful()) {
                            // nombre disponible, pasamos a crear la cuenta
                            register(rootView, email, password, confirmPassword,
                                    username, firstName, lastName,
                                    progressBar, btnCrearCuenta, tvError);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnCrearCuenta.setEnabled(true);
                            showError(tvError, "Could not verify username. Please try again.");
                            Log.e(TAG, "checkUsername error HTTP: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call,
                                          @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnCrearCuenta.setEnabled(true);
                        showError(tvError, "Connection error: " + t.getMessage());
                        Log.e(TAG, "checkUsername onFailure: " + t.getMessage());
                    }
                });
    }

    // con el username libre, mandamos todos los datos al backend
    private void register(View rootView, String email, String password, String confirmPassword,
                          String username, String firstName, String lastName,
                          ProgressBar progressBar, Button btnCrearCuenta, TextView tvError) {

        RegisterRequest request = new RegisterRequest(
                email, password, confirmPassword, username, firstName, lastName);

        apiService.register(request).enqueue(new Callback<ApiResponse<RegisterData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RegisterData>> call,
                                   @NonNull Response<ApiResponse<RegisterData>> response) {
                progressBar.setVisibility(View.GONE);
                btnCrearCuenta.setEnabled(true);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Registration successful: " + email);
                    Navigation.findNavController(rootView).navigate(R.id.action_register_to_home);
                } else {
                    // mostramos el mensaje específico que devuelve el backend
                    showError(tvError, parseErrorMessage(response));
                    Log.e(TAG, "register error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<RegisterData>> call,
                                  @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCrearCuenta.setEnabled(true);
                showError(tvError, "Connection error: " + t.getMessage());
                Log.e(TAG, "register onFailure: " + t.getMessage());
            }
        });
    }

    // extrae el campo "message" del cuerpo del error
    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBodyStr = response.errorBody().string();
                JSONObject json = new JSONObject(errorBodyStr);
                if (json.has("message")) {
                    return json.getString("message");
                }
            }
        } catch (IOException | org.json.JSONException e) {
            Log.e(TAG, "Error parsing errorBody: " + e.getMessage());
        }
        return "Could not create account. Please try again.";
    }

    private void showError(TextView tvError, String message) {
        tvError.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}