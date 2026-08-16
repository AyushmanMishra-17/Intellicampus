package com.ayushman.intellicampus.repositories;

import androidx.annotation.NonNull;

import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Notice;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NoticeRepository {

    private final FirestoreManager firestoreManager;

    public NoticeRepository() {
        firestoreManager = FirestoreManager.getInstance();
    }

    /**
     * Add Notice
     */
    public void addNotice(@NonNull Notice notice,
                          FirestoreCallback<Void> callback) {

        firestoreManager
                .getNoticeCollection()
                .add(notice)
                .addOnSuccessListener(documentReference -> {

                    notice.setId(documentReference.getId());

                    callback.onSuccess(null);

                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Update Notice
     */
    public void updateNotice(@NonNull Notice notice,
                             FirestoreCallback<Void> callback) {

        firestoreManager
                .getNoticeCollection()
                .document(notice.getId())
                .set(notice)
                .addOnSuccessListener(unused ->
                        callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Delete Notice
     */
    public void deleteNotice(@NonNull String noticeId,
                             FirestoreCallback<Void> callback) {

        firestoreManager
                .getNoticeCollection()
                .document(noticeId)
                .delete()
                .addOnSuccessListener(unused ->
                        callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Listen to all notices
     */
    public ListenerRegistration getNotices(
            FirestoreCallback<List<Notice>> callback) {

        return firestoreManager
                .getNoticeCollection()
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }

                    List<Notice> noticeList = new ArrayList<>();

                    if (value != null) {

                        for (DocumentSnapshot document : value.getDocuments()) {

                            Notice notice =
                                    document.toObject(Notice.class);

                            if (notice != null) {

                                notice.setId(document.getId());

                                noticeList.add(notice);
                            }
                        }
                    }

                    Collections.sort(
                            noticeList,
                            Comparator.comparing(Notice::isPinned).reversed()
                    );

                    callback.onSuccess(noticeList);

                });
    }
    /**
     * Returns total number of notices.
     */
    public int getNoticeCount(@NonNull List<Notice> notices) {
        return notices.size();
    }

    /**
     * Returns the latest notice.
     * Assumes notices are already sorted newest first.
     */
    public Notice getLatestNotice(@NonNull List<Notice> notices) {

        if (notices.isEmpty()) {
            return null;
        }

        return notices.get(0);
    }

    /**
     * Returns all pinned notices.
     */
    @NonNull
    public List<Notice> getPinnedNotices(@NonNull List<Notice> notices) {

        List<Notice> pinned = new ArrayList<>();

        for (Notice notice : notices) {

            if (notice.isPinned()) {
                pinned.add(notice);
            }

        }

        return pinned;
    }

    /**
     * Returns all notices of a given category.
     */
    @NonNull
    public List<Notice> getNoticesByCategory(
            @NonNull List<Notice> notices,
            @NonNull String category) {

        List<Notice> filtered = new ArrayList<>();

        for (Notice notice : notices) {

            if (category.equalsIgnoreCase(notice.getCategory())) {
                filtered.add(notice);
            }

        }

        return filtered;
    }
}