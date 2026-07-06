package com.example.xplorenow.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BookingReminderWorker extends Worker {

    public static final String KEY_BOOKING_ID = "booking_id";
    public static final String KEY_ACTIVITY_TITLE = "activity_title";

    public BookingReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int bookingId = getInputData().getInt(KEY_BOOKING_ID, 0);
        String activityTitle = getInputData().getString(KEY_ACTIVITY_TITLE);
        if (activityTitle == null || activityTitle.trim().isEmpty()) {
            activityTitle = getApplicationContext().getString(com.example.xplorenow.R.string.notification_default_activity);
        }

        NotificationHelper.showBookingReminder(getApplicationContext(), bookingId, activityTitle);
        return Result.success();
    }
}
