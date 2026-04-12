package com.example.xplorenow;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.adapters.ActivitiesAdapter;
import com.example.xplorenow.databinding.ActivityMainBinding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        fetchActivities(1); // Carga la primera página
    }

    private void setupRecyclerView() {
        adapter = new ActivitiesAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvActivities.setLayoutManager(layoutManager);
        binding.rvActivities.setAdapter(adapter);

        // Detectar scroll para cargar más
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

    private void fetchActivities(int page) {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Map<String, String> filters = new HashMap<>();
        filters.put("page", String.valueOf(page));
        filters.put("page_size", String.valueOf(PAGE_SIZE));
        
        RetrofitClient.getApiService().getActivities(filters).enqueue(new Callback<ApiResponse<List<Activity>>>() {
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

                    if (activities != null && !activities.isEmpty()) {
                        if (page == 1) {
                            adapter.setActivities(activities);
                        } else {
                            adapter.addActivities(activities);
                        }
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
