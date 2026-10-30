package com.ayushman.intellicampus.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.repositories.AssignmentRepository;

import java.util.ArrayList;
import java.util.List;

public class AssignmentViewModel extends ViewModel {

    private final AssignmentRepository repository;
    private final MutableLiveData<List<Assignment>> assignments;
    private ListenerRegistration listenerRegistration;

    public AssignmentViewModel() {
        repository = new AssignmentRepository();
        assignments = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<Assignment>> getAssignments() {
        return assignments;
    }

    public void loadAssignments() {

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = repository.getAssignments(
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

    public void addAssignment(Assignment assignment,
                              FirestoreCallback<Void> callback) {

        repository.addAssignment(assignment, callback);
    }

    public void updateAssignment(Assignment assignment) {

        repository.updateAssignment(
                assignment,
                new FirestoreCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        // No action needed
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void updateAssignment(Assignment assignment,
                                 FirestoreCallback<Void> callback) {

        repository.updateAssignment(assignment, callback);
    }

    public void deleteAssignment(String assignmentId) {

        repository.deleteAssignment(
                assignmentId,
                new FirestoreCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        // No action needed
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void deleteAssignment(String assignmentId,
                                 FirestoreCallback<Void> callback) {

        repository.deleteAssignment(assignmentId, callback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}