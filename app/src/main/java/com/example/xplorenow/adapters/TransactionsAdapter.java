package com.example.xplorenow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.data.model.PaymentTransaction;
import com.example.xplorenow.payment.PaymentUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(PaymentTransaction transaction);
    }

    private final List<PaymentTransaction> transactions = new ArrayList<>();
    private final OnTransactionClickListener listener;

    public TransactionsAdapter(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<PaymentTransaction> newTransactions) {
        transactions.clear();
        if (newTransactions != null) {
            transactions.addAll(newTransactions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        PaymentTransaction transaction = transactions.get(position);
        holder.tvActivity.setText(transaction.getActivityTitle());
        holder.tvAmount.setText(holder.itemView.getContext().getString(
                R.string.transaction_amount, PaymentUtils.formatAmount(transaction.getAmount())));
        holder.tvDate.setText(holder.itemView.getContext().getString(
                R.string.transaction_date, transaction.getCreatedAt()));
        holder.tvCard.setText(holder.itemView.getContext().getString(
                R.string.transaction_card, transaction.getMaskedCard()));
        holder.tvStatus.setText(holder.itemView.getContext().getString(
                R.string.transaction_status, getStatusLabel(holder, transaction)));

        holder.itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
    }

    private String getStatusLabel(TransactionViewHolder holder, PaymentTransaction transaction) {
        return transaction.isApproved()
                ? holder.itemView.getContext().getString(R.string.transaction_status_approved)
                : holder.itemView.getContext().getString(R.string.transaction_status_rejected);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvActivity;
        final TextView tvAmount;
        final TextView tvDate;
        final TextView tvCard;
        final TextView tvStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivity = itemView.findViewById(R.id.tvTransactionActivity);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvCard = itemView.findViewById(R.id.tvTransactionCard);
            tvStatus = itemView.findViewById(R.id.tvTransactionStatus);
        }
    }
}
