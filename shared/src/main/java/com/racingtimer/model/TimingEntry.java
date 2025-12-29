package com.racingtimer.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a timing entry captured during a race.
 * Each entry records when a crew crosses a timing milestone.
 */
public class TimingEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private int sequenceNumber;
    private Date timestamp;
    private Integer bibNumber;
    private String crewId;
    private String crewName;
    private String category;
    private String raceId;
    private long raceClockMillis;
    private boolean synced;

    public TimingEntry() {
    }

    public TimingEntry(int sequenceNumber, Date timestamp) {
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.synced = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getBibNumber() {
        return bibNumber;
    }

    public void setBibNumber(Integer bibNumber) {
        this.bibNumber = bibNumber;
    }

    public String getCrewId() {
        return crewId;
    }

    public void setCrewId(String crewId) {
        this.crewId = crewId;
    }

    public String getCrewName() {
        return crewName;
    }

    public void setCrewName(String crewName) {
        this.crewName = crewName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public long getRaceClockMillis() {
        return raceClockMillis;
    }

    public void setRaceClockMillis(long raceClockMillis) {
        this.raceClockMillis = raceClockMillis;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    /**
     * Format race clock time as MM:SS.S
     */
    public String getFormattedRaceTime() {
        long totalSeconds = raceClockMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long tenths = (raceClockMillis % 1000) / 100;
        return String.format("%d:%02d.%d", minutes, seconds, tenths);
    }
}
