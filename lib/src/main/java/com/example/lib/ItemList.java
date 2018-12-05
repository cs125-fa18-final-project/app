package com.example.lib;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;

public class ItemList {
    private String name;
    private List<Item> items;

    public static ItemList itemListFromJSON(String json) {
        return new Gson().fromJson(json, ItemList.class);
    }

    public ItemList(String setName) {
        name = setName;
        items = new ArrayList();
    }

    public String toJSON() {
        return new Gson().toJson(this);
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

    public void removeItem(Item item) {
        items.remove(item);
    }
}
