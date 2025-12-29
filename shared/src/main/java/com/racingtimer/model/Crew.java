package com.racingtimer.model;

import java.io.Serializable;

/**
 * Represents a crew competing in a rowing race.
 */
public class Crew implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private int bibNumber;
    private String name;
    private String category;
    private String raceId;

    public Crew() {
    }

    public Crew(String id, int bibNumber, String name, String category, String raceId) {
        this.id = id;
        this.bibNumber = bibNumber;
        this.name = name;
        this.category = category;
        this.raceId = raceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBibNumber() {
        return bibNumber;
    }

    public void setBibNumber(int bibNumber) {
        this.bibNumber = bibNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
