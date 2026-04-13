package com.example.xplorenow.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.session.SessionStore;
import com.example.xplorenow.network.ApiService;
import com.example.xplorenow.network.AuthInterceptor;
import com.example.xplorenow.network.RetrofitProvider;
import com.example.xplorenow.network.dto.LogoutRequest;
import com.example.xplorenow.network.dto.WrappedResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private TextView tvInfo;
    private TextView tvError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInfo = view.findViewById(R.id.tvInfo);
        tvError = view.findViewById(R.id.tvError);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        tvInfo.setText("Home (sesión activa)");

        btnLogout.setOnClickListener(v -> doLogout(view));
    }

    private void doLogout(View rootView) {
        tvError.setText("");

        SessionStore store = SessionStore.getInstance(requireContext());
        store.getRefreshToken().subscribe(refresh -> {
            if (refresh == null || refresh.trim().isEmpty()) {
                store.clear().subscribe(() ->
                        Navigation.findNavController(rootView).navigate(R.id.loginEmailFragment)
                );
                return;
            }

            ApiService api = RetrofitProvider
                    .getRetrofit(RetrofitProvider.buildAuthedClient(new AuthInterceptor(requireContext())))
                    .create(ApiService.class);

            api.logout(new LogoutRequest(refresh)).enqueue(new Callback<WrappedResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<WrappedResponse<Void>> call, @NonNull Response<WrappedResponse<Void>> response) {
                    store.clear().subscribe(() ->
                            Navigation.findNavController(rootView).navigate(R.id.loginEmailFragment)
                    );
                }

                @Override
                public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                    // Even if backend logout fails, clear local session.
                    store.clear().subscribe(() ->
                            Navigation.findNavController(rootView).navigate(R.id.loginEmailFragment)
                    );
                }
            });
        }, throwable -> tvError.setText("No se pudo leer la sesión."));
    }
}

