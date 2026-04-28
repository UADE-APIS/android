package com.example.xplorenow.bookings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.ActivityAvailability;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.model.BookingRequest;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.local.CachedBooking;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CreateBookingFragment extends Fragment {

    private static final String TAG = "CreateBookingFragment";

    @Inject
    ApiService apiService;

    @Inject
    com.example.xplorenow.data.local.CachedBookingDao cachedBookingDao;

    private List<ActivityAvailability> availabilities = new ArrayList<>();
    private Activity currentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvActivityTitle = view.findViewById(R.id.tvActivityTitle);
        Spinner spDate = view.findViewById(R.id.spDate);
        TextView tvAvailableSlots = view.findViewById(R.id.tvAvailableSlots);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        Button btnBook = view.findViewById(R.id.btnBook);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        int activityId = getArguments() != null ? getArguments().getInt("activityId", -1) : -1;

        if (activityId == -1) {
            tvError.setText(getString(R.string.error_invalid_activity));
            tvError.setVisibility(View.VISIBLE);
            btnBook.setEnabled(false);
            return;
        }

        fetchActivityDetails(activityId, tvActivityTitle, spDate, tvAvailableSlots, progressBar, tvError, btnBook);

        spDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!availabilities.isEmpty()) {
                    int slots = availabilities.get(position).getAvailableSlots();
                    tvAvailableSlots.setText(getString(R.string.text_available_slots, slots));
                } else if (currentActivity != null) {
                    tvAvailableSlots.setText(getString(R.string.text_available_slots, currentActivity.getAvailableSlots()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnBook.setOnClickListener(v -> {
            tvError.setVisibility(View.GONE);
            String quantityStr = etQuantity.getText().toString().trim();

            if (quantityStr.isEmpty()) {
                tvError.setText(getString(R.string.error_fill_quantity));
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            int requestedQuantity = Integer.parseInt(quantityStr);

            if (requestedQuantity <= 0) {
                tvError.setText(getString(R.string.error_invalid_quantity));
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            int availableSlots;
            Integer availabilityId = null;

            if (!availabilities.isEmpty()) {
                ActivityAvailability selectedAvailability = availabilities.get(spDate.getSelectedItemPosition());
                availableSlots = selectedAvailability.getAvailableSlots();
                availabilityId = selectedAvailability.getId();
            } else {
                availableSlots = currentActivity.getAvailableSlots();
            }

            if (requestedQuantity > availableSlots) {
                tvError.setText(getString(R.string.error_insufficient_slots));
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            executeBooking(view, new BookingRequest(activityId, availabilityId, requestedQuantity), progressBar, tvError);
        });
    }

    private void fetchActivityDetails(int activityId, TextView tvTitle, Spinner spDate, TextView tvSlots, ProgressBar pb, TextView err, Button btnBook) {
        pb.setVisibility(View.VISIBLE);
        btnBook.setEnabled(false);

        apiService.getActivity(activityId).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Activity>> call, @NonNull Response<ApiResponse<Activity>> response) {
                pb.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentActivity = response.body().getData();
                    tvTitle.setText(currentActivity.getTitle());

                    if (currentActivity.getAvailabilities() != null && !currentActivity.getAvailabilities().isEmpty()) {
                        availabilities = currentActivity.getAvailabilities();
                        List<String> dateStrings = new ArrayList<>();
                        for (ActivityAvailability availability : availabilities) {
                            // Si el modelo tiene un campo getTime() separado, se concatena aquí.
                            // Si todo viene en getDate(), esto es suficiente.
                            dateStrings.add(availability.getDate());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dateStrings);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spDate.setAdapter(adapter);
                    } else {
                        spDate.setVisibility(View.GONE);
                        tvSlots.setText(getString(R.string.text_available_slots, currentActivity.getAvailableSlots()));
                    }

                    btnBook.setEnabled(true);
                } else {
                    err.setText(getString(R.string.error_loading_data));
                    err.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Activity>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                err.setText(getString(R.string.error_connection));
                err.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void executeBooking(View view, BookingRequest request, ProgressBar pb, TextView err) {
        pb.setVisibility(View.VISIBLE);
        err.setVisibility(View.GONE);

        apiService.createBooking(request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call, @NonNull Response<ApiResponse<Booking>> response) {
                pb.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    err.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    err.setText(getString(R.string.msg_booking_success));
                    err.setVisibility(View.VISIBLE);

                    Booking createdBooking = response.body().getData();
                    if (createdBooking != null) {
                        new Thread(() -> {
                            String imgUrl = "";
                            if (createdBooking.getActivityDetail() != null &&
                                    createdBooking.getActivityDetail().getImages() != null &&
                                    !createdBooking.getActivityDetail().getImages().isEmpty()) {
                                imgUrl = createdBooking.getActivityDetail().getImages().get(0).getImageUrl();
                            }
                            
                            CachedBooking cb = new CachedBooking(
                                    String.valueOf(createdBooking.getId()),
                                    createdBooking.getActivityDetail() != null ? createdBooking.getActivityDetail().getTitle() : "",
                                    createdBooking.getDate(),
                                    createdBooking.getActivityDetail() != null ? createdBooking.getActivityDetail().getMeetingPoint() : "",
                                    createdBooking.getStatus() != null ? createdBooking.getStatus() : "CONFIRMED",
                                    imgUrl
                            );
                            List<CachedBooking> list = new ArrayList<>();
                            list.add(cb);
                            cachedBookingDao.insertBookings(list);
                        }).start();
                    }

                    view.postDelayed(() -> Navigation.findNavController(view).popBackStack(), 1500);
                } else {
                    err.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                    err.setText(getString(R.string.error_booking_failed) + " " + response.code());
                    err.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                err.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                err.setText(getString(R.string.error_connection));
                err.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}