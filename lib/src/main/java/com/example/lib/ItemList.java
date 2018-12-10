package com.example.lib;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ItemList {
    private int id;
    private String name;
    private List<Item> items;
    private String username;

    public ItemList(String setName) {
        name = setName;
        username = "";
        items = new ArrayList();
        id = hashCode();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getID() { return id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void sort() {
        Collections.sort(items);
    }

    public void addItem(Item newItem) {
        items.add(newItem);
        sort();
    }

    public void removeItem(Item item) {
        items.remove(item);
        sort();
    }
}
