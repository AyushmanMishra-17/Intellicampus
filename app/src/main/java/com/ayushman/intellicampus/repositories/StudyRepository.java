package com.ayushman.intellicampus.repositories;

import androidx.annotation.NonNull;

import com.ayushman.intellicampus.models.Subject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudyRepository {

    private final FirebaseFirestore db;

    public StudyRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback used when loading subjects.
     */
    public interface SubjectsCallback {

        void onSuccess(List<Subject> result);

        void onError(Exception e);
    }

    /**
     * Listen for subjects belonging to a particular
     * programme, semester and academic session.
     */
    public ListenerRegistration listenToSubjects(
            String course,
            int semester,
            String academicSession,
            SubjectsCallback callback
    ) {

        course = course == null
                ? ""
                : course.trim();

        academicSession = academicSession == null
                ? ""
                : academicSession.trim();

        // Lambda expressions require captured local variables
        // to be final or effectively final.
        final String sessionFilter = academicSession;

        String semesterPath = "semester_" + semester;

        return db.collection("curriculum")
                .document(course)
                .collection(semesterPath)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                        return;
                    }

                    if (snapshots == null) {
                        if (callback != null) {
                            callback.onSuccess(new ArrayList<>());
                        }
                        return;
                    }

                    List<Subject> result = new ArrayList<>();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document
                            : snapshots) {

                        try {
                            Subject subject =
                                    document.toObject(Subject.class);

                            /*
                             * Keep the Firestore document ID available
                             * as the subject ID.
                             */
                            if (subject.getId() == null ||
                                    subject.getId().trim().isEmpty()) {

                                subject.setId(document.getId());
                            }

                            /*
                             * If the document contains an academic
                             * session, filter it here.
                             */
                            if (!sessionFilter.isEmpty()) {

                                String documentSession =
                                        document.getString(
                                                "academicSession"
                                        );

                                if (documentSession != null &&
                                        !documentSession.trim().isEmpty() &&
                                        !sessionFilter.equalsIgnoreCase(
                                                documentSession.trim()
                                        )) {

                                    continue;
                                }
                            }

                            result.add(subject);

                        } catch (Exception ignored) {
                            /*
                             * Ignore malformed individual documents
                             * rather than failing the entire subject list.
                             */
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                });
    }

    /**
     * Convenience overload when no academic session is specified.
     */
    /**
     * Compatibility method used by AITutorFragment.
     */
    public void getSubjects(
            String course,
            int semester,
            String academicSession,
            SubjectsCallback callback
    ) {
        course = course == null
                ? ""
                : course.trim();

        academicSession = academicSession == null
                ? ""
                : academicSession.trim();

        final String sessionFilter = academicSession;

        String semesterPath = "semester_" + semester;

        db.collection("curriculum")
                .document(course)
                .collection(semesterPath)
                .get()
                .addOnSuccessListener(snapshots -> {

                    List<Subject> result = new ArrayList<>();

                    if (snapshots != null) {

                        for (com.google.firebase.firestore.QueryDocumentSnapshot document
                                : snapshots) {

                            try {
                                Subject subject =
                                        document.toObject(Subject.class);

                                if (subject.getId() == null ||
                                        subject.getId().trim().isEmpty()) {

                                    subject.setId(document.getId());
                                }

                                if (!sessionFilter.isEmpty()) {

                                    String documentSession =
                                            document.getString(
                                                    "academicSession"
                                            );

                                    if (documentSession != null &&
                                            !documentSession.trim().isEmpty() &&
                                            !sessionFilter.equalsIgnoreCase(
                                                    documentSession.trim()
                                            )) {

                                        continue;
                                    }
                                }

                                result.add(subject);

                            } catch (Exception ignored) {
                                // Ignore malformed documents.
                            }
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                })
                .addOnFailureListener(e -> {

                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }
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