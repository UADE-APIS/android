package com.example.xplorenow.notifications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.xplorenow.data.model.Booking;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

public final class BookingReminderScheduler {

    private static final String TAG = "BookingReminderScheduler";
    private static final String WORK_PREFIX = "booking-reminder-";

    private BookingReminderScheduler() {}

    public static void schedule(Context context, Booking booking) {
        schedule(context, booking, null, null);
    }

    public static void schedule(Context context, Booking booking,
                                @Nullable String fallbackActivityDate,
                                @Nullable String fallbackActivityTitle) {
        if (booking == null) {
            return;
        }

        String rawActivityDate = fallbackActivityDate != null && !fallbackActivityDate.trim().isEmpty()
                ? fallbackActivityDate
                : booking.getDate();
        ZonedDateTime activityDate = parseActivityDate(rawActivityDate);
        if (activityDate == null) {
            Log.w(TAG, "No se pudo programar recordatorio: fecha invalida para reserva " + booking.getId());
            return;
        }

        String activityTitle = fallbackActivityTitle != null ? fallbackActivityTitle : "";
        if (booking.getActivityDetail() != null && booking.getActivityDetail().getTitle() != null) {
            activityTitle = booking.getActivityDetail().getTitle();
        }

        ZonedDateTime notificationTime = isDebuggable(context)
                ? ZonedDateTime.now().plusMinutes(1)
                : activityDate.minusHours(24);

        long delayMillis = Duration.between(ZonedDateTime.now(), notificationTime).toMillis();

        Data data = new Data.Builder()
                .putInt(BookingReminderWorker.KEY_BOOKING_ID, booking.getId())
                .putString(BookingReminderWorker.KEY_ACTIVITY_TITLE, activityTitle)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(BookingReminderWorker.class)
                .setInitialDelay(Math.max(delayMillis, 0), TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(WORK_PREFIX + booking.getId(), ExistingWorkPolicy.REPLACE, request);
    }

    private static boolean isDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    @Nullable
    private static ZonedDateTime parseActivityDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }

        String value = rawDate.trim();
        ZoneId zone = ZoneId.systemDefault();

        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(zone);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(value).atZone(zone);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(value).atStartOfDay(zone);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
