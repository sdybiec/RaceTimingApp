package com.racingtimer.service;

import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.storage.StorageCallback;
import com.racingtimer.storage.TimingStorage;

import java.util.List;

/**
 * Service for synchronizing data with the Race Results Repository.
 */
public class SyncService {

    private final TimingStorage storage;
    private String apiEndpoint;
    private boolean isSyncing = false;

    public SyncService(TimingStorage storage) {
        this.storage = storage;
    }

    public void setApiEndpoint(String endpoint) {
        this.apiEndpoint = endpoint;
    }

    /**
     * Sync unsynced timing entries with the backend.
     */
    public void syncTimingEntries(final StorageCallback<Integer> callback) {
        if (isSyncing) {
            callback.onError("Sync already in progress");
            return;
        }

        isSyncing = true;

        storage.getUnsyncedTimingEntries(new StorageCallback<List<TimingEntry>>() {
            @Override
            public void onSuccess(List<TimingEntry> entries) {
                if (entries.isEmpty()) {
                    isSyncing = false;
                    callback.onSuccess(0);
                    return;
                }

                // TODO: Implement actual HTTP sync with Race-Results-Repository
                // For now, just mark as synced
                int syncedCount = 0;
                for (TimingEntry entry : entries) {
                    entry.setSynced(true);
                    storage.saveTimingEntry(entry, new StorageCallback<TimingEntry>() {
                        @Override
                        public void onSuccess(TimingEntry result) {
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                    syncedCount++;
                }

                isSyncing = false;
                callback.onSuccess(syncedCount);
            }

            @Override
            public void onError(String error) {
                isSyncing = false;
                callback.onError(error);
            }
        });
    }

    /**
     * Download the start list from the backend.
     */
    public void downloadStartList(final StorageCallback<List<Race>> callback) {
        // TODO: Implement HTTP call to Race-Results-Repository
        // For now, return empty list
        callback.onSuccess(new java.util.ArrayList<Race>());
    }

    public boolean isSyncing() {
        return isSyncing;
    }
}
