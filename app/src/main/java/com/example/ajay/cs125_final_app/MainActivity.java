package com.example.ajay.cs125_final_app;

import java.util.List;
import java.util.ArrayList;
import com.example.lib.ItemList;
import com.example.lib.Item;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<ItemList> itemLists;
    private ItemList currentList;

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
        if (itemLists.size() > 0) {
            currentList = itemLists.get(0);
        }

        updateListsMenu();
        updateCurrentList();
    }

    private void updateListsMenu() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu listsMenu = navigationView.getMenu();
        listsMenu.removeGroup(R.id.lists_menu_group);

        for (int i = 0; i < itemLists.size(); i++) {
            listsMenu.add(R.id.lists_menu_group, i, 0, itemLists.get(i).getName());
        }
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
            TextView itemTextView = new TextView(this);
            itemTextView.setText(item.getName());
            itemTextView.setPadding(20, 50, 20, 50);
            itemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);

            TableRow tr = new TableRow(this);
            tr.addView(itemTextView);
            tr.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));

            itemTableLayout.addView(tr);
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
            currentList = itemLists.get(id);
            updateCurrentList();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
