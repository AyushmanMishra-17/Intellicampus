package com.ayushman.intellicampus.repositories;

import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AssignmentStatusRepository {

    private static final String COLLECTION =
            "assignmentStatus";

    private static final String FIELD_COMPLETED =
            "completed";

    private static final String FIELD_UPDATED_AT =
            "updatedAt";

    private final FirebaseFirestore firestore;

    public AssignmentStatusRepository() {

        firestore =
                FirestoreManager
                        .getInstance()
                        .getFirestore();
    }

    private DocumentReference getStatusDocument(
            String assignmentId
    ) {

        String uid =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser() == null
                        ? null
                        : FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        .getUid();

        if (uid == null ||
                assignmentId == null ||
                assignmentId.trim().isEmpty()) {

            return null;
        }

        return firestore
                .collection("users")
                .document(uid)
                .collection(COLLECTION)
                .document(assignmentId);
    }

    public void getCompletionStatus(
            String assignmentId,
            FirestoreCallback<Boolean> callback
    ) {

        DocumentReference reference =
                getStatusDocument(assignmentId);

        if (reference == null) {

            callback.onFailure(
                    new IllegalStateException(
                            "User is not authenticated"
                    )
            );

            return;
        }

        reference
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        callback.onSuccess(false);
                        return;
                    }

                    Boolean completed =
                            document.getBoolean(
                                    FIELD_COMPLETED
                            );

                    callback.onSuccess(
                            completed != null &&
                                    completed
                    );
                })
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    public void setCompletionStatus(
            String assignmentId,
            boolean completed,
            FirestoreCallback<Void> callback
    ) {

        DocumentReference reference =
                getStatusDocument(assignmentId);

        if (reference == null) {

            callback.onFailure(
                    new IllegalStateException(
                            "User is not authenticated"
                    )
            );

            return;
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                FIELD_COMPLETED,
                completed
        );

        data.put(
                FIELD_UPDATED_AT,
                System.currentTimeMillis()
        );

        reference
                .set(data)
                .addOnSuccessListener(
                        unused -> callback.onSuccess(null)
                )
                .addOnFailureListener(
                        callback::onFailure
                );
    }

    public void deleteCompletionStatus(
            String assignmentId,
            FirestoreCallback<Void> callback
    ) {

        DocumentReference reference =
                getStatusDocument(assignmentId);

        if (reference == null) {

            callback.onFailure(
                    new IllegalStateException(
                            "User is not authenticated"
                    )
            );

            return;
        }

        reference
                .delete()
                .addOnSuccessListener(
                        unused -> callback.onSuccess(null)
                )
                .addOnFailureListener(
                        callback::onFailure
                );
    }
}