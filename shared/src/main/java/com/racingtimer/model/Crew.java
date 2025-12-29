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
    private String teamName;
    private String teamName2;
    private String age;
    private String gender;
    private CrewStatus status;

    public enum CrewStatus {
        ACTIVE,
        DNS,    // Did Not Start
        DNF,    // Did Not Finish
        DSQ     // Disqualified
    }

    public Crew() {
        this.status = CrewStatus.ACTIVE;
    }

    public Crew(String id, int bibNumber, String name, String category, String raceId) {
        this.id = id;
        this.bibNumber = bibNumber;
        this.name = name;
        this.category = category;
        this.raceId = raceId;
        this.status = CrewStatus.ACTIVE;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName2() {
        return teamName2;
    }

    public void setTeamName2(String teamName2) {
        this.teamName2 = teamName2;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public CrewStatus getStatus() {
        return status;
    }

    public void setStatus(CrewStatus status) {
        this.status = status;
    }
}
