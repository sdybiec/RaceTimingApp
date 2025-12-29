package com.racingtimer.service;

import com.racingtimer.model.Crew;
import com.racingtimer.model.Race;
import com.racingtimer.model.TimingEntry;
import com.racingtimer.storage.StorageCallback;
import com.racingtimer.storage.TimingStorage;

import java.util.Date;
import java.util.UUID;

/**
 * Business logic service for timing operations.
 */
public class TimingService {

    private final TimingStorage storage;
    private Race currentRace;
    private Date raceStartTime;
    private int sequenceCounter = 0;

    public TimingService(TimingStorage storage) {
        this.storage = storage;
    }

    /**
     * Start timing for a race.
     */
    public void startRace(Race race) {
        this.currentRace = race;
        this.raceStartTime = new Date();
        this.sequenceCounter = 0;

        race.setActualStartTime(raceStartTime);
        race.setStatus(Race.RaceStatus.IN_PROGRESS);

        storage.saveRace(race, new StorageCallback<Race>() {
            @Override
            public void onSuccess(Race result) {
                // Race started successfully
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    /**
     * Record a timestamp when timer button is tapped.
     */
    public void recordTimestamp(final StorageCallback<TimingEntry> callback) {
        final Date timestamp = new Date();
        sequenceCounter++;

        final TimingEntry entry = new TimingEntry(sequenceCounter, timestamp);
        entry.setId(UUID.randomUUID().toString());

        if (currentRace != null) {
            entry.setRaceId(currentRace.getId());
            if (raceStartTime != null) {
                long raceClockMillis = timestamp.getTime() - raceStartTime.getTime();
                entry.setRaceClockMillis(raceClockMillis);
            }
        }

        storage.saveTimingEntry(entry, callback);
    }

    /**
     * Assign a bib number to a timing entry.
     */
    public void assignBibNumber(final String entryId, final int bibNumber, final StorageCallback<TimingEntry> callback) {
        if (currentRace == null) {
            callback.onError("No active race");
            return;
        }

        // Look up crew by bib number
        storage.getCrewByBibNumber(bibNumber, currentRace.getId(), new StorageCallback<Crew>() {
            @Override
            public void onSuccess(Crew crew) {
                // Update timing entry with crew information
                storage.getTimingEntries(currentRace.getId(), new StorageCallback<java.util.List<TimingEntry>>() {
                    @Override
                    public void onSuccess(java.util.List<TimingEntry> entries) {
                        for (TimingEntry entry : entries) {
                            if (entry.getId().equals(entryId)) {
                                entry.setBibNumber(bibNumber);
                                if (crew != null) {
                                    entry.setCrewId(crew.getId());
                                    entry.setCrewName(crew.getName());
                                    entry.setCategory(crew.getCategory());
                                }
                                storage.saveTimingEntry(entry, callback);
                                return;
                            }
                        }
                        callback.onError("Timing entry not found");
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Finish the current race.
     */
    public void finishRace() {
        if (currentRace != null) {
            currentRace.setStatus(Race.RaceStatus.FINISHED);
            storage.saveRace(currentRace, new StorageCallback<Race>() {
                @Override
                public void onSuccess(Race result) {
                    currentRace = null;
                    raceStartTime = null;
                    sequenceCounter = 0;
                }

                @Override
                public void onError(String error) {
                    // Handle error
                }
            });
        }
    }

    public Race getCurrentRace() {
        return currentRace;
    }

    public void setCurrentRace(Race race) {
        this.currentRace = race;
        this.raceStartTime = race.getActualStartTime();
    }
}
