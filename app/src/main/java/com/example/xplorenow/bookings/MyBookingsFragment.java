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

import com.example.xplorenow.data.local.CachedBooking;
import com.example.xplorenow.data.local.CachedBookingDao;
import com.example.xplorenow.data.model.ActivityImage;
import java.util.ArrayList;

@AndroidEntryPoint
public class MyBookingsFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject CachedBookingDao cachedBookingDao;
    private BookingsAdapter adapter;
    private TextView tvOfflineMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvOfflineMode = view.findViewById(R.id.tvOfflineMode);
        RecyclerView rvBookings = view.findViewById(R.id.rvBookings);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookingsAdapter(booking -> showCancelDialog(booking, progressBar, tvError));
        rvBookings.setAdapter(adapter);
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        loadBookings(progressBar, tvError);
    }

    private void loadBookings(ProgressBar pb, TextView err) {
        pb.setVisibility(View.VISIBLE);
        err.setVisibility(View.GONE);
        tvOfflineMode.setVisibility(View.GONE);

        apiService.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Booking>>> call, @NonNull Response<ApiResponse<List<Booking>>> response) {
                pb.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> list = response.body().getData();
                    adapter.setBookings(list);
                    new Thread(() -> {
                        cachedBookingDao.clearAllBookings();
                        List<CachedBooking> cacheList = new ArrayList<>();
                        for (Booking b : list) {
                            String imgUrl = "";
                            if (b.getActivityDetail() != null && b.getActivityDetail().getImages() != null && !b.getActivityDetail().getImages().isEmpty()) {
                                imgUrl = b.getActivityDetail().getImages().get(0).getImageUrl();
                            }
                            cacheList.add(new CachedBooking(
                                    String.valueOf(b.getId()),
                                    b.getActivityDetail() != null ? b.getActivityDetail().getTitle() : "",
                                    b.getDate(),
                                    b.getActivityDetail() != null ? b.getActivityDetail().getMeetingPoint() : "",
                                    b.getStatus(),
                                    imgUrl
                            ));
                        }
                        cachedBookingDao.insertBookings(cacheList);
                    }).start();
                } else {
                    err.setVisibility(View.VISIBLE);
                    err.setText(getString(R.string.error_loading_data));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Booking>>> call, @NonNull Throwable t) {
                new Thread(() -> {
                    List<CachedBooking> cached = cachedBookingDao.getAllBookings();
                    if (!cached.isEmpty()) {
                        List<Booking> converted = new ArrayList<>();

                        for (CachedBooking cb : cached) {

                            Booking b = new Booking();
                            b.setId(Integer.parseInt(cb.getId()));
                            b.setStatus(cb.getStatus());
                            b.setCreatedAt(cb.getDate());

                            com.example.xplorenow.data.model.Activity a = new com.example.xplorenow.data.model.Activity();
                            a.setTitle(cb.getActivityTitle());
                            a.setMeetingPoint(cb.getMeetingPoint());

                            if (cb.getActivityImageUrl() != null && !cb.getActivityImageUrl().isEmpty()) {
                                ActivityImage ai = new ActivityImage();
                                ai.setImageUrl(cb.getActivityImageUrl());

                                List<ActivityImage> imgs = new ArrayList<>();
                                imgs.add(ai);
                                a.setImages(imgs);
                            }

                            b.setActivityDetail(a);

                            converted.add(b);
                        }
                        requireActivity().runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            tvOfflineMode.setVisibility(View.VISIBLE);
                            adapter.setBookings(converted);
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            err.setVisibility(View.VISIBLE);
                            err.setText(getString(R.string.error_connection));
                        });
                    }
                }).start();
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