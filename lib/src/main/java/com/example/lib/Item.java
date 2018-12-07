package com.example.lib;

import java.util.Date;

public class Item implements Comparable<Item> {
    private int id;
    private Date date;
    private String name;
    private boolean completed;

    public Item(String setName) {
        name = setName;
        completed = false;
        date = new Date();
        id = hashCode();
    }

    public int compareTo(Item item) {
        if (item == null) return 1;
        if (completed && !item.isCompleted()) return 1;
        if (!completed && item.isCompleted()) return -1;

        return date.compareTo(item.date);
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
