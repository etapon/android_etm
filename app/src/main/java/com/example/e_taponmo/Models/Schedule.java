package com.example.e_taponmo.Models;

public class Schedule {

    public String day, typeOfWaste, startOfCollection, id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTypeOfWaste() {
        return typeOfWaste;
    }

    public void setTypeOfWaste(String typeOfWaste) {
        this.typeOfWaste = typeOfWaste;
    }

    public String getStartOfCollection() {
        return startOfCollection;
    }

    public void setStartOfCollection(String startOfCollection) {
        this.startOfCollection = startOfCollection;
    }
}
