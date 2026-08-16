package com.ayushman.intellicampus.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class Assignment {

    // Priority Constants
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    // Status Constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_OVERDUE = 2;

    @Exclude
    private String id;

    private String title;
    private String description;
    private String subject;
    private long dueDateTime;
    private int priority;
    private int status;
    private long createdAt;
    private long updatedAt;

    public Assignment() {
        // Required for Firestore
    }

    public Assignment(String title,
                      String description,
                      String subject,
                      long dueDateTime,
                      int priority,
                      int status,
                      long createdAt,
                      long updatedAt) {

        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDateTime = dueDateTime;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(long dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }

    @Exclude
    public boolean isOverdue() {
        return status == STATUS_OVERDUE;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Assignment)) return false;

        Assignment that = (Assignment) o;

        if (dueDateTime != that.dueDateTime) return false;
        if (priority != that.priority) return false;
        if (status != that.status) return false;
        if (createdAt != that.createdAt) return false;
        if (updatedAt != that.updatedAt) return false;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;

        if (title != null ? !title.equals(that.title) : that.title != null)
            return false;

        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;

        return subject != null
                ? subject.equals(that.subject)
                : that.subject == null;
    }

    @Override
    public int hashCode() {

        int result = id != null ? id.hashCode() : 0;

        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (int) (dueDateTime ^ (dueDateTime >>> 32));
        result = 31 * result + priority;
        result = 31 * result + status;
        result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
        result = 31 * result + (int) (updatedAt ^ (updatedAt >>> 32));

        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Assignment{" +
                "title='" + title + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}