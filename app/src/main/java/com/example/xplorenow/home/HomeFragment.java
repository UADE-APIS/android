package com.example.xplorenow.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.ActivitiesAdapter;
import com.example.xplorenow.data.session.SessionStore;
import com.example.xplorenow.databinding.DialogFiltersBinding;
import com.example.xplorenow.models.Activity;
import com.example.xplorenow.models.ApiResponse;
import com.example.xplorenow.models.Pagination;
import com.example.xplorenow.network.ApiService;
import com.example.xplorenow.network.AuthInterceptor;
import com.example.xplorenow.network.RetrofitClient;
import com.example.xplorenow.network.RetrofitProvider;
import com.example.xplorenow.network.dto.LogoutRequest;
import com.example.xplorenow.network.dto.WrappedResponse;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ActivitiesAdapter.OnActivityClickListener {

    private MaterialToolbar toolbar;
    private TextView tvError;
    private ProgressBar progressBar;
    private RecyclerView rvActivities;
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

        toolbar = view.findViewById(R.id.toolbar);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);
        rvActivities = view.findViewById(R.id.rvActivities);

        toolbar.setSubtitle(getString(R.string.home_subtitle));
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                doLogout(view);
                return true;
            }
            return false;
        });

        view.findViewById(R.id.fabFilter).setOnClickListener(v -> showFilterDialog());

        setupRecyclerView();
        fetchActivities(1);
    }

    private void setupRecyclerView() {
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
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        fetchActivities(currentPage + 1);
                    }
                }
            }
        });
    }

    private void showFilterDialog() {
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
            fetchActivities(1);
        });

        filterBinding.btnResetFilters.setOnClickListener(v -> {
            currentFilters.clear();
            dialog.dismiss();
            tvError.setVisibility(View.GONE);
            fetchActivities(1);
        });

        dialog.show();
    }

    private void fetchActivities(int page) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> queryParams = new HashMap<>(currentFilters);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("page_size", String.valueOf(pageSize));

        RetrofitClient.getApiService().getActivities(queryParams).enqueue(new Callback<ApiResponse<List<Activity>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Activity>>> call, @NonNull Response<ApiResponse<List<Activity>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body().getData();
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
            public void onFailure(@NonNull Call<ApiResponse<List<Activity>>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                tvError.setText("Error de red: " + t.getMessage());
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityClick(Activity activity) {
        Toast.makeText(requireContext(), "Click en: " + activity.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void doLogout(View rootView) {
        tvError.setVisibility(View.GONE);
        tvError.setText("");

        SessionStore store = SessionStore.getInstance(requireContext());
        store.getRefreshToken().subscribe(refresh -> {
            if (refresh == null || refresh.trim().isEmpty()) {
                store.clear().subscribe(() -> rootView.post(() -> {
                    if (!isAdded()) return;
                    Navigation.findNavController(rootView).navigate(R.id.action_home_to_authStart);
                }));
                return;
            }

            ApiService api = RetrofitProvider
                    .getRetrofit(RetrofitProvider.buildAuthedClient(new AuthInterceptor(requireContext())))
                    .create(ApiService.class);

            api.logout(new LogoutRequest(refresh)).enqueue(new Callback<WrappedResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<WrappedResponse<Void>> call, @NonNull Response<WrappedResponse<Void>> response) {
                    store.clear().subscribe(() -> rootView.post(() -> {
                        if (!isAdded()) return;
                        Navigation.findNavController(rootView).navigate(R.id.action_home_to_authStart);
                    }));
                }

                @Override
                public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                    store.clear().subscribe(() -> rootView.post(() -> {
                        if (!isAdded()) return;
                        Navigation.findNavController(rootView).navigate(R.id.action_home_to_authStart);
                    }));
                }
            });
        }, throwable -> rootView.post(() -> {
            if (!isAdded()) return;
            tvError.setText("No se pudo leer la sesión.");
            tvError.setVisibility(View.VISIBLE);
        }));
    }
}
