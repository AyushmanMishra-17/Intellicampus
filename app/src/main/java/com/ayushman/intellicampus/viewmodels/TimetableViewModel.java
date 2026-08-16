package com.ayushman.intellicampus.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Timetable;
import com.ayushman.intellicampus.repositories.TimetableRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class TimetableViewModel extends ViewModel {

    private final TimetableRepository repository;
    private final MutableLiveData<List<Timetable>> timetableList;
    private ListenerRegistration listenerRegistration;

    public TimetableViewModel() {
        repository = new TimetableRepository();
        timetableList = new MutableLiveData<>();
    }

    // =========================
    // READ
    // =========================

    public LiveData<List<Timetable>> getTimetableList() {
        return timetableList;
    }

    public void loadTimetable() {

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = repository.getTimetable(new FirestoreCallback<List<Timetable>>() {
            @Override
            public void onSuccess(List<Timetable> result) {
                timetableList.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                // TODO: Expose error through LiveData if needed
            }
        });
    }

    // =========================
    // CREATE
    // =========================

    public Task<Void> addTimetable(Timetable timetable) {
        return repository.addTimetable(timetable);
    }

    // =========================
    // UPDATE
    // =========================

    public Task<Void> updateTimetable(Timetable timetable) {
        return repository.updateTimetable(timetable);
    }

    // =========================
    // DELETE
    // =========================

    public Task<Void> deleteTimetable(String timetableId) {
        return repository.deleteTimetable(timetableId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}