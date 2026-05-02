package com.example.xplorenow.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.xplorenow.R;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.model.User;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.MeResponseData;
import com.example.xplorenow.data.network.dto.UpdateProfileRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.session.TokenManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> handleUnauthorized());

        btnEditProfile.setOnClickListener(v ->
                toggleEditMode(editContainer.getVisibility() != View.VISIBLE));

        btnGoToChangePassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_changePasswordFragment));

        setupSaveButton();
        loadProfile();
    }

    private void loadProfile() {
        tvName.setText("-");
        tvEmail.setText("-");
        tvUsername.setText("-");

        apiService.getMe().enqueue(new Callback<WrappedResponse<MeResponseData>>() {
            @Override
            public void onResponse(Call<WrappedResponse<MeResponseData>> call,
                                   Response<WrappedResponse<MeResponseData>> response) {

                if (response.code() == 401) {
                    handleUnauthorized();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    MeResponseData data = response.body().getData();
                    User user = data != null ? data.getUser() : null;

                    if (user == null) return;

                    String fullName = (user.getFirstName() + " " + user.getLastName()).trim();

                    tvName.setText(fullName.isEmpty() ? "-" : fullName);
                    tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
                    tvUsername.setText(user.getUsername() != null ? user.getUsername() : "-");
                    tvPhone.setText(user.getPhone() != null ? user.getPhone() : "-");

                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                    etUsername.setText(user.getUsername());
                    etPhone.setText(user.getPhone());

                    List<String> prefs = user.getPreferredCategories();
                    if (prefs != null) {
                        cbAventura.setChecked(prefs.contains("adventure"));
                        cbCultura.setChecked(prefs.contains("guided_tour"));
                        cbGastronomia.setChecked(prefs.contains("gastronomic"));
                        cbNaturaleza.setChecked(prefs.contains("excursion"));
                        cbRelax.setChecked(prefs.contains("free_tour"));
                    }

                    if (user.getProfileImageUrl() != null) {
                        Glide.with(requireContext())
                                .load(user.getProfileImageUrl())
                                .circleCrop()
                                .into(ivProfileImage);
                    }

                    loadBookingsSummary();
                }
            }

            @Override
            public void onFailure(Call<WrappedResponse<MeResponseData>> call, Throwable t) {
                Log.e(TAG, "Error perfil", t);
            }
        });
    }

    private void loadBookingsSummary() {
        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {

            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call,
                                   Response<ApiResponse<List<Booking>>> response) {

                if (!isAdded()) return;

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Error obteniendo bookings");
                    return;
                }

                List<Booking> bookings = response.body().getData();

                int reservadas = 0;
                int realizadas = 0;

                if (bookings != null) {
                    for (Booking b : bookings) {
                        if (b == null || b.getStatus() == null) continue;
                        switch (b.getStatus()) {
                            case "CONFIRMED":
                                reservadas++;
                                break;
                            case "FINISHED":
                            case "CANCELED":
                                realizadas++;
                                break;
                        }
                    }
                }

                tvReservadas.setText(getString(R.string.profile_bookings_confirmed, reservadas));
                tvRealizadas.setText(getString(R.string.profile_bookings_finished, realizadas));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error cargando bookings", t);
            }
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            List<String> preferences = new ArrayList<>();

            if (cbAventura.isChecked()) preferences.add("adventure");
            if (cbCultura.isChecked()) preferences.add("guided_tour");
            if (cbGastronomia.isChecked()) preferences.add("gastronomic");
            if (cbNaturaleza.isChecked()) preferences.add("excursion");
            if (cbRelax.isChecked()) preferences.add("free_tour");

            UpdateProfileRequest request = new UpdateProfileRequest(
                    etFirstName.getText().toString(),
                    etLastName.getText().toString(),
                    etUsername.getText().toString(),
                    etPhone.getText().toString(),
                    preferences
            );

            apiService.updateProfile(request).enqueue(new Callback<WrappedResponse<MeResponseData>>() {
                @Override
                public void onResponse(Call<WrappedResponse<MeResponseData>> call,
                                       Response<WrappedResponse<MeResponseData>> response) {

                    if (response.code() == 401) {
                        handleUnauthorized();
                        return;
                    }

                    if (response.isSuccessful()) {
                        Snackbar.make(requireView(), getString(R.string.profile_save_success), Snackbar.LENGTH_SHORT).show();
                        toggleEditMode(false);
                    }
                }

                @Override
                public void onFailure(Call<WrappedResponse<MeResponseData>> call, Throwable t) {
                    Snackbar.make(requireView(), getString(R.string.error_connection), Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleEditMode(boolean editing) {
        editContainer.setVisibility(editing ? View.VISIBLE : View.GONE);
    }

    private void handleUnauthorized() {
        tokenManager.clear();
        NavHostFragment.findNavController(this).navigate(R.id.authStartFragment);
    }
}
