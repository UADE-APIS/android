package com.example.xplorenow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xplorenow.R;
import com.example.xplorenow.data.model.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<Activity> activities = new ArrayList<>();
    private final OnActivityClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    // Interfaces dentro del Adapter (Regla 25)
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

    /**
     * Actualiza un único ítem sin redibujar toda la lista.
     * Llamar desde el Fragment tras un toggle de favorito.
     */
    public void notifyActivityChanged(Activity activity) {
        int index = activities.indexOf(activity);
        if (index >= 0) notifyItemChanged(index);
    }

    public void clear() {
        this.activities.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sin View Binding (Regla 22)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        
        holder.tvTitle.setText(activity.getTitle());
        holder.tvLocation.setText(activity.getLocation());
        holder.tvCategory.setText(activity.getCategory().toUpperCase().replace("_", " "));
        holder.tvDuration.setText(holder.itemView.getContext().getString(R.string.item_duration_format, activity.getDuration()));
        holder.tvPrice.setText(holder.itemView.getContext().getString(R.string.item_price_format, activity.getPrice()));
        holder.tvSlots.setText(holder.itemView.getContext().getString(R.string.item_slots_format, activity.getAvailableSlots()));

        if (activity.getImages() != null && !activity.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(activity.getImages().get(0).getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivActivityImage);
        } else {
            holder.ivActivityImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.ivFavorite.setImageResource(
                activity.isFavorited()
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        // Click en vistas individuales solo si es necesario (favorito), 
        // pero el principal va en itemView (Regla 24)
        holder.ivFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(activity);
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onActivityClick(activity));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        // Campos de clase para el ViewHolder (Regla 23)
        final ImageView ivActivityImage;
        final ImageView ivFavorite;
        final TextView tvTitle;
        final TextView tvLocation;
        final TextView tvCategory;
        final TextView tvDuration;
        final TextView tvPrice;
        final TextView tvSlots;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            // findViewById SOLO en el constructor (Regla 23)
            ivActivityImage = itemView.findViewById(R.id.ivActivityImage);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSlots = itemView.findViewById(R.id.tvSlots);
        }
    }
}
