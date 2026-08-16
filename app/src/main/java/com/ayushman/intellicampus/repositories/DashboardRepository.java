package com.ayushman.intellicampus.repositories;

import androidx.annotation.NonNull;

import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.models.DashboardData;
import com.ayushman.intellicampus.models.Notice;
import com.ayushman.intellicampus.models.Timetable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardRepository {

    private final AssignmentRepository assignmentRepository;
    private final NoticeRepository noticeRepository;
    private final TimetableRepository timetableRepository;

    public DashboardRepository() {
        assignmentRepository = new AssignmentRepository();
        noticeRepository = new NoticeRepository();
        timetableRepository = new TimetableRepository();
    }

    @NonNull
    public DashboardData buildDashboardData(
            @NonNull List<Assignment> assignments,
            @NonNull List<Notice> notices,
            @NonNull List<Timetable> timetableList) {

        DashboardData dashboard = new DashboardData();

        // ------------------------
        // Assignment
        // ------------------------

        dashboard.setAssignmentCount(
                assignmentRepository.getAssignmentCount(assignments));

        dashboard.setUpcomingAssignment(
                assignmentRepository.getUpcomingAssignment(assignments));

        // ------------------------
        // Notice
        // ------------------------

        dashboard.setNoticeCount(
                noticeRepository.getNoticeCount(notices));

        dashboard.setLatestNotice(
                noticeRepository.getLatestNotice(notices));

        // ------------------------
        // Timetable
        // ------------------------

        String today =
                new SimpleDateFormat("EEEE", Locale.getDefault())
                        .format(new Date());

        String currentTime =
                new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date());

        dashboard.setTodayClassCount(
                timetableRepository.getTodayClassCount(
                        timetableList,
                        today));

        dashboard.setNextClass(
                timetableRepository.getNextClass(
                        timetableList,
                        today,
                        currentTime));

        return dashboard;
    }
}