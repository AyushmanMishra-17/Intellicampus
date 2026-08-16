package com.ayushman.intellicampus.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Notice;
import com.ayushman.intellicampus.repositories.NoticeRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NoticeViewModel extends ViewModel {

    private final NoticeRepository repository;
    private final MutableLiveData<List<Notice>> noticeList;
    private ListenerRegistration listenerRegistration;

    public NoticeViewModel() {

        repository = new NoticeRepository();
        noticeList = new MutableLiveData<>(new ArrayList<>());

    }

    public LiveData<List<Notice>> getNoticeList() {
        return noticeList;
    }

    public void loadNotices() {

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = repository.getNotices(

                new FirestoreCallback<List<Notice>>() {

                    @Override
                    public void onSuccess(List<Notice> result) {
                        noticeList.postValue(result);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }

                }

        );

    }

    public void addNotice(Notice notice,
                          FirestoreCallback<Void> callback) {

        repository.addNotice(notice, callback);

    }

    public void updateNotice(Notice notice,
                             FirestoreCallback<Void> callback) {

        repository.updateNotice(notice, callback);

    }

    public void deleteNotice(String noticeId,
                             FirestoreCallback<Void> callback) {

        repository.deleteNotice(noticeId, callback);

    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}