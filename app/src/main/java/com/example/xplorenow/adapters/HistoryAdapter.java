package com.example.xplorenow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> items = new ArrayList<>();
    private final OnHistoryClickListener listener;

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityTitle, tvLocation, tvDate, tvDuration, tvGuide, tvQuantity, tvReviewed;
        Button btnReview;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvGuide = itemView.findViewById(R.id.tvGuide);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnReview = itemView.findViewById(R.id.btnReview);
            tvReviewed = itemView.findViewById(R.id.tvReviewed);
        }
    }

    private final OnItemClickListener itemListener;

    public interface OnHistoryClickListener {
        void onReviewClick(HistoryItem item);
    }

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    public HistoryAdapter(OnHistoryClickListener listener, OnItemClickListener itemListener) {
        this.listener = listener;
        this.itemListener = itemListener;
    }

    public void setItems(List<HistoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItems(List<HistoryItem> newItems) {
        int start = this.items.size();
        this.items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvActivityTitle.setText(item.getActivityTitle());
        holder.tvLocation.setText(context.getString(R.string.history_location, item.getActivityLocation()));
        holder.tvDate.setText(context.getString(R.string.history_date, item.getDate() != null ? item.getDate() : "-"));
        holder.tvDuration.setText(context.getString(R.string.history_duration, item.getActivityDuration()));
        holder.tvGuide.setText(context.getString(R.string.history_guide, item.getAssignedGuide()));
        holder.tvQuantity.setText(context.getString(R.string.history_quantity, item.getQuantity()));

        boolean reviewed = item.getReview() != null && item.getReview().getId() > 0;
        holder.btnReview.setVisibility(reviewed ? View.GONE : View.VISIBLE);
        holder.tvReviewed.setVisibility(reviewed ? View.VISIBLE : View.GONE);

        holder.btnReview.setOnClickListener(v -> {
            if (listener != null) listener.onReviewClick(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemListener != null) itemListener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }
}
