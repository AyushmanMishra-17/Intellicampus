package com.ayushman.intellicampus.constants;

public final class FirestoreConstants {

    private FirestoreConstants() {}

    // Root collections
    public static final String USERS = "users";
    public static final String CURRICULUM = "curriculum";
    public static final String ACADEMIC_BATCHES = "academic_batches";

    // Academic batch fields
    public static final String BATCH = "batch";
    public static final String PROGRAMME = "programme";
    public static final String CURRENT_SEMESTER = "currentSemester";
    public static final String CURRENT_ACADEMIC_YEAR = "currentAcademicYear";
    public static final String ACTIVE = "active";

    // Curriculum fields
    public static final String COURSE = "course";
    public static final String SEMESTER = "semester";
    public static final String ACADEMIC_SESSION = "academicSession";
    public static final String CODE = "code";
    public static final String CREDITS = "credits";

    // Common collections
    public static final String ASSIGNMENTS = "assignments";
    public static final String TIMETABLE = "timetable";
    public static final String NOTES = "notes";
    public static final String ATTENDANCE = "attendance";
    public static final String NOTICES = "notices";

    // Common fields
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String SUBJECT_ID = "subjectId";
    public static final String DUE_DATE_TIME = "dueDateTime";
    public static final String PRIORITY = "priority";
    public static final String STATUS = "status";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
}
