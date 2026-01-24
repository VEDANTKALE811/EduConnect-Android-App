package com.example.educonnect;

public class TimetableItem {
    private String title;
    private String url;
    private long timestamp;

    public TimetableItem() {} // Needed for Firestore

    public TimetableItem(String title, String url, long timestamp) {
        this.title = title;
        this.url = url;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public long getTimestamp() { return timestamp; }
}
