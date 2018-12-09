package com.example.ajay.cs125_final_app;

import com.example.lib.ItemList;
import com.example.lib.Item;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import android.content.SharedPreferences;
import android.app.Activity;
import android.content.Context;

public class ListManager {
    private static final String SHARED_PREFERENCES_KEY = "lists";

    private static List<ItemList> lists = new ArrayList<>();

    public static List<ItemList> getLists() {
        return lists;
    }

    public static void loadLists(Activity delegate, Context context) {
        SharedPreferences prefs = delegate.getSharedPreferences(delegate.getPackageName(),
                context.MODE_PRIVATE);
        String json = prefs.getString(SHARED_PREFERENCES_KEY, new Gson().toJson(new ArrayList()));
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();

        for (int i = 0; i < array.size(); i++) {
            lists.add(gson.fromJson(array.get(i), ItemList.class));
        }
    }

    public static void saveLists(Activity delegate, Context context) {
        for (ItemList l : lists)
            l.sort();

        SharedPreferences prefs = delegate.getSharedPreferences(delegate.getPackageName(),
                context.MODE_PRIVATE);
        String json = new Gson().toJson(lists);
        prefs.edit().putString(SHARED_PREFERENCES_KEY, json).apply();
    }

    public static void addList(Activity delegate, Context context, ItemList list) {
        lists.add(list);
        saveLists(delegate, context);
    }

    public static void removeList(Activity delegate, Context context, ItemList list) {
        lists.remove(list);
        saveLists(delegate, context);
    }

    public static void addItem(Activity delegate, Context context, ItemList list, Item item) {
        if (list == null || item == null) return;
        list.addItem(item);
        saveLists(delegate, context);
    }

    public static void removeItem(Activity delegate, Context context, ItemList list, Item item) {
        list.removeItem(item);
        saveLists(delegate, context);
    }

    public static ItemList getListWithID(int id) {
        for (ItemList list : lists)
            if (list.getID() == id) return list;

        return null;
    }

    public static Item getItemWithID(int id) {
        for (ItemList list : lists)
            for (Item item : list.getItems())
                if (item.getID() == id) return item;

        return null;
    }
}
