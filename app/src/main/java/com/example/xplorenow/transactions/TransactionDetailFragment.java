package com.example.xplorenow.transactions;

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
public class TransactionDetailFragment extends Fragment {

    @Inject
    PaymentStorage paymentStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String transactionId = getArguments() != null ? getArguments().getString("transactionId") : null;
        PaymentTransaction transaction = transactionId == null ? null : paymentStorage.getTransactionById(transactionId);
        if (transaction == null) {
            return;
        }

        ((TextView) view.findViewById(R.id.tvTransactionDetailTitle)).setText(transaction.getActivityTitle());
        ((TextView) view.findViewById(R.id.tvTransactionDetailStatus)).setText(getString(
                R.string.transaction_status,
                transaction.isApproved() ? getString(R.string.transaction_status_approved) : getString(R.string.transaction_status_rejected)
        ));
        ((TextView) view.findViewById(R.id.tvTransactionDetailAmount)).setText(getString(
                R.string.transaction_amount, PaymentUtils.formatAmount(transaction.getAmount())));
        ((TextView) view.findViewById(R.id.tvTransactionDetailDate)).setText(getString(
                R.string.transaction_date, transaction.getCreatedAt()));
        ((TextView) view.findViewById(R.id.tvTransactionDetailCard)).setText(getString(
                R.string.transaction_card, transaction.getMaskedCard()));

        TextView tvBooking = view.findViewById(R.id.tvTransactionDetailBooking);
        Button btnGoToBooking = view.findViewById(R.id.btnGoToBooking);
        if (transaction.getBookingId() != null) {
            tvBooking.setText(getString(R.string.transaction_booking, "#" + transaction.getBookingId()));
            btnGoToBooking.setVisibility(View.VISIBLE);
            btnGoToBooking.setOnClickListener(v -> {
                Booking booking = (Booking) (getArguments() != null ? getArguments().getSerializable("booking") : null);
                if (booking != null) {
                    Bundle args = new Bundle();
                    args.putSerializable("booking", booking);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_transactionDetail_to_bookingDetail, args);
                } else {
                    Navigation.findNavController(view).popBackStack();
                }
            });
        } else {
            tvBooking.setText(getString(R.string.transaction_booking_missing));
            btnGoToBooking.setVisibility(View.GONE);
        }

        TextView tvReason = view.findViewById(R.id.tvTransactionDetailReason);
        if (transaction.getRejectionReason() != null && !transaction.getRejectionReason().trim().isEmpty()) {
            tvReason.setVisibility(View.VISIBLE);
            tvReason.setText(getString(R.string.transaction_reason, transaction.getRejectionReason()));
        } else {
            tvReason.setVisibility(View.GONE);
        }
    }
}
