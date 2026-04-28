package com.example.xplorenow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.News;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<News> items = new ArrayList<>();
    private final OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(News news);
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }

    public NewsAdapter(OnNewsClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<News> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItems(List<News> newItems) {
        int start = this.items.size();
        this.items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        NewsViewHolder holder = new NewsViewHolder(view);

        holder.itemView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onNewsClick(items.get(position));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = items.get(position);
        holder.tvTitle.setText(news.getTitle());
        holder.tvDescription.setText(news.getDescription());
    }

    @Override
    public int getItemCount() { return items.size(); }
}
