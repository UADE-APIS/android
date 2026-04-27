package com.example.xplorenow.news;

import android.os.Bundle;
import android.util.Log;
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
import com.example.xplorenow.adapters.NewsAdapter;
import com.example.xplorenow.data.model.News;
import com.example.xplorenow.data.model.NewsListResponse;
import com.example.xplorenow.data.model.Pagination;
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
public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";

    @Inject ApiService apiService;

    private NewsAdapter adapter;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private final int pageSize = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        TextView tvError = view.findViewById(R.id.tvError);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        RecyclerView rvNews = view.findViewById(R.id.rvNews);

        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        adapter = new NewsAdapter(news -> {
            Bundle args = new Bundle();
            args.putInt("newsId", news.getId());
            Navigation.findNavController(view).navigate(R.id.action_news_to_newsDetail, args);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvNews.setLayoutManager(layoutManager);
        rvNews.setAdapter(adapter);

        rvNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && currentPage < totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        cargarNoticias(currentPage + 1, progressBar, tvError);
                    }
                }
            }
        });

        cargarNoticias(1, progressBar, tvError);
    }

    private void cargarNoticias(int page, ProgressBar progressBar, TextView tvError) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", String.valueOf(page));
        queryParams.put("page_size", String.valueOf(pageSize));

        apiService.getNews(queryParams).enqueue(new Callback<NewsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsListResponse> call, @NonNull Response<NewsListResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<News> results = response.body().getResults();
                    Pagination pagination = response.body().getPagination();

                    if (pagination != null) {
                        currentPage = pagination.getPage();
                        totalPages = pagination.getTotalPages();
                    }

                    if (page == 1) {
                        adapter.setItems(results != null ? results : new ArrayList<>());
                    } else if (results != null) {
                        adapter.addItems(results);
                    }
                } else {
                    Log.e(TAG, "Error al cargar noticias: " + response.code());
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsListResponse> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                Log.e(TAG, "Fallo de red al cargar noticias", t);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }
}
