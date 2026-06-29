package com.example.xplorenow.bookings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.xplorenow.R;

public class VoucherFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voucher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        int bookingId     = args.getInt("bookingId", -1);
        String title      = args.getString("activityTitle", "-");
        String date       = args.getString("date", "-");
        String meeting    = args.getString("meetingPoint", "-");
        String guide      = args.getString("guideName", "-");
        int quantity      = args.getInt("quantity", 1);

        TextView tvBookingNumber = view.findViewById(R.id.tvBookingNumber);
        TextView tvActivityTitle = view.findViewById(R.id.tvActivityTitle);
        TextView tvDate          = view.findViewById(R.id.tvDate);
        TextView tvMeetingPoint  = view.findViewById(R.id.tvMeetingPoint);
        TextView tvGuide         = view.findViewById(R.id.tvGuide);
        TextView tvParticipants  = view.findViewById(R.id.tvParticipants);
        TextView tvVoucherCode   = view.findViewById(R.id.tvVoucherCode);

        if (bookingId > 0) {
            tvBookingNumber.setText(getString(R.string.voucher_booking_number, bookingId));
        }
        tvActivityTitle.setText(title);
        tvDate.setText(date.isEmpty() ? "-" : date);
        tvMeetingPoint.setText(meeting.isEmpty() ? "-" : meeting);
        tvGuide.setText(guide.isEmpty() ? "-" : guide);
        tvParticipants.setText(getString(R.string.voucher_participants_value, quantity));
        tvVoucherCode.setText("VOUCHER-" + bookingId);
    }
}
