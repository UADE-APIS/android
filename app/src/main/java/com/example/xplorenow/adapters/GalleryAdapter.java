package com.example.xplorenow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xplorenow.R;
import com.example.xplorenow.data.model.ActivityImage;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<ActivityImage> images = new ArrayList<>();

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGalleryImage;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryImage = itemView.findViewById(R.id.ivGalleryImage);
        }
    }

    public void setImages(List<ActivityImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        ActivityImage image = images.get(position);
        Glide.with(holder.itemView.getContext())
                .load(image.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivGalleryImage);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
