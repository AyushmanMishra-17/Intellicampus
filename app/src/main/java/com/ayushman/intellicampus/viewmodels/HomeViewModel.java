package com.ayushman.intellicampus.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.models.Notice;
import com.ayushman.intellicampus.models.Timetable;
import com.ayushman.intellicampus.repositories.AssignmentRepository;
import com.ayushman.intellicampus.repositories.NoticeRepository;
import com.ayushman.intellicampus.repositories.TimetableRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final AssignmentRepository assignmentRepository;
    private final TimetableRepository timetableRepository;
    private final NoticeRepository noticeRepository;

    private final MutableLiveData<List<Assignment>> assignments =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Timetable>> timetable =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Notice>> notices =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration assignmentListener;
    private ListenerRegistration timetableListener;
    private ListenerRegistration noticeListener;

    public HomeViewModel() {

        assignmentRepository = new AssignmentRepository();
        timetableRepository = new TimetableRepository();
        noticeRepository = new NoticeRepository();

    }

    //==========================
    // LiveData Getters
    //==========================

    public LiveData<List<Assignment>> getAssignments() {
        return assignments;
    }

    public LiveData<List<Timetable>> getTimetable() {
        return timetable;
    }

    public LiveData<List<Notice>> getNotices() {
        return notices;
    }

    //==========================
    // Dashboard Loader
    //==========================

    public void loadDashboard() {

        loadAssignments();
        loadTimetable();
        loadNotices();

    }

    //==========================
    // Assignments
    //==========================

    private void loadAssignments() {

        if (assignmentListener != null) {
            assignmentListener.remove();
        }

        assignmentListener =
                assignmentRepository.getAssignments(
                        new FirestoreCallback<List<Assignment>>() {

                            @Override
                            public void onSuccess(List<Assignment> result) {
                                assignments.postValue(result);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                            }
                        });

    }

    //==========================
    // Timetable
    //==========================

    private void loadTimetable() {

        if (timetableListener != null) {
            timetableListener.remove();
        }

        timetableListener =
                timetableRepository.getTimetable(
                        new FirestoreCallback<List<Timetable>>() {

                            @Override
                            public void onSuccess(List<Timetable> result) {
                                timetable.postValue(result);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                            }
                        });

    }

    //==========================
    // Notices
    //==========================

    private void loadNotices() {

        if (noticeListener != null) {
            noticeListener.remove();
        }

        noticeListener =
                noticeRepository.getNotices(
                        new FirestoreCallback<List<Notice>>() {

                            @Override
                            public void onSuccess(List<Notice> result) {
                                notices.postValue(result);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                            }
                        });

    }

    //==========================
    // Cleanup
    //==========================

    @Override
    protected void onCleared() {
        super.onCleared();

        if (assignmentListener != null) {
            assignmentListener.remove();
        }

        if (timetableListener != null) {
            timetableListener.remove();
        }

        if (noticeListener != null) {
            noticeListener.remove();
        }
    }
}