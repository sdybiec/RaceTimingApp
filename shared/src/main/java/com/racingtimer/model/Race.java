package com.racingtimer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a rowing race in the regatta.
 */
public class Race implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Date scheduledStartTime;
    private Date actualStartTime;
    private List<Crew> crews;
    private RaceStatus status;

    public enum RaceStatus {
        SCHEDULED,
        IN_PROGRESS,
        FINISHED,
        CANCELLED
    }

    public Race() {
        this.crews = new ArrayList<>();
        this.status = RaceStatus.SCHEDULED;
    }

    public Race(String id, String name, Date scheduledStartTime) {
        this();
        this.id = id;
        this.name = name;
        this.scheduledStartTime = scheduledStartTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(Date scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public Date getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public List<Crew> getCrews() {
        return crews;
    }

    public void setCrews(List<Crew> crews) {
        this.crews = crews;
    }

    public RaceStatus getStatus() {
        return status;
    }

    public void setStatus(RaceStatus status) {
        this.status = status;
    }
}
