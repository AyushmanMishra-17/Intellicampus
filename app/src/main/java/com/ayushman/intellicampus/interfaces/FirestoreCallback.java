package com.ayushman.intellicampus.interfaces;

public interface FirestoreCallback<T> {

    void onSuccess(T result);

    void onFailure(Exception e);

}