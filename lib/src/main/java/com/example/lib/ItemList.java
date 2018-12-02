package com.example.lib;

import java.util.List;
import java.util.ArrayList;

public class ItemList {
    private String name;
    private List<Item> items;

    public ItemList(String setName) {
        name = setName;
        items = new ArrayList();
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

    public void addItem(Item newItem) {
        items.add(newItem);
    }
}
