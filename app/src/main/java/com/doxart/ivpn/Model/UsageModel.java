package com.doxart.ivpn.Model;

public class UsageModel {
    int id;
    String date;
    int usage;
    long dateTime;

    public UsageModel() {}

    public UsageModel(int id, String date, long dateTime, int usage) {
        this.id = id;
        this.date = date;
        this.dateTime = dateTime;
        this.usage = usage;
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

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }
}
