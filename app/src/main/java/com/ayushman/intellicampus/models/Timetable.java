package com.ayushman.intellicampus.models;

public class Timetable {

    private String id;
    private String subject;
    private String faculty;
    private String room;
    private String day;
    private String startTime;
    private String endTime;

    // Required empty constructor for Firestore
    public Timetable() {
    }

    public Timetable(String id,
                     String subject,
                     String faculty,
                     String room,
                     String day,
                     String startTime,
                     String endTime) {
        this.id = id;
        this.subject = subject;
        this.faculty = faculty;
        this.room = room;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}