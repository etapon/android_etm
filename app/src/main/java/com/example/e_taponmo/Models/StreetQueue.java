package com.example.e_taponmo.Models;

import java.util.LinkedList;
import java.util.Queue;

public class StreetQueue {
    public Queue<String> streetQueue = new LinkedList<>();

    public void addToQueue(String streetName) {
        this.streetQueue.add(streetName);
    }

    public void streetPoll(){
        this.streetQueue.poll();
    }

    public String streetPeek(){
        return this.streetQueue.peek();
    }

}
