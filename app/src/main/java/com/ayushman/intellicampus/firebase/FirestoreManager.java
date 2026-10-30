package com.ayushman.intellicampus.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ayushman.intellicampus.constants.FirestoreConstants;

public class FirestoreManager {

    private static FirestoreManager instance;

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private FirestoreManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {

        if (instance == null) {
            instance = new FirestoreManager();
        }

        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public String getCurrentUserId() {

        if (auth.getCurrentUser() == null) {
            return null;
        }

        return auth.getCurrentUser().getUid();
    }

    public CollectionReference getUserCollection() {

        return firestore.collection(
                FirestoreConstants.USERS
        );
    }

    /**
     * Shared assignment collection.
     *
     * Assignments are academic resources and therefore belong
     * to the application rather than to an individual user.
     */
    public CollectionReference getAssignmentCollection() {

        return firestore.collection(
                FirestoreConstants.ASSIGNMENTS
        );
    }

    /**
     * Explicitly named version of the shared assignment collection.
     *
     * This makes the new architecture clearer when used by
     * repositories and future management screens.
     */
    public CollectionReference getSharedAssignmentCollection() {

        return firestore.collection(
                FirestoreConstants.ASSIGNMENTS
        );
    }

    public CollectionReference getTimetableCollection() {

        return firestore
                .collection("college")
                .document("dbit")
                .collection(FirestoreConstants.TIMETABLE);
    }

    public CollectionReference getNoticeCollection() {

        return firestore
                .collection("college")
                .document("dbit")
                .collection(FirestoreConstants.NOTICES);
    }

    public DocumentReference getCurrentUserDocument() {

        String uid = getCurrentUserId();

        if (uid == null) {
            throw new IllegalStateException(
                    "User not logged in"
            );
        }

        return firestore
                .collection(FirestoreConstants.USERS)
                .document(uid);
    }
}