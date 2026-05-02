package com.example.xplorenow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xplorenow.R;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private final OnBookingInteractionListener listener;

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityTitle, tvDate, tvQuantity, tvStatus;
        ImageView ivActivityImage;
        Button btnCancel;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivActivityImage = itemView.findViewById(R.id.ivActivityImage);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    public interface OnBookingInteractionListener {
        void onCancelClick(Booking booking);
        void onItemClick(Booking booking);
    }

    public BookingsAdapter(OnBookingInteractionListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        BookingViewHolder holder = new BookingViewHolder(view);

        holder.btnCancel.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onCancelClick(bookings.get(position));
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(bookings.get(position));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        Context context = holder.itemView.getContext();

        Activity activity = booking.getActivityDetail();
        String title = (activity != null && activity.getTitle() != null) ? activity.getTitle() : "-";

        holder.tvActivityTitle.setText(title);
        holder.tvQuantity.setText("Participantes: " + booking.getQuantity());

        String date = booking.getDate() != null ? booking.getDate() : "-";
        holder.tvDate.setText("Fecha: " + date);

        String status = booking.getStatus();
        holder.tvStatus.setText(getStatusLabel(status, context));
        holder.tvStatus.setBackgroundResource(getStatusBackground(status));

        if (activity != null && activity.getImages() != null && !activity.getImages().isEmpty()) {
            Glide.with(context)
                    .load(activity.getImages().get(0).getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivActivityImage);
        } else {
            holder.ivActivityImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnCancel.setVisibility("CONFIRMED".equals(status) ? View.VISIBLE : View.GONE);
    }

    private String getStatusLabel(String status, Context context) {
        if ("CANCELED".equals(status)) return context.getString(R.string.status_canceled);
        if ("FINISHED".equals(status)) return context.getString(R.string.status_finished);
        return context.getString(R.string.status_confirmed);
    }

    private int getStatusBackground(String status) {
        if ("CANCELED".equals(status)) return R.drawable.bg_category_tag; // Or a specific red one
        if ("FINISHED".equals(status)) return R.drawable.bg_category_tag; // Or a specific green one
        return R.drawable.bg_category_tag;
    }

    @Override
    public int getItemCount() { return bookings.size(); }
}
