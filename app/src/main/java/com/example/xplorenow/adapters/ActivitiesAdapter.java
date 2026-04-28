package com.example.xplorenow.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xplorenow.databinding.ItemActivityBinding;
import com.example.xplorenow.data.model.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<Activity> activities = new ArrayList<>();
    private final OnActivityClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Activity activity);
    }

    public ActivitiesAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener favoriteListener) {
        this.favoriteListener = favoriteListener;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    public void addActivities(List<Activity> newActivities) {
        int startPosition = this.activities.size();
        this.activities.addAll(newActivities);
        notifyItemRangeInserted(startPosition, newActivities.size());
    }

    public void clear() {
        this.activities.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActivityBinding binding = ItemActivityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ActivityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        holder.bind(activities.get(position));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ItemActivityBinding binding;

        public ActivityViewHolder(ItemActivityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Activity activity) {
            binding.tvTitle.setText(activity.getTitle());
            binding.tvLocation.setText(activity.getLocation());
            binding.tvCategory.setText(activity.getCategory().toUpperCase().replace("_", " "));
            binding.tvDuration.setText("Duración: " + activity.getDuration() + "h");
            binding.tvPrice.setText("$" + activity.getPrice());
            binding.tvSlots.setText("Cupos disponibles: " + activity.getAvailableSlots());

            if (activity.getImages() != null && !activity.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(activity.getImages().get(0).getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivActivityImage);
            } else {
                binding.ivActivityImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            binding.ivFavorite.setImageResource(
                    activity.isFavorited()
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off
            );

            binding.ivFavorite.setOnClickListener(v -> {
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteClick(activity);
                }
            });

            itemView.setOnClickListener(v -> listener.onActivityClick(activity));
        }
    }
}
