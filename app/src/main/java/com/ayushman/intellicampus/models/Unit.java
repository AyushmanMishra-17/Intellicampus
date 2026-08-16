package com.ayushman.intellicampus.models;

import java.io.Serializable;

public class Unit implements Serializable {

    private String unit;
    private int hours;
    private String content;

    public Unit() {
        // Required for Firestore
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}