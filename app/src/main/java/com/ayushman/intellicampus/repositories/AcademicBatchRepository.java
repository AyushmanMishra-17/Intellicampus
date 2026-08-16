package com.ayushman.intellicampus.repositories;

import com.ayushman.intellicampus.constants.FirestoreConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.models.AcademicBatch;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AcademicBatchRepository {

    public interface BatchCallback {
        void onSuccess(AcademicBatch batch);
        void onError(Exception e);
    }

    public interface BatchListCallback {
        void onSuccess(List<AcademicBatch> batches);
        void onError(Exception e);
    }

    private final FirebaseFirestore firestore;

    public AcademicBatchRepository() {
        firestore = FirestoreManager.getInstance().getFirestore();
    }

    public void getBatch(String batch, BatchCallback callback) {
        firestore.collection(FirestoreConstants.ACADEMIC_BATCHES)
                .document(batch)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        callback.onError(new Exception("Academic batch not found."));
                        return;
                    }

                    AcademicBatch result = document.toObject(AcademicBatch.class);
                    if (result == null) {
                        callback.onError(new Exception("Invalid academic batch data."));
                        return;
                    }

                    if (result.getBatch() == null || result.getBatch().trim().isEmpty()) {
                        result.setBatch(document.getId());
                    }

                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getActiveBatches(BatchListCallback callback) {
        firestore.collection(FirestoreConstants.ACADEMIC_BATCHES)
                .whereEqualTo(FirestoreConstants.ACTIVE, true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<AcademicBatch> result = new ArrayList<>();

                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        AcademicBatch batch = document.toObject(AcademicBatch.class);
                        if (batch != null) {
                            if (batch.getBatch() == null || batch.getBatch().trim().isEmpty()) {
                                batch.setBatch(document.getId());
                            }
                            result.add(batch);
                        }
                    }

                    sortBatches(result);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getAllBatches(BatchListCallback callback) {
        firestore.collection(FirestoreConstants.ACADEMIC_BATCHES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<AcademicBatch> result = new ArrayList<>();

                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        AcademicBatch batch = document.toObject(AcademicBatch.class);
                        if (batch != null) {
                            if (batch.getBatch() == null || batch.getBatch().trim().isEmpty()) {
                                batch.setBatch(document.getId());
                            }
                            result.add(batch);
                        }
                    }

                    sortBatches(result);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onError);
    }

    public void createBatch(
            String batchName,
            String programme,
            BatchCallback callback
    ) {
        String batch = batchName.trim();
        String academicYear = academicYearForSemester(batch, 1);

        AcademicBatch academicBatch = new AcademicBatch(
                batch,
                programme.trim(),
                1,
                academicYear,
                true
        );

        firestore.collection(FirestoreConstants.ACADEMIC_BATCHES)
                .document(batch)
                .get()
                .addOnSuccessListener(existing -> {

                    if (existing.exists()) {
                        callback.onError(
                                new Exception(
                                        "Batch " + batch + " already exists."
                                )
                        );
                        return;
                    }

                    firestore.collection(FirestoreConstants.ACADEMIC_BATCHES)
                            .document(batch)
                            .set(academicBatch)
                            .addOnSuccessListener(
                                    unused -> callback.onSuccess(academicBatch)
                            )
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void promoteBatch(
            AcademicBatch current,
            BatchCallback callback
    ) {
        int fromSemester = current.getCurrentSemester();

        if (fromSemester >= 8) {
            callback.onError(new Exception("Semester 8 is the final semester."));
            return;
        }

        int nextSemester = fromSemester + 1;
        String nextAcademicYear =
                academicYearForSemester(current.getBatch(), nextSemester);

        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put(FirestoreConstants.CURRENT_SEMESTER, nextSemester);
        updates.put(FirestoreConstants.CURRENT_ACADEMIC_YEAR, nextAcademicYear);

        firestore.collection(FirestoreConstants.ACADEMIC_BATCHES)
                .document(current.getBatch())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    AcademicBatch updated = new AcademicBatch(
                            current.getBatch(),
                            current.getProgramme(),
                            nextSemester,
                            nextAcademicYear,
                            current.isActive()
                    );
                    callback.onSuccess(updated);
                })
                .addOnFailureListener(callback::onError);
    }

    public static String academicYearForSemester(
            String batch,
            int semester
    ) {
        try {
            String[] parts = batch.trim().split("-");
            int startYear = Integer.parseInt(parts[0]);

            int yearOffset = (semester - 1) / 2;
            int academicStart = startYear + yearOffset;

            return academicStart + "-" +
                    String.format("%02d", (academicStart + 1) % 100);

        } catch (Exception ignored) {
            return batch;
        }
    }

    private void sortBatches(List<AcademicBatch> batches) {
        Collections.sort(
                batches,
                Comparator.comparing(
                        AcademicBatch::getBatch,
                        Comparator.nullsLast(String::compareTo)
                )
        );
    }
}
