package com.example.ajay.cs125_final_app;

import java.util.List;
import java.util.ArrayList;
import com.example.lib.ItemList;
import com.example.lib.Item;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String SHARED_PREFERENCES_KEY = "lists";

    private List<ItemList> itemLists;
    private ItemList currentList;
    private boolean isCurrentlyEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        itemLists = new ArrayList();
        loadLists();
        if (itemLists.size() > 0) {
            currentList = itemLists.get(0);
        }

        isCurrentlyEditing = false;

        updateListsMenu();
        updateCurrentList();
    }

    private void saveLists() {
        SharedPreferences prefs = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        String json = new Gson().toJson(itemLists);
        prefs.edit().putString(SHARED_PREFERENCES_KEY, json).apply();
    }

    private void loadLists() {
        SharedPreferences prefs = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        String json = prefs.getString(SHARED_PREFERENCES_KEY, new Gson().toJson(new ArrayList()));
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();

        for (int i = 0; i < array.size(); i++) {
            itemLists.add(gson.fromJson(array.get(i), ItemList.class));
        }
    }

    public ItemList getListWithID(int id) {
        for (ItemList list : itemLists) {
            if (list.hashCode() == id) return list;
        }

        return null;
    }

    private void updateListsMenu() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu listsMenu = navigationView.getMenu();
        listsMenu.removeGroup(R.id.lists_menu_group);

        for (ItemList list : itemLists) {
            listsMenu.add(R.id.lists_menu_group, list.hashCode(), 0, list.getName());
        }
    }

    private TableRow generateTableRowForItem(final Item item) {
        final int completedColor = Color.argb(150, 150, 200, 150);
        final int notCompletedColor = Color.TRANSPARENT;
        final int deleteButtonColor = Color.argb(255, 200, 20, 20);

        final TableRow tr = new TableRow(this);

        final TextView itemTextView = new TextView(this);
        TableRow.LayoutParams tvlp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tvlp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        tvlp.column = 1;
        itemTextView.setGravity(Gravity.LEFT);
        itemTextView.setId(item.hashCode());
        itemTextView.setText(item.getName());
        itemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        itemTextView.setLayoutParams(tvlp);

        CheckBox itemCheckBox = new CheckBox(this);
        TableRow.LayoutParams cblp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        cblp.gravity = Gravity.CENTER_VERTICAL;
        cblp.column = 1;
        itemCheckBox.setId(item.hashCode());
        itemCheckBox.setGravity(Gravity.CENTER_VERTICAL);
        itemCheckBox.setPadding(0, 80, 0, 0);
        itemCheckBox.setChecked(item.isCompleted());
        itemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setCompleted(!item.isCompleted());
                tr.setBackgroundColor(item.isCompleted() ? completedColor : notCompletedColor);
                saveLists();
            }
        });

        Button itemDeleteButton = new Button(this);
        TableRow.LayoutParams idbLayoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        idbLayoutParams.gravity = Gravity.RIGHT;
        idbLayoutParams.column = 2;
        itemDeleteButton.setText("-");
        itemDeleteButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        itemDeleteButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
        itemDeleteButton.setLayoutParams(idbLayoutParams);
        itemDeleteButton.setBackgroundColor(Color.TRANSPARENT);
        itemDeleteButton.setTextColor(deleteButtonColor);
        itemDeleteButton.setId(item.hashCode());
        itemDeleteButton.setVisibility(isCurrentlyEditing ? View.VISIBLE : View.INVISIBLE);
        itemDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentList.removeItem(item);
                updateCurrentList();
                saveLists();
            }
        });

        tr.setBackgroundColor(item.isCompleted() ? completedColor : notCompletedColor);
        tr.setPadding(20, 10, 20, 10);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tr.setLayoutParams(trlp);

        tr.addView(itemCheckBox);
        tr.addView(itemTextView);
        tr.addView(itemDeleteButton);

        final MainActivity delegate = this;
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editItemIntent = new Intent(delegate, ItemActivity.class);
                editItemIntent.putExtra("id", item.hashCode());
                startActivity(editItemIntent);
            }
        });

        return tr;
    }

    private void updateCurrentList() {
        TableLayout itemTableLayout = findViewById(R.id.items_table_layout);
        itemTableLayout.removeAllViews();

        if (currentList == null) {
            setTitle("Select List...");
            return;
        }

        setTitle(currentList.getName());

        for (Item item : currentList.getItems()) {
            itemTableLayout.addView(generateTableRowForItem(item));
        }
    }

    private void addList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add List...");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String listName = input.getText().toString();
                currentList = new ItemList(listName);
                itemLists.add(currentList);
                updateListsMenu();
                updateCurrentList();
                saveLists();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item...");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = input.getText().toString();
                Item newItem = new Item(itemName);

                if (currentList != null)
                    currentList.addItem(newItem);

                updateCurrentList();
                saveLists();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_list) {
            itemLists.remove(currentList);
            currentList = null;
            if (itemLists.size() > 0) {
                currentList = itemLists.get(0);
            }

            updateCurrentList();
            updateListsMenu();
            saveLists();
        }

        if (id == R.id.edit_list) {
            isCurrentlyEditing = !isCurrentlyEditing;

            if (isCurrentlyEditing) {
                item.setTitle("Finish Editing List");
            } else {
                item.setTitle("Edit List...");
            }

            updateCurrentList();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_item) {
            addList();
        } else {
            currentList = getListWithID(id);
            updateCurrentList();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
