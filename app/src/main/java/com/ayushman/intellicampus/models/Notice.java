package com.ayushman.intellicampus.models;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class Notice implements Serializable {

    @DocumentId
    private String id;
    private String title;
    private String description;
    private String category;
    private long date;
    private boolean pinned;
    private String attachmentUrl;

    // Required empty constructor for Firestore serialization
    public Notice() {
    }

    public Notice(String title, String description, String category, long date, boolean pinned, String attachmentUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.date = date;
        this.pinned = pinned;
        this.attachmentUrl = attachmentUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}