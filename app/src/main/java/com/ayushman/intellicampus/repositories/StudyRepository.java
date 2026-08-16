package com.ayushman.intellicampus.repositories;

import com.ayushman.intellicampus.constants.FirestoreConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.models.Subject;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class StudyRepository {

    public interface SubjectsCallback {
        void onSuccess(List<Subject> subjects);
        void onError(Exception e);
    }

    private final FirebaseFirestore firestore;

    public StudyRepository() {
        firestore = FirestoreManager
                .getInstance()
                .getFirestore();
    }

    public ListenerRegistration listenToSubjects(
            String course,
            int semester,
            String academicSession,
            SubjectsCallback callback
    ) {

        return firestore
                .collection(FirestoreConstants.CURRICULUM)
                .document(course)
                .collection("semester_" + semester)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {
                        callback.onError(error);
                        return;
                    }

                    List<Subject> allSubjects =
                            new ArrayList<>();

                    if (snapshots != null) {

                        for (DocumentSnapshot document :
                                snapshots.getDocuments()) {

                            Subject subject =
                                    document.toObject(
                                            Subject.class
                                    );

                            if (subject == null) {
                                continue;
                            }

                            subject.setId(
                                    document.getId()
                            );

                            allSubjects.add(subject);
                        }
                    }

                    /*
                     * Batch-aware curriculum:
                     *
                     * If the imported curriculum contains an
                     * academicSession matching the student's
                     * admission batch, use those subjects.
                     *
                     * If older Firestore data has no session
                     * information, preserve compatibility and
                     * return the existing semester subjects.
                     */
                    if (academicSession == null ||
                            academicSession.trim().isEmpty()) {

                        callback.onSuccess(allSubjects);
                        return;
                    }

                    List<Subject> sessionSubjects =
                            new ArrayList<>();

                    boolean hasSessionData = false;

                    for (Subject subject : allSubjects) {

                        String subjectSession =
                                subject.getAcademicSession();

                        if (subjectSession != null &&
                                !subjectSession.trim().isEmpty()) {

                            hasSessionData = true;

                            if (academicSession.equalsIgnoreCase(
                                    subjectSession.trim()
                            )) {

                                sessionSubjects.add(subject);
                            }
                        }
                    }

                    if (hasSessionData) {
                        callback.onSuccess(sessionSubjects);
                    } else {
                        callback.onSuccess(allSubjects);
                    }
                });
    }

    public void getSubjects(
            String course,
            int semester,
            String academicSession,
            SubjectsCallback callback
    ) {
        firestore
                .collection(FirestoreConstants.CURRICULUM)
                .document(course)
                .collection("semester_" + semester)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Subject> allSubjects = new ArrayList<>();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Subject subject = document.toObject(Subject.class);
                        if (subject == null) continue;
                        subject.setId(document.getId());
                        allSubjects.add(subject);
                    }

                    if (academicSession == null || academicSession.trim().isEmpty()) {
                        callback.onSuccess(allSubjects);
                        return;
                    }

                    List<Subject> sessionSubjects = new ArrayList<>();
                    boolean hasSessionData = false;
                    for (Subject subject : allSubjects) {
                        String session = subject.getAcademicSession();
                        if (session != null && !session.trim().isEmpty()) {
                            hasSessionData = true;
                            if (academicSession.equalsIgnoreCase(session.trim())) {
                                sessionSubjects.add(subject);
                            }
                        }
                    }
                    callback.onSuccess(hasSessionData ? sessionSubjects : allSubjects);
                })
                .addOnFailureListener(callback::onError);
    }

    // Backwards-compatible overload
    public ListenerRegistration listenToSubjects(
            String course,
            int semester,
            SubjectsCallback callback
    ) {
        return listenToSubjects(
                course,
                semester,
                null,
                callback
        );
    }
}
