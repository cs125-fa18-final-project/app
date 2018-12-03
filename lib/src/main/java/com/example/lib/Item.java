package com.example.lib;

public class Item {
    private static int globalID = 0;

    private String name;
    private boolean completed;
    private int id;

    public Item(String setName) {
        name = setName;
        id = globalID++;
        completed = false;
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

    public int getID() {
        return id;
    }
}
