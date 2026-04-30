package com.example.xplorenow.bookings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.BookingsAdapter;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.network.ApiService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyBookingsFragment extends Fragment {

    private static final String TAG = "MyBookingsFragment";

    @Inject
    ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vistas como variables locales (Regla 3)
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);
        RecyclerView rvBookings = view.findViewById(R.id.rvBookings);
        TextView tvOfflineMode = view.findViewById(R.id.tvOfflineMode);

        // Inicializar UI
        tvOfflineMode.setVisibility(View.GONE);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Adapter como variable local
        BookingsAdapter adapter = new BookingsAdapter(booking -> 
                showCancelDialog(view, booking, progressBar, tvError));
        rvBookings.setAdapter(adapter);

        // Navegación (Regla 16)
        view.findViewById(R.id.btnBack).setOnClickListener(v -> 
                Navigation.findNavController(view).popBackStack());

        loadBookings(adapter, progressBar, tvError);
    }

    private void loadBookings(BookingsAdapter adapter, ProgressBar progressBar, TextView tvError) {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Booking>>> call,
                                   @NonNull Response<ApiResponse<List<Booking>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().getData();
                    if (bookings != null) {
                        adapter.setBookings(bookings);
                    }
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Booking>>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void showCancelDialog(View view, Booking booking, ProgressBar progressBar, TextView tvError) {
        String policy = (booking.getActivityDetail() != null && booking.getActivityDetail().getCancellationPolicy() != null)
                ? booking.getActivityDetail().getCancellationPolicy()
                : getString(R.string.default_cancellation_policy);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_cancel_booking)
                .setMessage(getString(R.string.msg_cancel_policy, policy))
                .setPositiveButton(R.string.action_confirm, (d, w) -> 
                        performApiCancel(view, booking.getId(), progressBar, tvError))
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    private void performApiCancel(View view, int bookingId, ProgressBar progressBar, TextView tvError) {
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.cancelBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call,
                                   @NonNull Response<ApiResponse<Booking>> response) {
                // Al cancelar, recargamos la lista
                RecyclerView rv = view.findViewById(R.id.rvBookings);
                if (rv != null && rv.getAdapter() instanceof BookingsAdapter) {
                    loadBookings((BookingsAdapter) rv.getAdapter(), progressBar, tvError);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
