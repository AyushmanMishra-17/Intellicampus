package com.ayushman.intellicampus.models;

import java.io.Serializable;

public class CourseOutcome implements Serializable {

    private String code;
    private String description;

    public CourseOutcome() {
        // Required for Firestore
    }

    public CourseOutcome(
            String code,
            String description
    ) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}