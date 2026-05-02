package com.example.xplorenow.news;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.xplorenow.R;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.News;
import com.example.xplorenow.data.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NewsDetailFragment extends Fragment {

    private static final String TAG = "NewsDetailFragment";

    @Inject ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);
        ImageView ivImage = view.findViewById(R.id.ivImage);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvRelatedActivity = view.findViewById(R.id.tvRelatedActivity);
        Button btnVerActividad = view.findViewById(R.id.btnVerActividad);



        int newsId = -1;
        if (getArguments() != null) {
            newsId = getArguments().getInt("newsId", -1);
        }

        if (newsId == -1) {
            tvError.setText(getString(R.string.news_not_found));
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.getNewsDetail(newsId).enqueue(new Callback<ApiResponse<News>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<News>> call, @NonNull Response<ApiResponse<News>> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    News news = response.body().getData();

                    tvTitle.setText(news.getTitle());
                    tvDate.setText(news.getCreatedAt());
                    tvDescription.setText(news.getDescription());

                    if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                        ivImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(news.getImageUrl()).into(ivImage);
                    }

                    if (news.getRelatedActivity() != null) {
                        tvRelatedActivity.setText(getString(R.string.news_related_activity, news.getRelatedActivity().getTitle()));
                        tvRelatedActivity.setVisibility(View.VISIBLE);
                        btnVerActividad.setVisibility(View.VISIBLE);
                        btnVerActividad.setOnClickListener(v -> {
                            Bundle args = new Bundle();
                            args.putInt("activityId", news.getRelatedActivity().getId());
                            Navigation.findNavController(view).navigate(
                                    R.id.action_newsDetail_to_activityDetail, args);
                        });
                    }
                } else {
                    Log.e(TAG, "Error al cargar noticia: " + response.code());
                    tvError.setText(getString(R.string.error_http, response.code()));
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<News>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                Log.e(TAG, "Fallo de red al cargar noticia", t);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }
}
