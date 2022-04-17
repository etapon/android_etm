package com.example.e_taponmo.Models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Schedule {
    public String day, typeOfWaste, startOfCollection, id;
    ArrayList<String> queue = new ArrayList<String>();
    public Queue<String> streetQueue = new LinkedList<>();

    public void addToQueue (String street){
        this.queue.add(street);
    }

    public String peekQueue (){
        return this.streetQueue.peek();
    }

    public ArrayList<String> getQueue() {
        return queue;
    }

//    public String peekQueue (){
//        return queue.
//    }

    public void setQueue(ArrayList<String> queue) {
        this.queue = queue;
    }

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
