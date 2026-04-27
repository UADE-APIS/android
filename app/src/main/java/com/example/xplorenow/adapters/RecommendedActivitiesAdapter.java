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

public class RecommendedActivitiesAdapter extends RecyclerView.Adapter<RecommendedActivitiesAdapter.RecommendedViewHolder> {

    private List<Activity> activities = new ArrayList<>();
    private final ActivitiesAdapter.OnActivityClickListener listener;

    public RecommendedActivitiesAdapter(ActivitiesAdapter.OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecommendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_horizontal, parent, false);
        return new RecommendedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class RecommendedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvLocation, tvPrice;

        public RecommendedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onActivityClick(activities.get(position));
                }
            });
        }

        public void bind(Activity activity) {
            tvTitle.setText(activity.getTitle());
            tvLocation.setText(activity.getLocation());
            tvPrice.setText(itemView.getContext().getString(R.string.detail_price_format, activity.getPrice()));

            if (activity.getImages() != null && !activity.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(activity.getImages().get(0).getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(ivImage);
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}
