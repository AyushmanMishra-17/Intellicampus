package com.ayushman.intellicampus.models;

import java.io.Serializable;

public class Practical implements Serializable {

    private int number;
    private String description;

    public Practical() {
        // Required for Firestore
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}