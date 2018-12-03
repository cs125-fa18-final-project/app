package com.example.lib;

import java.util.List;
import java.util.ArrayList;

public class ItemList {
    private static int globalID = 0;

    private String name;
    private List<Item> items;
    private int id;

    public ItemList(String setName) {
        name = setName;
        items = new ArrayList();
        id = globalID++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getItems() {
        return items;
    }

    public int getID() { return id; }

    public void addItem(Item newItem) {
        items.add(newItem);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }
}
