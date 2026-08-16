package com.ayushman.intellicampus.models;

public class AcademicBatch {

    private String batch;
    private String programme;
    private int currentSemester;
    private String currentAcademicYear;
    private boolean active;

    public AcademicBatch() {
        // Required by Firestore
    }

    public AcademicBatch(
            String batch,
            String programme,
            int currentSemester,
            String currentAcademicYear,
            boolean active
    ) {
        this.batch = batch;
        this.programme = programme;
        this.currentSemester = currentSemester;
        this.currentAcademicYear = currentAcademicYear;
        this.active = active;
    }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    public int getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    public String getCurrentAcademicYear() { return currentAcademicYear; }
    public void setCurrentAcademicYear(String currentAcademicYear) {
        this.currentAcademicYear = currentAcademicYear;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
