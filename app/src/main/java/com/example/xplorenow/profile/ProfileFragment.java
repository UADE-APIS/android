package com.example.xplorenow.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.User;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.MeResponseData;
import com.example.xplorenow.data.network.dto.WrappedResponse;

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

    private TextView tvName, tvEmail, tvUsername;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUsername = view.findViewById(R.id.tvUsername);

        loadProfile();

        return view;
    }

    private void loadProfile() {

        apiService.getMe().enqueue(new Callback<WrappedResponse<MeResponseData>>() {
            @Override
            public void onResponse(Call<WrappedResponse<MeResponseData>> call,
                                   Response<WrappedResponse<MeResponseData>> response) {

                Log.d(TAG, "HTTP CODE: " + response.code());

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
}