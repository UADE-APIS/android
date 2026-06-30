package com.example.xplorenow.bookings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.model.PaymentTransaction;
import com.example.xplorenow.payment.PaymentStorage;
import com.example.xplorenow.payment.PaymentUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BookingDetailFragment extends Fragment {

    @Inject
    PaymentStorage paymentStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Booking booking = (Booking) (getArguments() != null ? getArguments().getSerializable("booking") : null);
        if (booking == null) {
            return;
        }

        ((TextView) view.findViewById(R.id.tvBookingDetailTitle)).setText(
                booking.getActivityDetail() != null ? booking.getActivityDetail().getTitle() : "-");
        ((TextView) view.findViewById(R.id.tvBookingDetailStatus)).setText(booking.getStatus());
        ((TextView) view.findViewById(R.id.tvBookingDetailDate)).setText(
                getString(R.string.booking_detail_date, booking.getDate()));
        ((TextView) view.findViewById(R.id.tvBookingDetailQuantity)).setText(
                getString(R.string.booking_detail_quantity, booking.getQuantity()));

        TextView tvPrice = view.findViewById(R.id.tvBookingDetailPrice);
        Button btnTransaction = view.findViewById(R.id.btnBookingTransaction);

        if (booking.getActivityDetail() != null && !booking.getActivityDetail().isFree()) {
            double total = booking.getActivityDetail().getPriceValue() * booking.getQuantity();
            tvPrice.setText(getString(R.string.booking_detail_price, "$" + PaymentUtils.formatAmount(total)));

            PaymentTransaction transaction = paymentStorage.getTransactionByBookingId(booking.getId());
            if (transaction != null) {
                btnTransaction.setVisibility(View.VISIBLE);
                btnTransaction.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("transactionId", transaction.getId());
                    args.putSerializable("booking", booking);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_bookingDetail_to_transactionDetail, args);
                });
            }
        } else {
            tvPrice.setText(getString(R.string.booking_detail_price, getString(R.string.price_free)));
        }
    }
}
