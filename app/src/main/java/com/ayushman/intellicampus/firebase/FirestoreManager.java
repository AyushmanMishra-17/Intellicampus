package com.ayushman.intellicampus.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ayushman.intellicampus.constants.FirestoreConstants;
import com.google.firebase.firestore.DocumentReference;

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

        if (auth.getCurrentUser() == null)
            return null;

        return auth.getCurrentUser().getUid();
    }

    public CollectionReference getUserCollection() {

        return firestore.collection(FirestoreConstants.USERS);

    }

    public CollectionReference getAssignmentCollection() {

        String uid = getCurrentUserId();

        if (uid == null)
            throw new IllegalStateException("User not logged in");

        return firestore
                .collection(FirestoreConstants.USERS)
                .document(uid)
                .collection(FirestoreConstants.ASSIGNMENTS);
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

        if (uid == null)
            throw new IllegalStateException("User not logged in");

        return firestore
                .collection(FirestoreConstants.USERS)
                .document(uid);

    }
}