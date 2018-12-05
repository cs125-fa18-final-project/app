package com.example.ajay.cs125_final_app;

import com.example.lib.Item;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class ItemActivity extends AppCompatActivity {

    private EditText itemName;
    private EditText itemDate;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        setTitle("Edit Item...");

        itemDate = findViewById(R.id.item_date);
        itemDate.setShowSoftInputOnFocus(false);
        final ItemActivity delegate = this;
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
    }

    private void pickDate() {
        Calendar cal = Calendar.getInstance();

        this.hideKeyboard(this);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                itemDate.setText(month + "/" + dayOfMonth + "/" + year);
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
