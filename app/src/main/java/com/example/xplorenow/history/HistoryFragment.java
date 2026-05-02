package com.example.xplorenow.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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
import com.google.android.material.appbar.MaterialToolbar;

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
        tvError.setVisibility(View.GONE);

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
                    } else if (resultItems != null) {
                        adapter.addItems(resultItems);
                    }
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryListResponse> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void mostrarDialogoFiltros(ProgressBar progressBar, TextView tvError) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_history_filters, null);

        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        EditText etDateFrom = dialogView.findViewById(R.id.etDateFrom);
        EditText etDateTo = dialogView.findViewById(R.id.etDateTo);
        Button btnApplyFilters = dialogView.findViewById(R.id.btnApplyFilters);
        Button btnResetFilters = dialogView.findViewById(R.id.btnResetFilters);

        if (currentFilters.containsKey("location")) etLocation.setText(currentFilters.get("location"));
        if (currentFilters.containsKey("date_from")) etDateFrom.setText(currentFilters.get("date_from"));
        if (currentFilters.containsKey("date_to")) etDateTo.setText(currentFilters.get("date_to"));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnApplyFilters.setOnClickListener(v -> {
            currentFilters.clear();
            String loc = etLocation.getText().toString().trim();
            if (!loc.isEmpty()) currentFilters.put("location", loc);
            String df = etDateFrom.getText().toString().trim();
            if (!df.isEmpty()) currentFilters.put("date_from", df);
            String dt = etDateTo.getText().toString().trim();
            if (!dt.isEmpty()) currentFilters.put("date_to", dt);
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
                        tvError.setText(getString(R.string.error_http, response.code()));
                        tvError.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Review>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    tvError.setText(R.string.error_connection);
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        });

        dialog.show();
    }
}
