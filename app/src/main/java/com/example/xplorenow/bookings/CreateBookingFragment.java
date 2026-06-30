package com.example.xplorenow.bookings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.xplorenow.R;
import com.example.xplorenow.data.local.CachedBooking;
import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.ActivityAvailability;
import com.example.xplorenow.data.model.ApiResponse;
import com.example.xplorenow.data.model.Booking;
import com.example.xplorenow.data.model.BookingRequest;
import com.example.xplorenow.data.model.PaymentCard;
import com.example.xplorenow.data.model.PaymentRequest;
import com.example.xplorenow.data.model.PaymentTransaction;
import com.example.xplorenow.data.network.ApiService;
import com.example.xplorenow.payment.MockPaymentService;
import com.example.xplorenow.payment.PaymentStorage;
import com.example.xplorenow.payment.PaymentUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CreateBookingFragment extends Fragment {

    private static final String TAG = "CreateBookingFragment";

    @Inject ApiService apiService;
    @Inject com.example.xplorenow.data.local.CachedBookingDao cachedBookingDao;
    @Inject MockPaymentService mockPaymentService;
    @Inject PaymentStorage paymentStorage;

    private final List<ActivityAvailability> availabilities = new ArrayList<>();
    private Activity currentActivity;
    private PaymentTransaction pendingApprovedTransaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvActivityTitle = view.findViewById(R.id.tvActivityTitle);
        Spinner spDate = view.findViewById(R.id.spDate);
        TextView tvAvailableSlots = view.findViewById(R.id.tvAvailableSlots);
        TextView tvPriceType = view.findViewById(R.id.tvPriceType);
        TextView tvTotal = view.findViewById(R.id.tvTotal);
        LinearLayout layoutPaymentForm = view.findViewById(R.id.layoutPaymentForm);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etCardHolder = view.findViewById(R.id.etCardHolder);
        EditText etCardNumber = view.findViewById(R.id.etCardNumber);
        EditText etCardExpiry = view.findViewById(R.id.etCardExpiry);
        EditText etCardCvv = view.findViewById(R.id.etCardCvv);
        Button btnBook = view.findViewById(R.id.btnBook);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tvError);

        int activityId = getArguments() != null ? getArguments().getInt("activityId", -1) : -1;
        if (activityId == -1) {
            showError(tvError, getString(R.string.error_invalid_activity));
            btnBook.setEnabled(false);
            return;
        }

        fetchActivityDetails(activityId, tvActivityTitle, spDate, tvAvailableSlots, tvPriceType, tvTotal,
                layoutPaymentForm, progressBar, tvError, btnBook);

        spDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                if (!availabilities.isEmpty()) {
                    int slots = availabilities.get(position).getAvailableSlots();
                    tvAvailableSlots.setText(getString(R.string.text_available_slots, slots));
                } else if (currentActivity != null) {
                    tvAvailableSlots.setText(getString(R.string.text_available_slots, currentActivity.getAvailableSlots()));
                }
                updateTotal(tvTotal, etQuantity.getText().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        etQuantity.setText("1");

        btnBook.setOnClickListener(v -> {
            tvError.setVisibility(View.GONE);

            String quantityStr = etQuantity.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                showError(tvError, getString(R.string.error_fill_quantity));
                return;
            }

            int requestedQuantity;
            try {
                requestedQuantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                showError(tvError, getString(R.string.error_invalid_quantity));
                return;
            }

            if (requestedQuantity <= 0) {
                showError(tvError, getString(R.string.error_invalid_quantity));
                return;
            }

            int availableSlots;
            Integer availabilityId = null;
            if (!availabilities.isEmpty()) {
                ActivityAvailability selectedAvailability = availabilities.get(spDate.getSelectedItemPosition());
                availableSlots = selectedAvailability.getAvailableSlots();
                availabilityId = selectedAvailability.getId();
            } else {
                availableSlots = currentActivity != null ? currentActivity.getAvailableSlots() : 0;
            }

            if (requestedQuantity > availableSlots) {
                showError(tvError, getString(R.string.error_insufficient_slots));
                return;
            }

            String selectedDate = getSelectedDate(spDate);
            double totalAmount = currentActivity != null ? currentActivity.getPriceValue() * requestedQuantity : 0d;
            BookingRequest bookingRequest = new BookingRequest(activityId, availabilityId, requestedQuantity);

            if (currentActivity != null && !currentActivity.isFree()) {
                PaymentCard card = validateCardFields(etCardHolder, etCardNumber, etCardExpiry, etCardCvv, tvError);
                if (card == null) {
                    return;
                }

                showPaymentSummary(currentActivity.getTitle(), selectedDate, requestedQuantity, totalAmount, () -> {
                    showInfo(tvError, getString(R.string.payment_processing));
                    processPaymentAndBooking(view, bookingRequest, progressBar, tvError, card,
                            selectedDate, requestedQuantity, totalAmount, activityId);
                });
            } else {
                showPaymentSummary(currentActivity != null ? currentActivity.getTitle() : "",
                        selectedDate, requestedQuantity, totalAmount,
                        () -> executeBooking(view, bookingRequest, progressBar, tvError));
            }
        });
    }

    private void fetchActivityDetails(int activityId, TextView tvTitle, Spinner spDate, TextView tvSlots,
                                      TextView tvPriceType, TextView tvTotal, LinearLayout layoutPaymentForm,
                                      ProgressBar pb, TextView err, Button btnBook) {
        pb.setVisibility(View.VISIBLE);
        btnBook.setEnabled(false);

        apiService.getActivity(activityId).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Activity>> call, @NonNull Response<ApiResponse<Activity>> response) {
                pb.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentActivity = response.body().getData();
                    tvTitle.setText(currentActivity.getTitle());

                    if (currentActivity.isFree()) {
                        tvPriceType.setText(getString(R.string.price_free));
                        layoutPaymentForm.setVisibility(View.GONE);
                    } else {
                        tvPriceType.setText(getString(R.string.price_paid,
                                PaymentUtils.formatAmount(currentActivity.getPriceValue())));
                        layoutPaymentForm.setVisibility(View.VISIBLE);
                    }

                    if (currentActivity.getAvailabilities() != null && !currentActivity.getAvailabilities().isEmpty()) {
                        availabilities.clear();
                        availabilities.addAll(currentActivity.getAvailabilities());
                        List<String> dateStrings = new ArrayList<>();
                        for (ActivityAvailability availability : availabilities) {
                            dateStrings.add(availability.getDate());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, dateStrings);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spDate.setAdapter(adapter);
                    } else {
                        spDate.setVisibility(View.GONE);
                        tvSlots.setText(getString(R.string.text_available_slots, currentActivity.getAvailableSlots()));
                    }

                    updateTotal(tvTotal, "1");
                    btnBook.setEnabled(true);
                } else {
                    showError(err, getString(R.string.error_loading_data));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Activity>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                showError(err, getString(R.string.error_connection));
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void processPaymentAndBooking(View view, BookingRequest bookingRequest, ProgressBar progressBar,
                                          TextView tvError, PaymentCard card, String selectedDate,
                                          int requestedQuantity, double totalAmount, int activityId) {
        PaymentRequest paymentRequest = new PaymentRequest(
                currentActivity.getTitle(),
                selectedDate,
                requestedQuantity,
                totalAmount,
                card
        );

        mockPaymentService.processPayment(paymentRequest, result -> {
            if (!isAdded()) return;

            if (result.isApproved()) {
                pendingApprovedTransaction = new PaymentTransaction(
                        UUID.randomUUID().toString(),
                        null,
                        activityId,
                        currentActivity.getTitle(),
                        selectedDate,
                        requestedQuantity,
                        totalAmount,
                        LocalDateTime.now().toString(),
                        result.getMaskedCard(),
                        result.getStatus(),
                        null
                );
                showInfo(tvError, getString(R.string.payment_approved));
                executeBooking(view, bookingRequest, progressBar, tvError);
            } else {
                paymentStorage.saveTransaction(new PaymentTransaction(
                        UUID.randomUUID().toString(),
                        null,
                        activityId,
                        currentActivity.getTitle(),
                        selectedDate,
                        requestedQuantity,
                        totalAmount,
                        LocalDateTime.now().toString(),
                        result.getMaskedCard(),
                        result.getStatus(),
                        result.getMessage()
                ));
                showError(tvError, getString(R.string.payment_rejected, result.getMessage()));
            }
        });
    }

    private void executeBooking(View view, BookingRequest request, ProgressBar pb, TextView err) {
        pb.setVisibility(View.VISIBLE);
        err.setVisibility(View.GONE);

        apiService.createBooking(request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call, @NonNull Response<ApiResponse<Booking>> response) {
                pb.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    showInfo(err, getString(R.string.msg_booking_success));

                    Booking createdBooking = response.body().getData();
                    if (createdBooking != null) {
                        if (pendingApprovedTransaction != null) {
                            pendingApprovedTransaction.setBookingId(createdBooking.getId());
                            paymentStorage.saveTransaction(pendingApprovedTransaction);
                            pendingApprovedTransaction = null;
                        }

                        new Thread(() -> {
                            String imgUrl = "";
                            if (createdBooking.getActivityDetail() != null &&
                                    createdBooking.getActivityDetail().getImages() != null &&
                                    !createdBooking.getActivityDetail().getImages().isEmpty()) {
                                imgUrl = createdBooking.getActivityDetail().getImages().get(0).getImageUrl();
                            }

                            CachedBooking cb = new CachedBooking(
                                    String.valueOf(createdBooking.getId()),
                                    createdBooking.getActivityDetail() != null ? createdBooking.getActivityDetail().getTitle() : "",
                                    createdBooking.getDate(),
                                    createdBooking.getActivityDetail() != null ? createdBooking.getActivityDetail().getMeetingPoint() : "",
                                    createdBooking.getStatus() != null ? createdBooking.getStatus() : "CONFIRMED",
                                    imgUrl,
                                    "VOUCHER-" + createdBooking.getId(),
                                    createdBooking.getQuantity(),
                                    createdBooking.getActivityId()
                            );
                            List<CachedBooking> list = new ArrayList<>();
                            list.add(cb);
                            cachedBookingDao.insertBookings(list);
                        }).start();
                    }

                    view.postDelayed(() -> Navigation.findNavController(view).popBackStack(), 1500);
                } else {
                    pendingApprovedTransaction = null;
                    showError(err, getString(R.string.error_booking_failed) + " " + response.code());
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                pb.setVisibility(View.GONE);
                pendingApprovedTransaction = null;
                showError(err, getString(R.string.error_connection));
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void updateTotal(TextView tvTotal, String quantityText) {
        int quantity = 1;
        try {
            if (quantityText != null && !quantityText.trim().isEmpty()) {
                quantity = Integer.parseInt(quantityText.trim());
            }
        } catch (Exception ignored) {
        }

        double total = currentActivity != null ? currentActivity.getPriceValue() * Math.max(quantity, 1) : 0d;
        tvTotal.setText(getString(R.string.price_total, PaymentUtils.formatAmount(total)));
    }

    private String getSelectedDate(Spinner spDate) {
        if (spDate.getVisibility() == View.GONE) {
            return "-";
        }
        return spDate.getSelectedItem() != null ? spDate.getSelectedItem().toString() : "-";
    }

    private PaymentCard validateCardFields(EditText etCardHolder, EditText etCardNumber, EditText etCardExpiry,
                                           EditText etCardCvv, TextView tvError) {
        String holder = etCardHolder.getText().toString().trim();
        String number = PaymentUtils.normalizeCardNumber(etCardNumber.getText().toString());
        String expiry = etCardExpiry.getText().toString().trim();
        String cvv = etCardCvv.getText().toString().trim();

        if (holder.isEmpty()) {
            showError(tvError, getString(R.string.payment_invalid_holder));
            return null;
        }
        if (!number.matches("\\d{13,19}")) {
            showError(tvError, getString(R.string.payment_invalid_number));
            return null;
        }
        if (!PaymentUtils.isValidExpiryDate(expiry)) {
            showError(tvError, getString(R.string.payment_invalid_expiry));
            return null;
        }
        if (!cvv.matches("\\d{3,4}")) {
            showError(tvError, getString(R.string.payment_invalid_cvv));
            return null;
        }

        return new PaymentCard(holder, number, expiry, cvv);
    }

    private void showPaymentSummary(String title, String selectedDate, int quantity, double totalAmount,
                                    Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.payment_summary_title)
                .setMessage(getString(R.string.payment_summary_message, title, selectedDate,
                        quantity, PaymentUtils.formatAmount(totalAmount)))
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> onConfirm.run())
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    private void showError(TextView tvError, String message) {
        tvError.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void showInfo(TextView tvError, String message) {
        tvError.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
