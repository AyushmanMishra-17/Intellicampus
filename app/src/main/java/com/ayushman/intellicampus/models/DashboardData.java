package com.ayushman.intellicampus.models;

public class DashboardData {

    private int assignmentCount;
    private int noticeCount;
    private int todayClassCount;

    private Assignment upcomingAssignment;
    private Notice latestNotice;
    private Timetable nextClass;

    public DashboardData() {
    }

    //==========================
    // Assignment Count
    //==========================

    public int getAssignmentCount() {
        return assignmentCount;
    }

    public void setAssignmentCount(int assignmentCount) {
        this.assignmentCount = assignmentCount;
    }

    //==========================
    // Notice Count
    //==========================

    public int getNoticeCount() {
        return noticeCount;
    }

    public void setNoticeCount(int noticeCount) {
        this.noticeCount = noticeCount;
    }

    //==========================
    // Today's Class Count
    //==========================

    public int getTodayClassCount() {
        return todayClassCount;
    }

    public void setTodayClassCount(int todayClassCount) {
        this.todayClassCount = todayClassCount;
    }

    //==========================
    // Upcoming Assignment
    //==========================

    public Assignment getUpcomingAssignment() {
        return upcomingAssignment;
    }

    public void setUpcomingAssignment(Assignment upcomingAssignment) {
        this.upcomingAssignment = upcomingAssignment;
    }

    //==========================
    // Latest Notice
    //==========================

    public Notice getLatestNotice() {
        return latestNotice;
    }

    public void setLatestNotice(Notice latestNotice) {
        this.latestNotice = latestNotice;
    }

    //==========================
    // Next Class
    //==========================

    public Timetable getNextClass() {
        return nextClass;
    }

    public void setNextClass(Timetable nextClass) {
        this.nextClass = nextClass;
    }
}