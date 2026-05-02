package com.example.xplorenow.bookings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.example.xplorenow.data.model.BookingsListResponse;
import com.example.xplorenow.data.network.ApiService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyBookingsFragment extends Fragment {

    private static final String TAG = "MyBookingsFragment";

    @Inject ApiService apiService;

    private BookingsAdapter adapter;
    private final Map<String, String> currentFilters = new HashMap<>();

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

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);
        RecyclerView rvBookings = view.findViewById(R.id.rvBookings);
        TextView tvOfflineMode = view.findViewById(R.id.tvOfflineMode);

        tvOfflineMode.setVisibility(View.GONE);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new BookingsAdapter(new BookingsAdapter.OnBookingInteractionListener() {
            @Override
            public void onCancelClick(Booking booking) {
                showCancelDialog(view, booking, progressBar, tvError);
            }

            @Override
            public void onItemClick(Booking booking) {
                Bundle args = new Bundle();
                args.putInt("activityId", booking.getActivityId());
                Navigation.findNavController(view).navigate(
                        R.id.action_myBookings_to_activityDetail, args);
            }
        });

        rvBookings.setAdapter(adapter);

        view.findViewById(R.id.fabFilter).setOnClickListener(v -> mostrarDialogoFiltros(progressBar, tvError));

        loadBookings(progressBar, tvError);
    }

    private void loadBookings(ProgressBar progressBar, TextView tvError) {
        TextView tvEmpty = getView() != null ? getView().findViewById(R.id.tvEmpty) : null;
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        apiService.getMyBookings(currentFilters).enqueue(new Callback<BookingsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingsListResponse> call,
                                   @NonNull Response<BookingsListResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().getResults();
                    if (bookings != null) {
                        adapter.setBookings(bookings);
                        if (tvEmpty != null) {
                            tvEmpty.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingsListResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void mostrarDialogoFiltros(ProgressBar progressBar, TextView tvError) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_history_filters, null);

        // We reuse dialog_history_filters as it has the same fields needed
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);
        TextInputEditText etGuide = dialogView.findViewById(R.id.etGuide);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);
        TextInputEditText etDateFrom = dialogView.findViewById(R.id.etDateFrom);
        TextInputEditText etDateTo = dialogView.findViewById(R.id.etDateTo);
        Spinner spinnerOrdering = dialogView.findViewById(R.id.spinnerOrdering);
        Button btnApplyFilters = dialogView.findViewById(R.id.btnApplyFilters);
        Button btnResetFilters = dialogView.findViewById(R.id.btnResetFilters);

        if (currentFilters.containsKey("name")) etName.setText(currentFilters.get("name"));
        if (currentFilters.containsKey("destination")) etLocation.setText(currentFilters.get("destination"));
        if (currentFilters.containsKey("guide")) etGuide.setText(currentFilters.get("guide"));
        if (currentFilters.containsKey("duration")) etDuration.setText(currentFilters.get("duration"));
        if (currentFilters.containsKey("date")) etDate.setText(currentFilters.get("date"));
        if (currentFilters.containsKey("date_from")) etDateFrom.setText(currentFilters.get("date_from"));
        if (currentFilters.containsKey("date_to")) etDateTo.setText(currentFilters.get("date_to"));

        String[] orderings = {"Más recientes", "Más antiguos", "Duración", "Actividad (A-Z)"};
        String[] orderingValues = {"-created_at", "created_at", "activity__duration", "activity__name"};
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, orderings);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdering.setAdapter(orderAdapter);

        if (currentFilters.containsKey("ordering")) {
            String currentOrder = currentFilters.get("ordering");
            for (int i = 0; i < orderingValues.length; i++) {
                if (orderingValues[i].equals(currentOrder)) {
                    spinnerOrdering.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnApplyFilters.setOnClickListener(v -> {
            currentFilters.clear();
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) currentFilters.put("name", name);
            String loc = etLocation.getText().toString().trim();
            if (!loc.isEmpty()) currentFilters.put("destination", loc);
            String guide = etGuide.getText().toString().trim();
            if (!guide.isEmpty()) currentFilters.put("guide", guide);
            String dur = etDuration.getText().toString().trim();
            if (!dur.isEmpty()) currentFilters.put("duration", dur);
            String date = etDate.getText().toString().trim();
            if (!date.isEmpty()) currentFilters.put("date", date);
            String df = etDateFrom.getText().toString().trim();
            if (!df.isEmpty()) currentFilters.put("date_from", df);
            String dt = etDateTo.getText().toString().trim();
            if (!dt.isEmpty()) currentFilters.put("date_to", dt);
            currentFilters.put("ordering", orderingValues[spinnerOrdering.getSelectedItemPosition()]);

            dialog.dismiss();
            loadBookings(progressBar, tvError);
        });

        btnResetFilters.setOnClickListener(v -> {
            currentFilters.clear();
            dialog.dismiss();
            loadBookings(progressBar, tvError);
        });

        dialog.show();
    }

    private void showCancelDialog(View view, Booking booking, ProgressBar pb, TextView err) {
        String policy = getString(R.string.default_cancellation_policy);

        if (booking.getActivityDetail() != null &&
                booking.getActivityDetail().getCancellationPolicy() != null &&
                !booking.getActivityDetail().getCancellationPolicy().trim().isEmpty()) {
            policy = booking.getActivityDetail().getCancellationPolicy();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_cancel_booking)
                .setMessage(getString(R.string.msg_cancel_policy, policy))
                .setPositiveButton(R.string.action_confirm,
                        (d, w) -> performApiCancel(view, booking.getId(), pb, err))
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    private void performApiCancel(View view, int bookingId, ProgressBar progressBar, TextView tvError) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.cancelBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call,
                                   @NonNull Response<ApiResponse<Booking>> response) {

                if (response.isSuccessful()) {
                    loadBookings(progressBar, tvError);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(view, getString(R.string.error_http, response.code()), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(view, R.string.error_connection, Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
