package com.ayushman.intellicampus.repositories;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AssignmentRepository {

    private final FirestoreManager firestoreManager;

    public AssignmentRepository() {
        firestoreManager = FirestoreManager.getInstance();
    }

    /**
     * Add Assignment
     */
    public void addAssignment(Assignment assignment,
                              FirestoreCallback<Void> callback) {

        firestoreManager
                .getAssignmentCollection()
                .add(assignment)
                .addOnSuccessListener(documentReference -> {

                    assignment.setId(documentReference.getId());

                    callback.onSuccess(null);

                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Update Assignment
     */
    public void updateAssignment(@NonNull Assignment assignment,
                                 FirestoreCallback<Void> callback) {

        firestoreManager
                .getAssignmentCollection()
                .document(assignment.getId())
                .set(assignment)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Delete Assignment
     */
    public void deleteAssignment(String assignmentId,
                                 FirestoreCallback<Void> callback) {

        firestoreManager
                .getAssignmentCollection()
                .document(assignmentId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Listen to all assignments
     */
    public ListenerRegistration getAssignments(
            FirestoreCallback<List<Assignment>> callback) {

        return firestoreManager
                .getAssignmentCollection()
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }

                    List<Assignment> assignments = new ArrayList<>();

                    if (value != null) {

                        for (DocumentSnapshot document : value.getDocuments()) {

                            Assignment assignment =
                                    document.toObject(Assignment.class);

                            if (assignment != null) {

                                assignment.setId(document.getId());

                                assignments.add(assignment);
                            }
                        }
                    }

                    Collections.sort(assignments,
                            Comparator.comparingLong(Assignment::getDueDateTime));

                    callback.onSuccess(assignments);

                });
    }
    /**
     * Returns total number of assignments.
     */
    public int getAssignmentCount(@NonNull List<Assignment> assignments) {
        return assignments.size();
    }

    /**
     * Returns the nearest upcoming pending assignment.
     * Assumes the list is already sorted by due date.
     */
    public Assignment getUpcomingAssignment(@NonNull List<Assignment> assignments) {

        long currentTime = System.currentTimeMillis();

        for (Assignment assignment : assignments) {

            if (assignment.isCompleted()) {
                continue;
            }

            if (assignment.getDueDateTime() < currentTime) {
                continue;
            }

            return assignment;
        }

        return null;
    }

    /**
     * Returns all completed assignments.
     */
    public List<Assignment> getCompletedAssignments(
            @NonNull List<Assignment> assignments) {

        List<Assignment> completed = new ArrayList<>();

        for (Assignment assignment : assignments) {

            if (assignment.isCompleted()) {
                completed.add(assignment);
            }

        }

        return completed;
    }

    /**
     * Returns all overdue assignments.
     */
    public List<Assignment> getOverdueAssignments(
            @NonNull List<Assignment> assignments) {

        List<Assignment> overdue = new ArrayList<>();

        for (Assignment assignment : assignments) {

            if (assignment.isOverdue()) {
                overdue.add(assignment);
            }

        }

        return overdue;
    }

    /**
     * Returns all pending assignments.
     */
    public List<Assignment> getPendingAssignments(
            @NonNull List<Assignment> assignments) {

        List<Assignment> pending = new ArrayList<>();

        for (Assignment assignment : assignments) {

            if (!assignment.isCompleted() && !assignment.isOverdue()) {
                pending.add(assignment);
            }

        }

        return pending;
    }
}
