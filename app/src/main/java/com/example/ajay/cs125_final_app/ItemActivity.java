package com.example.ajay.cs125_final_app;

import com.example.lib.Item;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class ItemActivity extends AppCompatActivity {

    private EditText itemName;
    private EditText itemDate;
    private Item item;
    private Button calendarButton;
    private Button locationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        final ItemActivity delegate = this;

        setTitle("Edit Item...");

        int id = getIntent().getIntExtra("id", -1);
        item = ListManager.getItemWithID(id);
        if (item == null) {
            item = new Item("");
        }

        itemName = findViewById(R.id.item_name);
        itemName.setText(item.getName());

        itemDate = findViewById(R.id.item_date);
        itemDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(item.getDate()));
        itemDate.setShowSoftInputOnFocus(false);
        itemDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) delegate.pickDate();
            }
        });
        itemDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.pickDate();
            }
        });

        calendarButton = findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(delegate, "Check calendar", Toast.LENGTH_SHORT).show();
            }
        });

        locationButton = findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(delegate, "Set location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        final ItemActivity delegate = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save changes?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item.setName(itemName.getText().toString());
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    item.setDate(sdf.parse(itemDate.getText().toString()));
                } catch (Exception e) {
                    Log.e("ItemActivity", "Invalid date text");
                }

                ListManager.saveLists(delegate, getApplicationContext());
                delegate.goBack();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delegate.goBack();
            }
        });

        builder.show();
    }

    private void goBack() {
        super.onBackPressed();
    }

    private void pickDate() {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(new SimpleDateFormat("MM/dd/yyyy").parse(itemDate.getText().toString()));
        } catch (Exception e) {}

        this.hideKeyboard(this);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                itemDate.setText(month + 1 + "/" + dayOfMonth + "/" + year);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
