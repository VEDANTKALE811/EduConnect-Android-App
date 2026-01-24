package com.example.educonnect;

public class Reminder {
    private String id;
    private String title;
    private String date;
    private String time;
    private String userId;

    public Reminder() {}

    public Reminder(String id, String title, String date, String time, String userId) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getUserId() { return userId; }
}
