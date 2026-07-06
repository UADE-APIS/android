package com.example.xplorenow.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.xplorenow.MainActivity;
import com.example.xplorenow.R;

public final class NotificationHelper {

    public static final String CHANNEL_BOOKING_REMINDERS = "booking_reminders";
    public static final String CHANNEL_ACTIVITY_UPDATES = "activity_updates";

    private NotificationHelper() {}

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel reminders = new NotificationChannel(
                CHANNEL_BOOKING_REMINDERS,
                context.getString(R.string.notification_channel_booking_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        reminders.setDescription(context.getString(R.string.notification_channel_booking_reminders_desc));

        NotificationChannel updates = new NotificationChannel(
                CHANNEL_ACTIVITY_UPDATES,
                context.getString(R.string.notification_channel_activity_updates),
                NotificationManager.IMPORTANCE_HIGH
        );
        updates.setDescription(context.getString(R.string.notification_channel_activity_updates_desc));

        manager.createNotificationChannel(reminders);
        manager.createNotificationChannel(updates);
    }

    public static void showBookingReminder(Context context, int bookingId, String activityTitle) {
        ensureChannels(context);

        Bundle args = new Bundle();
        args.putInt("bookingId", bookingId);

        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.myBookingsFragment)
                .setArguments(args)
                .createPendingIntent();

        String title = context.getString(R.string.notification_booking_reminder_title);
        String text = context.getString(R.string.notification_booking_reminder_body, activityTitle);
        showNotification(context, CHANNEL_BOOKING_REMINDERS, bookingId, title, text, pendingIntent);
    }

    public static void showActivityUpdate(Context context, int notificationId, String title, String text, int bookingId) {
        ensureChannels(context);

        Bundle args = new Bundle();
        args.putInt("bookingId", bookingId);

        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.myBookingsFragment)
                .setArguments(args)
                .createPendingIntent();

        showNotification(context, CHANNEL_ACTIVITY_UPDATES, notificationId, title, text, pendingIntent);
    }

    private static void showNotification(Context context,
                                         String channelId,
                                         int notificationId,
                                         String title,
                                         String text,
                                         PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
