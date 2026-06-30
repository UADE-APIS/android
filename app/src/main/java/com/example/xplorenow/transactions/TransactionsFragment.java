package com.example.xplorenow.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xplorenow.R;
import com.example.xplorenow.adapters.TransactionsAdapter;
import com.example.xplorenow.data.model.PaymentTransaction;
import com.example.xplorenow.payment.PaymentStorage;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionsFragment extends Fragment {

    @Inject
    PaymentStorage paymentStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvTransactions);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyTransactions);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        TransactionsAdapter adapter = new TransactionsAdapter(transaction -> {
            Bundle args = new Bundle();
            args.putString("transactionId", transaction.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_transactions_to_transactionDetail, args);
        });
        recyclerView.setAdapter(adapter);

        List<PaymentTransaction> transactions = paymentStorage.getTransactions();
        adapter.setTransactions(transactions);
        tvEmpty.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
