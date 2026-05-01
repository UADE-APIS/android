package com.example.xplorenow.auth;

import android.os.Bundle;
import android.util.Log;
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
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.CheckUsernameRequest;
import com.example.xplorenow.data.model.RegisterData;
import com.example.xplorenow.data.model.RegisterRequest;
import com.example.xplorenow.data.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
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

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int dp24 = Math.round(24 * getResources().getDisplayMetrics().density);
            v.setPadding(dp24 + bars.left, bars.top, dp24 + bars.right, dp24 + bars.bottom);
            return insets;
        });

        String email = getArguments() != null
                ? getArguments().getString("email", "")
                : "";

        TextInputLayout tilFirstName = view.findViewById(R.id.tilFirstName);
        TextInputLayout tilLastName = view.findViewById(R.id.tilLastName);
        TextInputLayout tilUsername = view.findViewById(R.id.tilUsername);
        TextInputLayout tilPassword = view.findViewById(R.id.tilPassword);
        TextInputLayout tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        MaterialButton btnCrearCuenta = view.findViewById(R.id.btnCrearCuenta);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        btnCrearCuenta.setOnClickListener(v -> {
            tilFirstName.setError(null);
            tilLastName.setError(null);
            tilUsername.setError(null);
            tilPassword.setError(null);
            tilConfirmPassword.setError(null);

            String firstName = tilFirstName.getEditText() != null ? tilFirstName.getEditText().getText().toString().trim() : "";
            String lastName = tilLastName.getEditText() != null ? tilLastName.getEditText().getText().toString().trim() : "";
            String username = tilUsername.getEditText() != null ? tilUsername.getEditText().getText().toString().trim() : "";
            String password = tilPassword.getEditText() != null ? tilPassword.getEditText().getText().toString().trim() : "";
            String confirmPassword = tilConfirmPassword.getEditText() != null ? tilConfirmPassword.getEditText().getText().toString().trim() : "";

            if (firstName.isEmpty()) { tilFirstName.setError("Campo requerido"); return; }
            if (lastName.isEmpty()) { tilLastName.setError("Campo requerido"); return; }
            if (username.isEmpty()) { tilUsername.setError("Campo requerido"); return; }
            if (password.isEmpty()) { tilPassword.setError("Campo requerido"); return; }
            if (!password.equals(confirmPassword)) {
                tilConfirmPassword.setError("Las contraseñas no coinciden");
                return;
            }

            checkUsernameAndRegister(view, email, password, confirmPassword,
                    username, firstName, lastName,
                    progressBar, btnCrearCuenta,
                    tilUsername);
        });
    }

    private void checkUsernameAndRegister(View rootView, String email, String password, String confirmPassword,
                                          String username, String firstName, String lastName,
                                          ProgressBar progressBar, MaterialButton btnCrearCuenta,
                                          TextInputLayout tilUsername) {
        progressBar.setVisibility(View.VISIBLE);
        btnCrearCuenta.setEnabled(false);

        apiService.checkUsername(new CheckUsernameRequest(username))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                           @NonNull Response<ApiResponse<Void>> response) {
                        if (response.code() == 409) {
                            progressBar.setVisibility(View.GONE);
                            btnCrearCuenta.setEnabled(true);
                            tilUsername.setError("El nombre de usuario ya está en uso");
                            return;
                        }

                        if (response.isSuccessful()) {
                            register(rootView, email, password, confirmPassword,
                                    username, firstName, lastName,
                                    progressBar, btnCrearCuenta);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnCrearCuenta.setEnabled(true);
                            Snackbar.make(rootView, "No se pudo verificar el usuario", Snackbar.LENGTH_SHORT).show();
                            Log.e(TAG, "checkUsername error HTTP: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnCrearCuenta.setEnabled(true);
                        Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                        Log.e(TAG, "checkUsername onFailure: " + t.getMessage());
                    }
                });
    }

    private void register(View rootView, String email, String password, String confirmPassword,
                          String username, String firstName, String lastName,
                          ProgressBar progressBar, MaterialButton btnCrearCuenta) {

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
                    Bundle args = new Bundle();
                    args.putBoolean("register_success", true);
                    Navigation.findNavController(rootView).navigate(R.id.action_register_to_authStart, args);
                } else {
                    Snackbar.make(rootView, parseErrorMessage(response), Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "register error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<RegisterData>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCrearCuenta.setEnabled(true);
                Snackbar.make(rootView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "register onFailure: " + t.getMessage());
            }
        });
    }

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
        return "No se pudo crear la cuenta. Intentá de nuevo.";
    }
}