package com.ayushman.intellicampus.repositories;

import androidx.annotation.NonNull;

import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Timetable;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class TimetableRepository {

    private final FirestoreManager firestoreManager;

    public TimetableRepository() {
        firestoreManager = FirestoreManager.getInstance();
    }

    // =========================
    // CREATE
    // =========================

    public Task<Void> addTimetable(@NonNull Timetable timetable) {

        String documentId = firestoreManager
                .getTimetableCollection()
                .document()
                .getId();

        timetable.setId(documentId);

        return firestoreManager
                .getTimetableCollection()
                .document(documentId)
                .set(timetable);
    }

    // =========================
    // UPDATE
    // =========================

    public Task<Void> updateTimetable(@NonNull Timetable timetable) {

        return firestoreManager
                .getTimetableCollection()
                .document(timetable.getId())
                .set(timetable);
    }

    // =========================
    // DELETE
    // =========================

    public Task<Void> deleteTimetable(@NonNull String timetableId) {

        return firestoreManager
                .getTimetableCollection()
                .document(timetableId)
                .delete();
    }

    // =========================
    // READ
    // =========================

    public ListenerRegistration getTimetable(
            @NonNull FirestoreCallback<List<Timetable>> callback) {

        return firestoreManager
                .getTimetableCollection()
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }

                    List<Timetable> timetableList = new ArrayList<>();

                    if (value != null) {

                        for (DocumentSnapshot document : value.getDocuments()) {

                            Timetable timetable =
                                    document.toObject(Timetable.class);

                            if (timetable != null) {
                                timetable.setId(document.getId());
                                timetableList.add(timetable);
                            }
                        }
                    }

                    callback.onSuccess(timetableList);
                });
    }
    /**
     * Returns total number of classes.
     */
    public int getClassCount(@NonNull List<Timetable> timetableList) {
        return timetableList.size();
    }

    /**
     * Returns total classes for a given day.
     */
    public int getTodayClassCount(
            @NonNull List<Timetable> timetableList,
            @NonNull String day) {

        int count = 0;

        for (Timetable timetable : timetableList) {

            if (day.equalsIgnoreCase(timetable.getDay())) {
                count++;
            }

        }

        return count;
    }

    /**
     * Returns all classes for a given day.
     */
    @NonNull
    public List<Timetable> getTodayClasses(
            @NonNull List<Timetable> timetableList,
            @NonNull String day) {

        List<Timetable> todayClasses = new ArrayList<>();

        for (Timetable timetable : timetableList) {

            if (day.equalsIgnoreCase(timetable.getDay())) {
                todayClasses.add(timetable);
            }

        }

        return todayClasses;
    }

    /**
     * Returns the next upcoming class.
     * Assumes the list is already ordered by time.
     */
    public Timetable getNextClass(
            @NonNull List<Timetable> timetableList,
            @NonNull String day,
            @NonNull String currentTime) {

        for (Timetable timetable : timetableList) {

            if (!day.equalsIgnoreCase(timetable.getDay())) {
                continue;
            }

            if (timetable.getStartTime().compareTo(currentTime) >= 0) {
                return timetable;
            }

        }

        return null;
    }
}