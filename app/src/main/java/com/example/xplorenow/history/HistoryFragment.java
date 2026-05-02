package com.example.xplorenow.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.HistoryAdapter;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.HistoryItem;
import com.example.xplorenow.data.model.HistoryListResponse;
import com.example.xplorenow.data.model.Pagination;
import com.example.xplorenow.data.model.Review;
import com.example.xplorenow.data.model.ReviewRequest;
import com.example.xplorenow.data.network.ApiService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";

    @Inject ApiService apiService;

    private HistoryAdapter adapter;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private final int pageSize = 10;
    private final Map<String, String> currentFilters = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvError = view.findViewById(R.id.tvError);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        RecyclerView rvHistory = view.findViewById(R.id.rvHistory);

        adapter = new HistoryAdapter(
                item -> mostrarDialogoCalificacion(item.getId(), progressBar, tvError),
                item -> {
                    Bundle args = new Bundle();
                    args.putInt("activityId", item.getActivityId());
                    Navigation.findNavController(view).navigate(
                            R.id.action_history_to_activityDetail, args);
                }
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setAdapter(adapter);

        rvHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && currentPage < totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        cargarHistorial(currentPage + 1, progressBar, tvError);
                    }
                }
            }
        });

        view.findViewById(R.id.fabFilter).setOnClickListener(v -> mostrarDialogoFiltros(progressBar, tvError));

        cargarHistorial(1, progressBar, tvError);
    }

    private void cargarHistorial(int page, ProgressBar progressBar, TextView tvError) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        if (page == 1) tvError.setVisibility(View.GONE);

        Map<String, String> queryParams = new HashMap<>(currentFilters);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("page_size", String.valueOf(pageSize));

        apiService.getHistory(queryParams).enqueue(new Callback<HistoryListResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistoryListResponse> call, @NonNull Response<HistoryListResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<HistoryItem> resultItems = response.body().getResults();
                    Pagination pagination = response.body().getPagination();

                    if (pagination != null) {
                        currentPage = pagination.getPage();
                        totalPages = pagination.getTotalPages();
                    }

                    if (page == 1) {
                        adapter.setItems(resultItems != null ? resultItems : new ArrayList<>());
                        if (resultItems == null || resultItems.isEmpty()) {
                            tvError.setText("No se encontraron actividades en el historial");
                            tvError.setVisibility(View.VISIBLE);
                        }
                    } else if (resultItems != null) {
                        adapter.addItems(resultItems);
                    }
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryListResponse> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarDialogoFiltros(ProgressBar progressBar, TextView tvError) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_history_filters, null);

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

        // Populate fields with current filters
        if (currentFilters.containsKey("name")) etName.setText(currentFilters.get("name"));
        if (currentFilters.containsKey("destination")) etLocation.setText(currentFilters.get("destination"));
        if (currentFilters.containsKey("guide")) etGuide.setText(currentFilters.get("guide"));
        if (currentFilters.containsKey("duration")) etDuration.setText(currentFilters.get("duration"));
        if (currentFilters.containsKey("date")) etDate.setText(currentFilters.get("date"));
        if (currentFilters.containsKey("date_from")) etDateFrom.setText(currentFilters.get("date_from"));
        if (currentFilters.containsKey("date_to")) etDateTo.setText(currentFilters.get("date_to"));

        // Setup Ordering Spinner
        String[] orderings = {"Más recientes", "Más antiguos", "Duración", "Actividad (A-Z)"};
        String[] orderingValues = {"-date", "date", "activity__duration", "activity__name"};
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, orderings);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdering.setAdapter(orderAdapter);

        // Select current ordering if exists
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

            int orderPos = spinnerOrdering.getSelectedItemPosition();
            currentFilters.put("ordering", orderingValues[orderPos]);

            dialog.dismiss();
            cargarHistorial(1, progressBar, tvError);
        });

        btnResetFilters.setOnClickListener(v -> {
            currentFilters.clear();
            dialog.dismiss();
            cargarHistorial(1, progressBar, tvError);
        });

        dialog.show();
    }

    private void mostrarDialogoCalificacion(int bookingId, ProgressBar progressBar, TextView tvError) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_review, null);

        RatingBar ratingActivity = dialogView.findViewById(R.id.ratingActivity);
        RatingBar ratingGuide = dialogView.findViewById(R.id.ratingGuide);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);
        TextView tvDialogError = dialogView.findViewById(R.id.tvDialogError);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnSubmitReview.setOnClickListener(v -> {
            int activityRating = (int) ratingActivity.getRating();
            int guideRating = (int) ratingGuide.getRating();

            if (activityRating == 0 || guideRating == 0) {
                tvDialogError.setText(R.string.history_review_fill_ratings);
                tvDialogError.setVisibility(View.VISIBLE);
                return;
            }

            tvDialogError.setVisibility(View.GONE);
            String comment = etComment.getText().toString().trim();

            apiService.createReview(bookingId, new ReviewRequest(activityRating, guideRating, comment)).enqueue(new Callback<ApiResponse<Review>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Review>> call, @NonNull Response<ApiResponse<Review>> response) {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    if (response.isSuccessful()) {
                        cargarHistorial(1, progressBar, tvError);
                    } else {
                        Snackbar.make(requireView(), getString(R.string.error_http, response.code()), Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Review>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    Snackbar.make(requireView(), R.string.error_connection, Snackbar.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
