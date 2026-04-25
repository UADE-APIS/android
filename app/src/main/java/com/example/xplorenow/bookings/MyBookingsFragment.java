package com.example.xplorenow.bookings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.BookingsAdapter;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.network.ApiService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyBookingsFragment extends Fragment {

    @Inject ApiService apiService;
    private BookingsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvBookings = view.findViewById(R.id.rvBookings);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookingsAdapter(booking -> showCancelDialog(booking, progressBar, tvError));
        rvBookings.setAdapter(adapter);

        loadBookings(progressBar, tvError);
    }

    private void loadBookings(ProgressBar pb, TextView err) {
        pb.setVisibility(View.VISIBLE);
        err.setVisibility(View.GONE);

        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Booking>>> call, @NonNull Response<ApiResponse<List<Booking>>> response) {
                pb.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setBookings(response.body().getData());
                } else {
                    err.setVisibility(View.VISIBLE);
                    err.setText(getString(R.string.error_loading_data));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Booking>>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                err.setVisibility(View.VISIBLE);
                err.setText(getString(R.string.error_connection));
            }
        });
    }

    private void showCancelDialog(Booking booking, ProgressBar pb, TextView err) {
        String policy = booking.getActivityDetail().getCancellationPolicy();

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_cancel_booking)
                .setMessage(getString(R.string.msg_cancel_policy, policy))
                .setPositiveButton(R.string.action_confirm, (d, w) -> performApiCancel(booking.getId(), pb, err))
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    private void performApiCancel(int id, ProgressBar pb, TextView err) {
        pb.setVisibility(View.VISIBLE);
        apiService.cancelBooking(id).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call, @NonNull Response<ApiResponse<Booking>> response) {
                loadBookings(pb, err);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                err.setVisibility(View.VISIBLE);
                err.setText(getString(R.string.error_connection));
            }
        });
    }
}