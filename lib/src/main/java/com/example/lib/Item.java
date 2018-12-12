package com.example.lib;

import java.util.Date;
import java.util.Calendar;

public class Item implements Comparable<Item> {
    private int id;
    private Date date;
    private String name;
    private boolean completed;
    private double latitude;
    private double longitude;
    private boolean hasLocation;
    private int daysTillToday;

    public Item(String setName) {
        name = setName;
        completed = false;
        hasLocation = false;
        longitude = 0;
        latitude = 0;
        date = new Date();
        id = hashCode();
    }

    public int compareTo(Item item) {
        if (item == null) return 1;
        if (completed && !item.isCompleted()) return 1;
        if (!completed && item.isCompleted()) return -1;

        return date.compareTo(item.date);
    }



    public boolean hasLocation() {
        return hasLocation;
    }

    public void setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        this.hasLocation = true;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        this.hasLocation = true;
    }

    public int getID() { return id; }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
