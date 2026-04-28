package com.example.xplorenow.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.User;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.MeResponseData;
import com.example.xplorenow.data.network.dto.UpdateProfileRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.session.TokenManager;

import android.widget.CheckBox;
import java.util.ArrayList;
import java.util.List;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.xplorenow.data.network.dto.bookings.Booking;
import com.example.xplorenow.data.network.dto.bookings.BookingsListResponse;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private static final String TAG = "PROFILE_DEBUG";

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private ImageView ivProfileImage;
    private TextView tvName, tvEmail, tvUsername, tvPhone;
    private EditText etFirstName, etLastName, etUsername, etPhone;

    private Button btnSave, btnEditProfile;

    private View editContainer;

    private CheckBox cbAventura, cbCultura, cbGastronomia, cbNaturaleza, cbRelax;
    private TextView tvReservadas, tvRealizadas;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        cbAventura = view.findViewById(R.id.cbAventura);
        cbCultura = view.findViewById(R.id.cbCultura);
        cbGastronomia = view.findViewById(R.id.cbGastronomia);
        cbNaturaleza = view.findViewById(R.id.cbNaturaleza);
        cbRelax = view.findViewById(R.id.cbRelax);

        tvReservadas = view.findViewById(R.id.tvReservadas);
        tvRealizadas = view.findViewById(R.id.tvRealizadas);

        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvPhone = view.findViewById(R.id.tvPhone);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etUsername = view.findViewById(R.id.etUsername);
        etPhone = view.findViewById(R.id.etPhone);

        btnSave = view.findViewById(R.id.btnSave);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnGoToChangePassword = view.findViewById(R.id.btnGoToChangePassword);

        editContainer = view.findViewById(R.id.editContainer);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        btnEditProfile.setOnClickListener(v ->
                toggleEditMode(editContainer.getVisibility() != View.VISIBLE));

        btnGoToChangePassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_changePasswordFragment));

        setupSaveButton();

        loadProfile();

        return view;
    }

    private void loadProfile() {
        tvName.setText("-");
        tvEmail.setText("-");
        tvUsername.setText("-");

        apiService.getMe().enqueue(new Callback<WrappedResponse<MeResponseData>>() {
            @Override
            public void onResponse(Call<WrappedResponse<MeResponseData>> call,
                                   Response<WrappedResponse<MeResponseData>> response) {

                Log.d(TAG, "HTTP CODE: " + response.code());

                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    try {
                        MeResponseData data = response.body().getData();
                        User user = data != null ? data.getUser() : null;

                        if (user == null) {
                            Toast.makeText(getContext(), "No llegaron datos del usuario", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                        String lastName = user.getLastName() != null ? user.getLastName() : "";
                        String fullName = (firstName + " " + lastName).trim();

                        tvName.setText(fullName.isEmpty() ? "-" : fullName);
                        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
                        tvUsername.setText(user.getUsername() != null ? user.getUsername() : "-");
                        tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "-");

                        etFirstName.setText(firstName);
                        etLastName.setText(lastName);
                        etUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");

                        String imageUrl = user.getProfileImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .circleCrop()
                                    .into(ivProfileImage);
                        } else {
                            ivProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
                        }

                        List<String> prefs = user.getPreferredCategories();
                        if (prefs != null) {
                            cbAventura.setChecked(prefs.contains("adventure"));
                            cbCultura.setChecked(prefs.contains("guided_tour"));
                            cbGastronomia.setChecked(prefs.contains("gastronomic"));
                            cbNaturaleza.setChecked(prefs.contains("excursion"));
                            cbRelax.setChecked(prefs.contains("free_tour"));
                        }

                        loadBookingsSummary();

                    } catch (Exception e) {
                        Log.e(TAG, "PARSE ERROR", e);
                        Toast.makeText(getContext(), "Error parseando usuario", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Log.e(TAG, "ERROR BODY: " + response.errorBody());
                    Toast.makeText(getContext(), "Error al obtener perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WrappedResponse<MeResponseData>> call, Throwable t) {
                Log.e(TAG, "NETWORK ERROR", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookingsSummary() {
        apiService.getMyBookings().enqueue(new Callback<BookingsListResponse>() {
            @Override
            public void onResponse(Call<BookingsListResponse> call, Response<BookingsListResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().getResults();
                    int reservadas = 0;
                    int realizadas = 0;
                    if (bookings != null) {
                        for (Booking b : bookings) {
                            if ("finished".equalsIgnoreCase(b.getStatus())) {
                                realizadas++;
                            } else if (!"canceled".equalsIgnoreCase(b.getStatus())) {
                                reservadas++;
                            }
                        }
                    }
                    tvReservadas.setText("Reservadas: " + reservadas);
                    tvRealizadas.setText("Realizadas: " + realizadas);
                }
            }

            @Override
            public void onFailure(Call<BookingsListResponse> call, Throwable t) {
                // Ignore failure for summary
            }
        });
    }

    private void setupSaveButton() {

        btnSave.setOnClickListener(v -> {

            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            List<String> preferences = new ArrayList<>();

            if (cbAventura.isChecked()) preferences.add("adventure");
            if (cbCultura.isChecked()) preferences.add("guided_tour");
            if (cbGastronomia.isChecked()) preferences.add("gastronomic");
            if (cbNaturaleza.isChecked()) preferences.add("excursion");
            if (cbRelax.isChecked()) preferences.add("free_tour");

            UpdateProfileRequest request =
                    new UpdateProfileRequest(firstName, lastName, username, phone, preferences);

            apiService.updateProfile(request).enqueue(new Callback<WrappedResponse<MeResponseData>>() {
                @Override
                public void onResponse(Call<WrappedResponse<MeResponseData>> call,
                                       Response<WrappedResponse<MeResponseData>> response) {

                    Log.d(TAG, "PATCH CODE: " + response.code());

                    if (response.code() == 401) {
                        handleUnauthorized();
                        return;
                    }

                    if (response.isSuccessful() && response.body() != null) {

                        User updatedUser = response.body().getData().getUser();

                        String fullName = (updatedUser.getFirstName() + " " + updatedUser.getLastName()).trim();

                        tvName.setText(fullName.isEmpty() ? "-" : fullName);
                        tvEmail.setText(updatedUser.getEmail() != null ? updatedUser.getEmail() : "-");
                        tvUsername.setText(updatedUser.getUsername() != null ? updatedUser.getUsername() : "-");
                        tvPhone.setText(updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty() ? updatedUser.getPhone() : "-");

                        Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        toggleEditMode(false);

                    } else {
                        try {
                            Log.e(TAG, "PATCH ERROR BODY: " + (response.errorBody() != null ? response.errorBody().string() : "sin body"));
                        } catch (Exception ignored) {
                        }
                        Toast.makeText(getContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<WrappedResponse<MeResponseData>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleEditMode(boolean editing) {
        editContainer.setVisibility(editing ? View.VISIBLE : View.GONE);
        btnEditProfile.setText(editing ? R.string.profile_cancel_edit : R.string.profile_edit);
    }

    private void handleUnauthorized() {
        tokenManager.clear();
        if (!isAdded()) {
            return;
        }
        Toast.makeText(getContext(), "Sesión vencida. Iniciá sesión de nuevo.", Toast.LENGTH_SHORT).show();
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.authStartFragment, null, options);
    }
}