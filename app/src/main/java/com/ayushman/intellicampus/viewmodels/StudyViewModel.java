package com.ayushman.intellicampus.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ayushman.intellicampus.models.Subject;
import com.ayushman.intellicampus.repositories.StudyRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class StudyViewModel extends ViewModel {

    private final StudyRepository repository;

    private final MutableLiveData<List<Subject>> subjects =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private ListenerRegistration listenerRegistration;

    public StudyViewModel() {
        repository = new StudyRepository();
    }

    public LiveData<List<Subject>> getSubjects() {
        return subjects;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadSubjects(
            String course,
            int semester,
            String academicSession
    ) {

        removeListener();

        listenerRegistration =
                repository.listenToSubjects(
                        course,
                        semester,
                        academicSession,
                        new StudyRepository.SubjectsCallback() {

                            @Override
                            public void onSuccess(
                                    List<Subject> result
                            ) {
                                subjects.postValue(result);
                            }

                            @Override
                            public void onError(
                                    Exception e
                            ) {
                                error.postValue(
                                        e.getMessage()
                                );
                            }
                        }
                );
    }

    public void loadSubjects(
            String course,
            int semester
    ) {
        loadSubjects(course, semester, null);
    }

    private void removeListener() {

        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    protected void onCleared() {
        removeListener();
        super.onCleared();
    }
}
