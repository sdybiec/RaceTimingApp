package com.racingtimer.storage;

import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;

import java.util.List;

/**
 * Interface for offline storage of timing data.
 * Implementations: LocalStorage for web, SQLite for Android.
 */
public interface TimingStorage {

    // Timing Entries
    void saveTimingEntry(TimingEntry entry, StorageCallback<TimingEntry> callback);
    void getTimingEntries(String raceId, StorageCallback<List<TimingEntry>> callback);
    void getUnsyncedTimingEntries(StorageCallback<List<TimingEntry>> callback);
    void deleteTimingEntry(String id, StorageCallback<Void> callback);

    // Races
    void saveRace(Race race, StorageCallback<Race> callback);
    void getRace(String raceId, StorageCallback<Race> callback);
    void getAllRaces(StorageCallback<List<Race>> callback);
    void deleteRace(String raceId, StorageCallback<Void> callback);

    // Crews
    void saveCrew(Crew crew, StorageCallback<Crew> callback);
    void getCrew(String crewId, StorageCallback<Crew> callback);
    void getCrewByBibNumber(int bibNumber, String raceId, StorageCallback<Crew> callback);
    void getCrewsForRace(String raceId, StorageCallback<List<Crew>> callback);
    void deleteCrew(String crewId, StorageCallback<Void> callback);

    // Utility
    void clear(StorageCallback<Void> callback);
}
