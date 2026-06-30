package com.example.xplorenow.payment;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.xplorenow.data.model.PaymentTransaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentStorage {

    private static final String PREFS_NAME = "payment_transactions";
    private static final String KEY_TRANSACTIONS = "transactions";

    private final SharedPreferences preferences;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<PaymentTransaction>>() {}.getType();

    public PaymentStorage(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void saveTransaction(PaymentTransaction transaction) {
        List<PaymentTransaction> transactions = getTransactionsInternal();
        transactions.add(0, transaction);
        persist(transactions);
    }

    public synchronized void updateTransaction(PaymentTransaction transaction) {
        List<PaymentTransaction> transactions = getTransactionsInternal();
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.set(i, transaction);
                persist(transactions);
                return;
            }
        }
        transactions.add(0, transaction);
        persist(transactions);
    }

    public synchronized List<PaymentTransaction> getTransactions() {
        return new ArrayList<>(getTransactionsInternal());
    }

    public synchronized PaymentTransaction getTransactionByBookingId(int bookingId) {
        for (PaymentTransaction transaction : getTransactionsInternal()) {
            if (transaction.getBookingId() != null && transaction.getBookingId() == bookingId) {
                return transaction;
            }
        }
        return null;
    }

    public synchronized PaymentTransaction getTransactionById(String transactionId) {
        for (PaymentTransaction transaction : getTransactionsInternal()) {
            if (transaction.getId().equals(transactionId)) {
                return transaction;
            }
        }
        return null;
    }

    private List<PaymentTransaction> getTransactionsInternal() {
        String json = preferences.getString(KEY_TRANSACTIONS, null);
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<PaymentTransaction> transactions = gson.fromJson(json, listType);
        if (transactions == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(transactions);
    }

    private void persist(List<PaymentTransaction> transactions) {
        preferences.edit().putString(KEY_TRANSACTIONS, gson.toJson(transactions)).apply();
    }
}
