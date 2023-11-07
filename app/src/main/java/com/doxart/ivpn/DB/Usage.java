package com.doxart.ivpn.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usage")
public class Usage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "date_time")
    private long dateTime;

    @ColumnInfo(name = "usage_in_minutes")
    private int usageInMinutes;

    public Usage(String date, long dateTime, int usageInMinutes) {
        this.date = date;
        this.dateTime = dateTime;
        this.usageInMinutes = usageInMinutes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getUsageInMinutes() {
        return usageInMinutes;
    }

    public void setUsageInMinutes(int usageInMinutes) {
        this.usageInMinutes = usageInMinutes;
    }
}
