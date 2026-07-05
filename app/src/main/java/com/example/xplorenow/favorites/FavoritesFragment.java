package com.example.xplorenow.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.ActivitiesAdapter;
import com.example.xplorenow.data.local.CachedFavorite;
import com.example.xplorenow.data.local.CachedFavoriteDao;
import com.example.xplorenow.data.model.ActivitiesListResponse;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.Pagination;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.data.network.dto.WrappedResponse;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment implements ActivitiesAdapter.OnActivityClickListener {

    @Inject ApiService apiService;
    @Inject CachedFavoriteDao cachedFavoriteDao;

    private ActivitiesAdapter adapter;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private final int pageSize = 10;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvError = view.findViewById(R.id.tvError);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        RecyclerView rvFavorites = view.findViewById(R.id.rvFavorites);

        adapter = new ActivitiesAdapter(this);
        adapter.setOnFavoriteClickListener(activity -> toggleFavorite(activity, progressBar, tvError));

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvFavorites.setLayoutManager(layoutManager);
        rvFavorites.setAdapter(adapter);

        rvFavorites.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && currentPage < totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        cargarFavoritos(currentPage + 1, progressBar, tvError);
                    }
                }
            }
        });

        cargarFavoritos(1, progressBar, tvError);
    }

    private void cargarFavoritos(int page, ProgressBar progressBar, TextView tvError) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", String.valueOf(page));
        queryParams.put("page_size", String.valueOf(pageSize));

        apiService.getMyFavorites(queryParams).enqueue(new Callback<ActivitiesListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivitiesListResponse> call, @NonNull Response<ActivitiesListResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Activity> activities = response.body().getResults();
                    Pagination pagination = response.body().getPagination();

                    if (pagination != null) {
                        currentPage = pagination.getPage();
                        totalPages = pagination.getTotalPages();
                    }

                    List<Activity> safeActivities = activities != null ? activities : new ArrayList<>();

                    if (page == 1) {
                        procesarNovedadesFavoritos(safeActivities, () -> adapter.setActivities(safeActivities));
                    } else {
                        procesarNovedadesFavoritos(safeActivities, () -> adapter.addActivities(safeActivities));
                    }
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivitiesListResponse> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                tvError.setText(R.string.error_connection);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void procesarNovedadesFavoritos(List<Activity> activities, Runnable onFinish) {
        databaseExecutor.execute(() -> {
            for (Activity activity : activities) {
                double currentPrice = parsePrice(activity.getPrice());
                int currentSlots = activity.getAvailableSlots();

                CachedFavorite cached = cachedFavoriteDao.getFavoriteByActivityId(activity.getId());

                if (cached != null) {
                    boolean priceDropped = currentPrice < cached.getLastSeenPrice();
                    boolean slotsReleased = currentSlots > cached.getLastSeenSlots();

                    if (priceDropped) {
                        activity.setFavoriteHasNews(true);
                        activity.setFavoriteNewsText("¡Precio actualizado!");
                    } else if (slotsReleased) {
                        activity.setFavoriteHasNews(true);
                        activity.setFavoriteNewsText("¡Hay nuevos cupos!");
                    } else {
                        activity.setFavoriteHasNews(false);
                        activity.setFavoriteNewsText(null);
                    }
                }

                cachedFavoriteDao.insertFavorite(new CachedFavorite(
                        activity.getId(),
                        currentPrice,
                        currentSlots
                ));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    onFinish.run();
                });
            }
        });
    }

    private double parsePrice(String price) {
        if (price == null) return 0.0;

        try {
            String normalized = price
                    .replaceAll("[^0-9,.-]", "")
                    .replace(".", "")
                    .replace(",", ".");

            if (normalized.isEmpty()) return 0.0;

            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void toggleFavorite(Activity activity, ProgressBar progressBar, TextView tvError) {
        apiService.toggleFavorite(activity.getId()).enqueue(new Callback<WrappedResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<WrappedResponse<Void>> call, @NonNull Response<WrappedResponse<Void>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    databaseExecutor.execute(() -> cachedFavoriteDao.deleteFavorite(activity.getId()));

                    Snackbar.make(requireView(), R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                    cargarFavoritos(1, progressBar, tvError);
                } else {
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WrappedResponse<Void>> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                tvError.setText(R.string.error_connection);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityClick(Activity activity) {
        Bundle args = new Bundle();
        args.putInt("activityId", activity.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_favorites_to_activityDetail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseExecutor.shutdown();
    }
}