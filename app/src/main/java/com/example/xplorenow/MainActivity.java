package com.example.xplorenow;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xplorenow.adapters.ActivitiesAdapter;
import com.example.xplorenow.databinding.ActivityMainBinding;
import com.example.xplorenow.models.Activity;
import com.example.xplorenow.models.ApiResponse;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        fetchActivities();
    }

    private void setupRecyclerView() {
        adapter = new ActivitiesAdapter(this);
        binding.rvActivities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvActivities.setAdapter(adapter);
    }

    private void fetchActivities() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Map<String, String> filters = new HashMap<>();
        
        RetrofitClient.getApiService().getActivities(filters).enqueue(new Callback<ApiResponse<List<Activity>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Activity>>> call, Response<ApiResponse<List<Activity>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body().getData();
                    if (activities != null && !activities.isEmpty()) {
                        adapter.setActivities(activities);
                    } else {
                        Toast.makeText(MainActivity.this, "No se encontraron actividades", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar actividades: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Activity>>> call, Throwable t) {
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
