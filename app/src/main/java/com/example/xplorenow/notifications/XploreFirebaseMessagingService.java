package com.example.xplorenow.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.xplorenow.R;
import com.example.xplorenow.di.FcmTokenRegistrarAccessor;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class XploreFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "XploreFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");
        if (body == null) {
            body = data.get("message");
        }

        if (message.getNotification() != null) {
            if (title == null) {
                title = message.getNotification().getTitle();
            }
            if (body == null) {
                body = message.getNotification().getBody();
            }
        }

        String activityName = data.get("activity_name");
        if (activityName == null) {
            activityName = data.get("activityName");
        }
        String type = data.get("type");
        if (title == null || title.trim().isEmpty()) {
            title = getString(R.string.notification_activity_update_title);
        }
        if (body == null || body.trim().isEmpty()) {
            body = buildFallbackBody(type, activityName);
        }

        int bookingId = parseBookingId(data.get("booking_id"));
        int notificationId = bookingId > 0 ? bookingId : (int) System.currentTimeMillis();
        NotificationHelper.showActivityUpdate(this, notificationId, title, body, bookingId);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);
        FcmTokenRegistrarAccessor.from(this).registerToken(token);
    }

    private String buildFallbackBody(String type, String activityName) {
        String name = activityName != null && !activityName.trim().isEmpty()
                ? activityName
                : getString(R.string.notification_default_activity);

        if ("cancelled".equalsIgnoreCase(type) || "canceled".equalsIgnoreCase(type) ||
                "cancelada".equalsIgnoreCase(type)) {
            return getString(R.string.notification_activity_cancelled_body, name);
        }
        if ("rescheduled".equalsIgnoreCase(type) || "reprogramada".equalsIgnoreCase(type)) {
            return getString(R.string.notification_activity_rescheduled_body, name);
        }
        return getString(R.string.notification_activity_update_body, name);
    }

    private int parseBookingId(String rawBookingId) {
        if (rawBookingId == null) {
            return -1;
        }
        try {
            return Integer.parseInt(rawBookingId);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
