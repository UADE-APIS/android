package com.example.xplorenow.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

    public CachedBooking(@NonNull String id, String activityTitle, String date,
                         String meetingPoint, String status, String activityImageUrl,
                         String voucherCode, int quantity) {
        this.id = id;
        this.activityTitle = activityTitle;
        this.date = date;
        this.meetingPoint = meetingPoint;
        this.status = status;
        this.activityImageUrl = activityImageUrl;
        this.voucherCode = voucherCode;
        this.quantity = quantity;
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
}
