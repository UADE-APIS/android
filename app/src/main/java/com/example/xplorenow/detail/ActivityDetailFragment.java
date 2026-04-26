package com.example.xplorenow.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xplorenow.R;
import com.example.xplorenow.adapters.GalleryAdapter;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.ActivityAvailability;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.network.ApiService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ActivityDetailFragment extends Fragment {

    private static final String TAG = "ActivityDetailFragment";

    @Inject
    ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referencias a las vistas — locales en onViewCreated, igual que CreateBookingFragment
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);
        ImageView ivMainImage = view.findViewById(R.id.ivMainImage);
        LinearLayout layoutContent = view.findViewById(R.id.layoutContent);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvLocation = view.findViewById(R.id.tvLocation);
        TextView tvCategory = view.findViewById(R.id.tvCategory);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvGalleryLabel = view.findViewById(R.id.tvGalleryLabel);
        RecyclerView rvGallery = view.findViewById(R.id.rvGallery);
        TextView tvIncludes = view.findViewById(R.id.tvIncludes);
        TextView tvMeetingPoint = view.findViewById(R.id.tvMeetingPoint);
        TextView tvGuide = view.findViewById(R.id.tvGuide);
        TextView tvLanguage = view.findViewById(R.id.tvLanguage);
        TextView tvCancellation = view.findViewById(R.id.tvCancellation);
        TextView tvAvailableDatesLabel = view.findViewById(R.id.tvAvailableDatesLabel);
        LinearLayout layoutAvailabilities = view.findViewById(R.id.layoutAvailabilities);
        Button btnReservar = view.findViewById(R.id.btnReservar);

        int activityId = getArguments() != null ? getArguments().getInt("activityId", -1) : -1;

        if (activityId == -1) {
            tvError.setText(getString(R.string.error_invalid_activity));
            tvError.setVisibility(View.VISIBLE);
            btnReservar.setEnabled(false);
            return;
        }

        // Galería adapter: sin ViewBinding, estilo del proyecto
        GalleryAdapter galleryAdapter = new GalleryAdapter();
        rvGallery.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvGallery.setAdapter(galleryAdapter);

        // Cargar datos de la actividad
        progressBar.setVisibility(View.VISIBLE);

        apiService.getActivity(activityId).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Activity>> call,
                                   @NonNull Response<ApiResponse<Activity>> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    tvError.setText(getString(R.string.error_loading_data));
                    tvError.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error HTTP: " + response.code());
                    return;
                }

                Activity activity = response.body().getData();

                // Imagen principal (primera si existe)
                if (activity.getImages() != null && !activity.getImages().isEmpty()) {
                    ivMainImage.setVisibility(View.VISIBLE);
                    Glide.with(requireContext())
                            .load(activity.getImages().get(0).getImageUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .into(ivMainImage);

                    // Galería si hay más de una imagen
                    if (activity.getImages().size() > 1) {
                        tvGalleryLabel.setVisibility(View.VISIBLE);
                        rvGallery.setVisibility(View.VISIBLE);
                        galleryAdapter.setImages(activity.getImages());
                    }
                }

                // Datos básicos
                tvTitle.setText(activity.getTitle());
                tvLocation.setText(activity.getLocation());
                tvCategory.setText(activity.getCategory());
                tvPrice.setText(getString(R.string.detail_price_format, activity.getPrice()));
                tvDuration.setText(getString(R.string.detail_duration_format, activity.getDuration()));
                tvDescription.setText(activity.getDescription());
                tvIncludes.setText(activity.getIncludes());
                tvMeetingPoint.setText(activity.getMeetingPoint());
                tvGuide.setText(activity.getAssignedGuide());
                tvLanguage.setText(activity.getLanguage());
                tvCancellation.setText(activity.getCancellationPolicy());

                // Availabilities: mostrar cada fecha + cupos disponibles
                List<ActivityAvailability> availabilities = activity.getAvailabilities();
                if (availabilities != null && !availabilities.isEmpty()) {
                    tvAvailableDatesLabel.setVisibility(View.VISIBLE);
                    layoutAvailabilities.setVisibility(View.VISIBLE);

                    for (ActivityAvailability avail : availabilities) {
                        TextView tvAvail = new TextView(requireContext());
                        tvAvail.setTextSize(14);
                        tvAvail.setText(getString(R.string.detail_availability_row,
                                avail.getDate(), avail.getAvailableSlots()));
                        layoutAvailabilities.addView(tvAvail);
                    }
                }

                // Mostrar contenido y habilitar botón reservar
                layoutContent.setVisibility(View.VISIBLE);

                btnReservar.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putInt("activityId", activityId);
                    Navigation.findNavController(view).navigate(R.id.action_activityDetailFragment_to_createBookingFragment, args);
                });
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Activity>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText(getString(R.string.error_connection));
                tvError.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
