package com.racingtimer.client.storage;

import com.google.gwt.storage.client.Storage;
import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.storage.StorageCallback;
import com.racingtimer.storage.TimingStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * LocalStorage implementation for web browser.
 */
public class LocalTimingStorage implements TimingStorage {

    private static final String TIMING_ENTRIES_KEY = "timingEntries";
    private static final String RACES_KEY = "races";
    private static final String CREWS_KEY = "crews";

    private final Storage storage;

    public LocalTimingStorage() {
        this.storage = Storage.getLocalStorageIfSupported();
        if (storage == null) {
            throw new RuntimeException("LocalStorage not supported");
        }
    }

    @Override
    public void saveTimingEntry(TimingEntry entry, StorageCallback<TimingEntry> callback) {
        try {
            List<TimingEntry> entries = getTimingEntriesList();

            // Remove existing entry with same ID
            entries.removeIf(e -> e.getId().equals(entry.getId()));

            // Add new/updated entry
            entries.add(entry);

            storage.setItem(TIMING_ENTRIES_KEY, serializeTimingEntries(entries));
            callback.onSuccess(entry);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getTimingEntries(String raceId, StorageCallback<List<TimingEntry>> callback) {
        try {
            List<TimingEntry> allEntries = getTimingEntriesList();
            List<TimingEntry> raceEntries = new ArrayList<>();

            for (TimingEntry entry : allEntries) {
                if (raceId.equals(entry.getRaceId())) {
                    raceEntries.add(entry);
                }
            }

            callback.onSuccess(raceEntries);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getUnsyncedTimingEntries(StorageCallback<List<TimingEntry>> callback) {
        try {
            List<TimingEntry> allEntries = getTimingEntriesList();
            List<TimingEntry> unsyncedEntries = new ArrayList<>();

            for (TimingEntry entry : allEntries) {
                if (!entry.isSynced()) {
                    unsyncedEntries.add(entry);
                }
            }

            callback.onSuccess(unsyncedEntries);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void deleteTimingEntry(String id, StorageCallback<Void> callback) {
        try {
            List<TimingEntry> entries = getTimingEntriesList();
            entries.removeIf(e -> e.getId().equals(id));
            storage.setItem(TIMING_ENTRIES_KEY, serializeTimingEntries(entries));
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void saveRace(Race race, StorageCallback<Race> callback) {
        try {
            List<Race> races = getRacesList();
            races.removeIf(r -> r.getId().equals(race.getId()));
            races.add(race);
            storage.setItem(RACES_KEY, serializeRaces(races));
            callback.onSuccess(race);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getRace(String raceId, StorageCallback<Race> callback) {
        try {
            List<Race> races = getRacesList();
            for (Race race : races) {
                if (race.getId().equals(raceId)) {
                    callback.onSuccess(race);
                    return;
                }
            }
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getAllRaces(StorageCallback<List<Race>> callback) {
        try {
            callback.onSuccess(getRacesList());
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void deleteRace(String raceId, StorageCallback<Void> callback) {
        try {
            List<Race> races = getRacesList();
            races.removeIf(r -> r.getId().equals(raceId));
            storage.setItem(RACES_KEY, serializeRaces(races));
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void saveCrew(Crew crew, StorageCallback<Crew> callback) {
        try {
            List<Crew> crews = getCrewsList();
            crews.removeIf(c -> c.getId().equals(crew.getId()));
            crews.add(crew);
            storage.setItem(CREWS_KEY, serializeCrews(crews));
            callback.onSuccess(crew);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getCrew(String crewId, StorageCallback<Crew> callback) {
        try {
            List<Crew> crews = getCrewsList();
            for (Crew crew : crews) {
                if (crew.getId().equals(crewId)) {
                    callback.onSuccess(crew);
                    return;
                }
            }
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getCrewByBibNumber(int bibNumber, String raceId, StorageCallback<Crew> callback) {
        try {
            List<Crew> crews = getCrewsList();
            for (Crew crew : crews) {
                if (crew.getBibNumber() == bibNumber && crew.getRaceId().equals(raceId)) {
                    callback.onSuccess(crew);
                    return;
                }
            }
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void getCrewsForRace(String raceId, StorageCallback<List<Crew>> callback) {
        try {
            List<Crew> allCrews = getCrewsList();
            List<Crew> raceCrews = new ArrayList<>();

            for (Crew crew : allCrews) {
                if (raceId.equals(crew.getRaceId())) {
                    raceCrews.add(crew);
                }
            }

            callback.onSuccess(raceCrews);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void deleteCrew(String crewId, StorageCallback<Void> callback) {
        try {
            List<Crew> crews = getCrewsList();
            crews.removeIf(c -> c.getId().equals(crewId));
            storage.setItem(CREWS_KEY, serializeCrews(crews));
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    @Override
    public void clear(StorageCallback<Void> callback) {
        try {
            storage.removeItem(TIMING_ENTRIES_KEY);
            storage.removeItem(RACES_KEY);
            storage.removeItem(CREWS_KEY);
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // Helper methods for serialization (simplified JSON-like format)
    private List<TimingEntry> getTimingEntriesList() {
        String data = storage.getItem(TIMING_ENTRIES_KEY);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return deserializeTimingEntries(data);
    }

    private List<Race> getRacesList() {
        String data = storage.getItem(RACES_KEY);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return deserializeRaces(data);
    }

    private List<Crew> getCrewsList() {
        String data = storage.getItem(CREWS_KEY);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return deserializeCrews(data);
    }

    // Simplified serialization (in production, use a proper JSON library)
    private native String serializeTimingEntries(List<TimingEntry> entries) /*-{
        return JSON.stringify(entries);
    }-*/;

    private native List<TimingEntry> deserializeTimingEntries(String data) /*-{
        return JSON.parse(data);
    }-*/;

    private native String serializeRaces(List<Race> races) /*-{
        return JSON.stringify(races);
    }-*/;

    private native List<Race> deserializeRaces(String data) /*-{
        return JSON.parse(data);
    }-*/;

    private native String serializeCrews(List<Crew> crews) /*-{
        return JSON.stringify(crews);
    }-*/;

    private native List<Crew> deserializeCrews(String data) /*-{
        return JSON.parse(data);
    }-*/;
}
