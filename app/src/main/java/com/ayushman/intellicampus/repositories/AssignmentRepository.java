package com.ayushman.intellicampus.repositories;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ayushman.intellicampus.constants.FirestoreConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentRepository {

    private final FirestoreManager firestoreManager;
    private final CollectionReference assignmentCollection;

    public AssignmentRepository() {

        firestoreManager =
                FirestoreManager.getInstance();

        assignmentCollection =
                firestoreManager.getSharedAssignmentCollection();
    }

    // ------------------------------------------------------------
    // ADD ASSIGNMENT
    // ------------------------------------------------------------

    public void addAssignment(
            Assignment assignment,
            FirestoreCallback<Void> callback) {

        if (assignment == null) {
            callback.onFailure(
                    new IllegalArgumentException(
                            "Assignment cannot be null"
                    )
            );
            return;
        }

        String uid =
                firestoreManager.getCurrentUserId();

        if (uid == null) {
            callback.onFailure(
                    new IllegalStateException(
                            "User not logged in"
                    )
            );
            return;
        }

        long currentTime =
                System.currentTimeMillis();

        if (assignment.getCreatedAt() == 0) {
            assignment.setCreatedAt(currentTime);
        }

        assignment.setUpdatedAt(currentTime);
        assignment.setCreatedBy(uid);

        /*
         * Shared assignment documents must not contain
         * a student's personal completion status.
         */
        Map<String, Object> data =
                new HashMap<>();

        data.put(
                FirestoreConstants.TITLE,
                assignment.getTitle()
        );

        data.put(
                FirestoreConstants.DESCRIPTION,
                assignment.getDescription()
        );

        data.put(
                FirestoreConstants.SUBJECT,
                assignment.getSubject()
        );

        data.put(
                FirestoreConstants.DUE_DATE_TIME,
                assignment.getDueDateTime()
        );

        data.put(
                FirestoreConstants.PRIORITY,
                assignment.getPriority()
        );

        data.put(
                FirestoreConstants.ASSIGNMENT_PROGRAMME,
                assignment.getProgramme()
        );

        data.put(
                FirestoreConstants.ASSIGNMENT_BATCH,
                assignment.getBatch()
        );

        data.put(
                FirestoreConstants.ASSIGNMENT_SEMESTER,
                assignment.getSemester()
        );

        data.put(
                FirestoreConstants.ASSIGNMENT_CREATED_BY,
                assignment.getCreatedBy()
        );

        data.put(
                FirestoreConstants.CREATED_AT,
                assignment.getCreatedAt()
        );

        data.put(
                FirestoreConstants.UPDATED_AT,
                assignment.getUpdatedAt()
        );

        assignmentCollection
                .add(data)
                .addOnSuccessListener(documentReference -> {

                    assignment.setId(
                            documentReference.getId()
                    );

                    callback.onSuccess(null);

                })
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // UPDATE ASSIGNMENT
    // ------------------------------------------------------------

    public void updateAssignment(
            Assignment assignment,
            FirestoreCallback<Void> callback) {

        if (assignment == null ||
                assignment.getId() == null ||
                assignment.getId().trim().isEmpty()) {

            callback.onFailure(
                    new IllegalArgumentException(
                            "Invalid assignment"
                    )
            );
            return;
        }

        long currentTime =
                System.currentTimeMillis();

        assignment.setUpdatedAt(currentTime);

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                FirestoreConstants.TITLE,
                assignment.getTitle()
        );

        updates.put(
                FirestoreConstants.DESCRIPTION,
                assignment.getDescription()
        );

        updates.put(
                FirestoreConstants.SUBJECT,
                assignment.getSubject()
        );

        updates.put(
                FirestoreConstants.DUE_DATE_TIME,
                assignment.getDueDateTime()
        );

        updates.put(
                FirestoreConstants.PRIORITY,
                assignment.getPriority()
        );

        updates.put(
                FirestoreConstants.ASSIGNMENT_PROGRAMME,
                assignment.getProgramme()
        );

        updates.put(
                FirestoreConstants.ASSIGNMENT_BATCH,
                assignment.getBatch()
        );

        updates.put(
                FirestoreConstants.ASSIGNMENT_SEMESTER,
                assignment.getSemester()
        );

        updates.put(
                FirestoreConstants.UPDATED_AT,
                assignment.getUpdatedAt()
        );

        assignmentCollection
                .document(assignment.getId())
                .update(updates)
                .addOnSuccessListener(
                        unused -> callback.onSuccess(null)
                )
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // DELETE ASSIGNMENT
    // ------------------------------------------------------------

    public void deleteAssignment(
            String assignmentId,
            FirestoreCallback<Void> callback) {

        if (assignmentId == null ||
                assignmentId.trim().isEmpty()) {

            callback.onFailure(
                    new IllegalArgumentException(
                            "Invalid assignment ID"
                    )
            );
            return;
        }

        assignmentCollection
                .document(assignmentId)
                .delete()
                .addOnSuccessListener(
                        unused -> callback.onSuccess(null)
                )
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // GET TARGETED ASSIGNMENTS
    // ------------------------------------------------------------

    public ListenerRegistration getAssignments(
            String programme,
            String batch,
            int semester,
            FirestoreCallback<List<Assignment>> callback) {

        if (programme == null ||
                programme.trim().isEmpty()) {

            callback.onFailure(
                    new IllegalArgumentException(
                            "Programme is required"
                    )
            );

            return null;
        }

        if (batch == null ||
                batch.trim().isEmpty()) {

            callback.onFailure(
                    new IllegalArgumentException(
                            "Batch is required"
                    )
            );

            return null;
        }

        Query query =
                assignmentCollection
                        .whereEqualTo(
                                FirestoreConstants.ASSIGNMENT_PROGRAMME,
                                programme.trim()
                        )
                        .whereEqualTo(
                                FirestoreConstants.ASSIGNMENT_BATCH,
                                batch.trim()
                        )
                        .whereEqualTo(
                                FirestoreConstants.ASSIGNMENT_SEMESTER,
                                semester
                        );

        return query.addSnapshotListener(
                (snapshot, error) -> {

                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }

                    List<Assignment> assignments =
                            new ArrayList<>();

                    long currentTime =
                            System.currentTimeMillis();

                    if (snapshot != null) {

                        for (DocumentSnapshot document :
                                snapshot.getDocuments()) {

                            try {

                                Assignment assignment =
                                        document.toObject(
                                                Assignment.class
                                        );

                                if (assignment == null) {
                                    continue;
                                }

                                assignment.setId(
                                        document.getId()
                                );

                                /*
                                 * Status is currently calculated
                                 * locally from the due date.
                                 *
                                 * Personal completion will be
                                 * separated in the next step.
                                 */
                                if (assignment.getStatus()
                                        != Assignment.STATUS_COMPLETED) {

                                    if (assignment.getDueDateTime()
                                            <= currentTime) {

                                        assignment.setStatus(
                                                Assignment.STATUS_OVERDUE
                                        );

                                    } else {

                                        assignment.setStatus(
                                                Assignment.STATUS_PENDING
                                        );
                                    }
                                }

                                assignments.add(assignment);

                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                        }
                    }

                    Collections.sort(
                            assignments,
                            Comparator.comparingLong(
                                    Assignment::getDueDateTime
                            )
                    );

                    callback.onSuccess(assignments);
                }
        );
    }

    // ------------------------------------------------------------
    // COMPATIBILITY GET
    // ------------------------------------------------------------

    public ListenerRegistration getAssignments(
            FirestoreCallback<List<Assignment>> callback) {

        return assignmentCollection
                .addSnapshotListener(
                        (snapshot, error) -> {

                            if (error != null) {
                                callback.onFailure(error);
                                return;
                            }

                            List<Assignment> assignments =
                                    new ArrayList<>();

                            long currentTime =
                                    System.currentTimeMillis();

                            if (snapshot != null) {

                                for (DocumentSnapshot document :
                                        snapshot.getDocuments()) {

                                    try {

                                        Assignment assignment =
                                                document.toObject(
                                                        Assignment.class
                                                );

                                        if (assignment == null) {
                                            continue;
                                        }

                                        assignment.setId(
                                                document.getId()
                                        );

                                        if (assignment.getStatus()
                                                != Assignment.STATUS_COMPLETED) {

                                            if (assignment.getDueDateTime()
                                                    <= currentTime) {

                                                assignment.setStatus(
                                                        Assignment.STATUS_OVERDUE
                                                );

                                            } else {

                                                assignment.setStatus(
                                                        Assignment.STATUS_PENDING
                                                );
                                            }
                                        }

                                        assignments.add(assignment);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            Collections.sort(
                                    assignments,
                                    Comparator.comparingLong(
                                            Assignment::getDueDateTime
                                    )
                            );

                            callback.onSuccess(assignments);
                        }
                );
    }

    // ------------------------------------------------------------
    // COUNT
    // ------------------------------------------------------------

    public void getAssignmentCount(
            FirestoreCallback<Integer> callback) {

        assignmentCollection
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onSuccess(
                                snapshot.size()
                        )
                )
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // UPCOMING
    // ------------------------------------------------------------

    public void getUpcomingAssignments(
            FirestoreCallback<List<Assignment>> callback) {

        assignmentCollection
                .orderBy(
                        FirestoreConstants.DUE_DATE_TIME,
                        Query.Direction.ASCENDING
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Assignment> result =
                            new ArrayList<>();

                    long now =
                            System.currentTimeMillis();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        Assignment assignment =
                                document.toObject(
                                        Assignment.class
                                );

                        if (assignment == null) {
                            continue;
                        }

                        assignment.setId(
                                document.getId()
                        );

                        if (assignment.getDueDateTime() > now) {
                            result.add(assignment);
                        }
                    }

                    callback.onSuccess(result);

                })
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // COMPATIBILITY HELPER
    // ------------------------------------------------------------

    public Assignment getUpcomingAssignment(
            List<Assignment> assignments) {

        if (assignments == null ||
                assignments.isEmpty()) {
            return null;
        }

        long currentTime =
                System.currentTimeMillis();

        Assignment upcoming = null;

        for (Assignment assignment : assignments) {

            if (assignment == null) {
                continue;
            }

            if (assignment.getStatus()
                    == Assignment.STATUS_COMPLETED) {
                continue;
            }

            if (assignment.getDueDateTime()
                    <= currentTime) {
                continue;
            }

            if (upcoming == null ||
                    assignment.getDueDateTime()
                            < upcoming.getDueDateTime()) {

                upcoming = assignment;
            }
        }

        return upcoming;
    }

    // ------------------------------------------------------------
    // COMPLETED
    // ------------------------------------------------------------

    public void getCompletedAssignments(
            FirestoreCallback<List<Assignment>> callback) {

        assignmentCollection
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Assignment> result =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        Assignment assignment =
                                document.toObject(
                                        Assignment.class
                                );

                        if (assignment == null) {
                            continue;
                        }

                        assignment.setId(
                                document.getId()
                        );

                        if (assignment.getStatus()
                                == Assignment.STATUS_COMPLETED) {

                            result.add(assignment);
                        }
                    }

                    callback.onSuccess(result);

                })
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // OVERDUE
    // ------------------------------------------------------------

    public void getOverdueAssignments(
            FirestoreCallback<List<Assignment>> callback) {

        assignmentCollection
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Assignment> result =
                            new ArrayList<>();

                    long now =
                            System.currentTimeMillis();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        Assignment assignment =
                                document.toObject(
                                        Assignment.class
                                );

                        if (assignment == null) {
                            continue;
                        }

                        assignment.setId(
                                document.getId()
                        );

                        if (assignment.getStatus()
                                != Assignment.STATUS_COMPLETED &&
                                assignment.getDueDateTime()
                                        <= now) {

                            assignment.setStatus(
                                    Assignment.STATUS_OVERDUE
                            );

                            result.add(assignment);
                        }
                    }

                    callback.onSuccess(result);

                })
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    // ------------------------------------------------------------
    // PENDING
    // ------------------------------------------------------------

    public void getPendingAssignments(
            FirestoreCallback<List<Assignment>> callback) {

        assignmentCollection
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Assignment> result =
                            new ArrayList<>();

                    long now =
                            System.currentTimeMillis();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        Assignment assignment =
                                document.toObject(
                                        Assignment.class
                                );

                        if (assignment == null) {
                            continue;
                        }

                        assignment.setId(
                                document.getId()
                        );

                        if (assignment.getStatus()
                                != Assignment.STATUS_COMPLETED &&
                                assignment.getDueDateTime()
                                        > now) {

                            assignment.setStatus(
                                    Assignment.STATUS_PENDING
                            );

                            result.add(assignment);
                        }
                    }

                    callback.onSuccess(result);

                })
                .addOnFailureListener(
                        callback::onFailure
                );
    }
}