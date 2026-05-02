package com.example.xplorenow.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.xplorenow.data.model.Activity;
import com.example.xplorenow.data.model.ActivityImage;
import com.example.xplorenow.data.model.Booking;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "cached_bookings")
public class CachedBooking {
    @PrimaryKey
    @NonNull
    private String id;
    private String activityTitle;
    private String date;
    private String meetingPoint;
    private String status;
    private String activityImageUrl;
    /** Código de voucher de confirmación (req. 19) */
    private String voucherCode;
    /** Cantidad de participantes (req. 19) */
    private int quantity;
    /** ID de la actividad para navegar al detalle en modo offline (req. 18) */
    private int activityId;

    public CachedBooking(@NonNull String id, String activityTitle, String date,
                         String meetingPoint, String status, String activityImageUrl,
                         String voucherCode, int quantity, int activityId) {
        this.id = id;
        this.activityTitle = activityTitle;
        this.date = date;
        this.meetingPoint = meetingPoint;
        this.status = status;
        this.activityImageUrl = activityImageUrl;
        this.voucherCode = voucherCode;
        this.quantity = quantity;
        this.activityId = activityId;
    }

    public Booking toBooking() {
        Booking b = new Booking();
        try {
            b.setId(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            b.setId(0);
        }
        b.setActivityId(activityId);
        b.setQuantity(quantity);
        b.setStatus(status);
        b.setCreatedAt(date);

        Activity activity = new Activity();
        activity.setId(activityId);
        activity.setTitle(activityTitle);
        activity.setMeetingPoint(meetingPoint);

        if (activityImageUrl != null && !activityImageUrl.isEmpty()) {
            List<ActivityImage> images = new ArrayList<>();
            // CORRECCIÓN: usar el constructor de 1 parámetro definido en ActivityImage
            images.add(new ActivityImage(activityImageUrl));
            activity.setImages(images);
        }

        b.setActivityDetail(activity);
        return b;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMeetingPoint() { return meetingPoint; }
    public void setMeetingPoint(String meetingPoint) { this.meetingPoint = meetingPoint; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActivityImageUrl() { return activityImageUrl; }
    public void setActivityImageUrl(String activityImageUrl) { this.activityImageUrl = activityImageUrl; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }
}
