package com.example.xplorenow.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.ActivitiesAdapter;
import com.example.xplorenow.adapters.RecommendedActivitiesAdapter;
import com.example.xplorenow.data.model.ActivitiesListResponse;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.Pagination;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.LogoutRequest;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.example.xplorenow.data.session.TokenManager;
import com.example.xplorenow.databinding.DialogFiltersBinding;
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
public class HomeFragment extends Fragment implements ActivitiesAdapter.OnActivityClickListener {

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;

    private ActivitiesAdapter adapter;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private final int pageSize = 10;
    private final Map<String, String> currentFilters = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // REGLA 3: Vistas como variables locales
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        TextView tvError = view.findViewById(R.id.tvError);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        RecyclerView rvActivities = view.findViewById(R.id.rvActivities);
        RecyclerView rvRecommended = view.findViewById(R.id.rvRecommended);
        TextView tvRecommendedLabel = view.findViewById(R.id.tvRecommendedLabel);

        if (!tokenManager.isLoggedIn()) {
            handleUnauthorized(view);
            return;
        }

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                doLogout(view, tvError);
                return true;
            }
            if (item.getItemId() == R.id.action_profile) {
                Navigation.findNavController(view).navigate(R.id.action_home_to_profile);
                return true;
            }
            if (item.getItemId() == R.id.action_my_bookings) {
                Navigation.findNavController(view).navigate(R.id.action_home_to_myBookings);
                return true;
            }
            return false;
        });

        view.findViewById(R.id.fabFilter).setOnClickListener(v -> showFilterDialog(tvError, progressBar));

        setupRecyclerView(rvActivities, progressBar, tvError);
        fetchActivities(1, progressBar, tvError);

        RecommendedActivitiesAdapter recommendedAdapter = new RecommendedActivitiesAdapter(this);
        rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommended.setAdapter(recommendedAdapter);
        fetchRecommendedActivities(recommendedAdapter, rvRecommended, tvRecommendedLabel, tvError);
    }

    private void setupRecyclerView(RecyclerView rvActivities, ProgressBar progressBar, TextView tvError) {
        adapter = new ActivitiesAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvActivities.setLayoutManager(layoutManager);
        rvActivities.setAdapter(adapter);

        rvActivities.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && currentPage < totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        fetchActivities(currentPage + 1, progressBar, tvError);
                    }
                }
            }
        });
    }

    private void showFilterDialog(TextView tvError, ProgressBar progressBar) {
        DialogFiltersBinding filterBinding = DialogFiltersBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(filterBinding.getRoot())
                .create();

        String[] categories = {"Todas", "free_tour", "guided_tour", "excursion", "gastronomic", "adventure", "other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterBinding.spinnerCategory.setAdapter(catAdapter);

        String[] orderings = {"Relevancia", "Precio Asc", "Precio Desc", "Recientes", "Duración"};
        String[] orderingValues = {"", "price", "-price", "-created_at", "duration"};
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, orderings);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterBinding.spinnerOrdering.setAdapter(orderAdapter);

        if (currentFilters.containsKey("search")) filterBinding.etSearch.setText(currentFilters.get("search"));
        if (currentFilters.containsKey("location")) filterBinding.etLocation.setText(currentFilters.get("location"));
        if (currentFilters.containsKey("min_price")) filterBinding.etMinPrice.setText(currentFilters.get("min_price"));
        if (currentFilters.containsKey("max_price")) filterBinding.etMaxPrice.setText(currentFilters.get("max_price"));
        if (currentFilters.containsKey("is_featured")) {
            filterBinding.cbIsFeatured.setChecked("true".equals(currentFilters.get("is_featured")));
        }

        filterBinding.btnApplyFilters.setOnClickListener(v -> {
            currentFilters.clear();
            String search = filterBinding.etSearch.getText().toString().trim();
            if (!search.isEmpty()) currentFilters.put("search", search);

            String category = filterBinding.spinnerCategory.getSelectedItem().toString();
            if (!category.equals("Todas")) currentFilters.put("category", category);

            String location = filterBinding.etLocation.getText().toString().trim();
            if (!location.isEmpty()) currentFilters.put("location", location);

            String minPrice = filterBinding.etMinPrice.getText().toString().trim();
            if (!minPrice.isEmpty()) currentFilters.put("min_price", minPrice);

            String maxPrice = filterBinding.etMaxPrice.getText().toString().trim();
            if (!maxPrice.isEmpty()) currentFilters.put("max_price", maxPrice);

            if (filterBinding.cbIsFeatured.isChecked()) currentFilters.put("is_featured", "true");

            int orderPos = filterBinding.spinnerOrdering.getSelectedItemPosition();
            if (orderPos > 0) currentFilters.put("ordering", orderingValues[orderPos]);

            dialog.dismiss();
            tvError.setVisibility(View.GONE);
            fetchActivities(1, progressBar, tvError);
        });

        filterBinding.btnResetFilters.setOnClickListener(v -> {
            currentFilters.clear();
            dialog.dismiss();
            tvError.setVisibility(View.GONE);
            fetchActivities(1, progressBar, tvError);
        });

        dialog.show();
    }

    private void fetchActivities(int page, ProgressBar progressBar, TextView tvError) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> queryParams = new HashMap<>(currentFilters);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("page_size", String.valueOf(pageSize));

        apiService.getActivities(queryParams).enqueue(new Callback<ActivitiesListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivitiesListResponse> call, @NonNull Response<ActivitiesListResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.code() == 401) {
                    if (getView() != null) handleUnauthorized(getView());
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body().getResults();
                    Pagination pagination = response.body().getPagination();

                    if (pagination != null) {
                        currentPage = pagination.getPage();
                        totalPages = pagination.getTotalPages();
                    }

                    if (page == 1) {
                        adapter.setActivities(activities != null ? activities : new ArrayList<>());
                    } else if (activities != null) {
                        adapter.addActivities(activities);
                    }
                } else {
                    tvError.setText("Error: " + response.code());
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivitiesListResponse> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                tvError.setText("Error de red: " + t.getMessage());
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchRecommendedActivities(RecommendedActivitiesAdapter adapter, RecyclerView rv, TextView tvLabel, TextView tvError) {
        apiService.getRecommendedActivities(new HashMap<>()).enqueue(new Callback<ActivitiesListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivitiesListResponse> call, @NonNull Response<ActivitiesListResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body().getResults();
                    if (activities != null && !activities.isEmpty()) {
                        adapter.setActivities(activities);
                        rv.setVisibility(View.VISIBLE);
                        tvLabel.setVisibility(View.VISIBLE);
                    } else {
                        rv.setVisibility(View.GONE);
                        tvLabel.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivitiesListResponse> call, @NonNull Throwable t) {
                // If it fails we just hide the recommended section
                if (!isAdded()) return;
                rv.setVisibility(View.GONE);
                tvLabel.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityClick(Activity activity) {
        Bundle args = new Bundle();
        args.putInt("activityId", activity.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_createBooking, args);
    }

    private void doLogout(View rootView, TextView tvError) {
        tvError.setVisibility(View.GONE);
        tvError.setText("");

        String refresh = tokenManager.getRefreshToken();
        if (refresh == null || refresh.trim().isEmpty()) {
            tokenManager.clear();
            if (!isAdded()) return;
            Navigation.findNavController(rootView).navigate(R.id.action_home_to_authStart);
            return;
        }

        apiService.logout(new LogoutRequest(refresh)).enqueue(new Callback<WrappedResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<Void>> call, @NonNull Response<WrappedResponse<Void>> response) {
                tokenManager.clear();
                rootView.post(() -> {
                    if (!isAdded()) return;
                    Navigation.findNavController(rootView).navigate(R.id.action_home_to_authStart);
                });
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                tokenManager.clear();
                rootView.post(() -> {
                    if (!isAdded()) return;
                    Navigation.findNavController(rootView).navigate(R.id.action_home_to_authStart);
                });
            }
        });
    }

    private void handleUnauthorized(View view) {
        tokenManager.clear();
        if (!isAdded()) return;

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        Navigation.findNavController(view).navigate(R.id.authStartFragment, null, options);
    }
}