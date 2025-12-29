package com.racingtimer.storage;

/**
 * Callback interface for asynchronous storage operations.
 */
public interface StorageCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
