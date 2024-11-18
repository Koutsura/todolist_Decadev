package com.example.myapplication;

public class Task {
    private int id;
    private String title;
    private String description;
    private long reminderTime;

    // Konstruktor tanpa ID (digunakan saat membuat tugas baru)
    public Task(String title, String description, long reminderTime) {
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
    }

    // Konstruktor dengan ID (digunakan saat mengambil data tugas dari database atau API)
    public Task(int id, String title, String description, long reminderTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getReminderTime() { return reminderTime; }

    public void setId(int id) { this.id = id; }
}
