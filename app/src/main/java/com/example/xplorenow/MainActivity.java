package com.example.xplorenow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.adapters.ActivitiesAdapter;
import com.example.xplorenow.databinding.ActivityMainBinding;
import com.example.xplorenow.databinding.DialogFiltersBinding;
import com.example.xplorenow.models.Activity;
import com.example.xplorenow.models.ApiResponse;
import com.example.xplorenow.models.Pagination;
import com.example.xplorenow.network.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ActivitiesAdapter.OnActivityClickListener {

    private ActivityMainBinding binding;
    private ActivitiesAdapter adapter;
    
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private final int PAGE_SIZE = 10;
    
    // Almacenar los filtros actuales
    private final Map<String, String> currentFilters = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupFilters();
        fetchActivities(1);
    }

    private void setupRecyclerView() {
        adapter = new ActivitiesAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvActivities.setLayoutManager(layoutManager);
        binding.rvActivities.setAdapter(adapter);

        binding.rvActivities.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void setupFilters() {
        binding.fabFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        DialogFiltersBinding filterBinding = DialogFiltersBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(filterBinding.getRoot())
                .create();

        // Configurar Spinners
        String[] categories = {"Todas", "free_tour", "guided_tour", "excursion", "gastronomic", "adventure", "other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterBinding.spinnerCategory.setAdapter(catAdapter);

        String[] orderings = {"Relevancia", "Precio Asc", "Precio Desc", "Recientes", "Duración"};
        String[] orderingValues = {"", "price", "-price", "-created_at", "duration"};
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orderings);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterBinding.spinnerOrdering.setAdapter(orderAdapter);

        // Pre-cargar valores actuales si existen
        if (currentFilters.containsKey("search")) filterBinding.etSearch.setText(currentFilters.get("search"));
        if (currentFilters.containsKey("location")) filterBinding.etLocation.setText(currentFilters.get("location"));
        if (currentFilters.containsKey("min_price")) filterBinding.etMinPrice.setText(currentFilters.get("min_price"));
        if (currentFilters.containsKey("max_price")) filterBinding.etMaxPrice.setText(currentFilters.get("max_price"));
        if (currentFilters.containsKey("is_featured")) filterBinding.cbIsFeatured.setChecked(currentFilters.get("is_featured").equals("true"));

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
            fetchActivities(1); // Reiniciar a la primera página con nuevos filtros
        });

        filterBinding.btnResetFilters.setOnClickListener(v -> {
            currentFilters.clear();
            dialog.dismiss();
            fetchActivities(1);
        });

        dialog.show();
    }

    private void fetchActivities(int page) {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Map<String, String> queryParams = new HashMap<>(currentFilters);
        queryParams.put("page", String.valueOf(page));
        queryParams.put("page_size", String.valueOf(PAGE_SIZE));
        
        RetrofitClient.getApiService().getActivities(queryParams).enqueue(new Callback<ApiResponse<List<Activity>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Activity>>> call, Response<ApiResponse<List<Activity>>> response) {
                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body().getData();
                    Pagination pagination = response.body().getPagination();
                    
                    if (pagination != null) {
                        currentPage = pagination.getPage();
                        totalPages = pagination.getTotalPages();
                    }

                    if (page == 1) {
                        adapter.setActivities(activities != null ? activities : new java.util.ArrayList<>());
                    } else if (activities != null) {
                        adapter.addActivities(activities);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Activity>>> call, Throwable t) {
                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityClick(Activity activity) {
        Toast.makeText(this, "Click en: " + activity.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
