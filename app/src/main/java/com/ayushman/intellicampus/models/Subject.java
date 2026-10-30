package com.ayushman.intellicampus.models;

import java.io.Serializable;
import java.util.List;

public class Subject implements Serializable {

    private String id;
    private String programme;
    private String academicSession;

    private int semester;

    private String courseCode;
    private String courseName;
    private String courseType;

    private String learningObjectives;
    private String prerequisites;

    private List<Unit> units;

    private List<Practical> corePracticals;
    private List<Practical> applicationPracticals;

    private String textbooks;
    private String referenceBooks;

    private String sourceFile;

    public Subject() {
        // Required for Firestore
    }

    // ========================================================
    // BASIC INFORMATION
    // ========================================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getAcademicSession() {
        return academicSession;
    }

    public void setAcademicSession(String academicSession) {
        this.academicSession = academicSession;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    // ========================================================
    // SYLLABUS INFORMATION
    // ========================================================

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    // ========================================================
    // THEORY UNITS
    // ========================================================

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    // ========================================================
    // PRACTICALS
    // ========================================================

    public List<Practical> getCorePracticals() {
        return corePracticals;
    }

    public void setCorePracticals(
            List<Practical> corePracticals
    ) {
        this.corePracticals = corePracticals;
    }

    public List<Practical> getApplicationPracticals() {
        return applicationPracticals;
    }

    public void setApplicationPracticals(
            List<Practical> applicationPracticals
    ) {
        this.applicationPracticals = applicationPracticals;
    }

    // ========================================================
    // BOOKS
    // ========================================================

    public String getTextbooks() {
        return textbooks;
    }

    public void setTextbooks(String textbooks) {
        this.textbooks = textbooks;
    }

    public String getReferenceBooks() {
        return referenceBooks;
    }

    public void setReferenceBooks(String referenceBooks) {
        this.referenceBooks = referenceBooks;
    }

    // ========================================================
    // SOURCE
    // ========================================================

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    // ========================================================
    // COMPATIBILITY METHODS
    // ========================================================

    /*
     * These methods are kept because some existing parts
     * of Intellicampus may still use the older naming.
     */

    public String getName() {
        return courseName;
    }

    public void setName(String name) {
        this.courseName = name;
    }

    public String getCode() {
        return courseCode;
    }

    public void setCode(String code) {
        this.courseCode = code;
    }

    public int getCredits() {
        return 0;
    }

    public void setCredits(int credits) {
        // Credits are not currently provided by the parser.
    }

    public String getCourse() {
        return programme;
    }

    public void setCourse(String course) {
        this.programme = course;
    }
}