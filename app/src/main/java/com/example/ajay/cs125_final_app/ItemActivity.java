package com.example.ajay.cs125_final_app;

import com.example.lib.Item;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.provider.CalendarContract;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class ItemActivity extends AppCompatActivity {

    private EditText itemName;
    private EditText itemDate;
    private Item item;
    private Button calendarButton;
    private Button locationButton;
    private SupportMapFragment mapFragment;
    private boolean locationSet = false;
    private double latitude;
    private double longitude;
    private FusedLocationProviderClient mFusedLocationClient;

    private final int CAL_WRITE_RESULT = 0;
    private Handler calWriteHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == CAL_WRITE_RESULT) {
                Toast.makeText(ItemActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
                checkCalendar();
            }
        });

        locationButton = findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationSet) return;

                item.setLatitude(latitude);
                item.setLongitude(longitude);



            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setBuildingsEnabled(true);

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latlng) {
                        googleMap.clear();

                        MarkerOptions marker = new MarkerOptions().position(latlng).title(item.getName());
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        googleMap.addMarker(marker);
                        float z = googleMap.getCameraPosition().zoom;
                        CameraPosition campos = new CameraPosition.Builder().target(latlng).zoom(z).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos));

                        latitude = latlng.latitude;
                        longitude = latlng.longitude;
                        locationSet = true;
                    }
                });

                //If If permission isn't granted for location, ask for it.

                if (ContextCompat.checkSelfPermission(ItemActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ItemActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            711);
                } else {
                    googleMap.setMyLocationEnabled(true);

                    //If item already has a location just set that to the latlng object
                    if (item.hasLocation()) {
                        LatLng latlng = new LatLng(item.getLatitude(), item.getLongitude());
                        MarkerOptions marker = new MarkerOptions().position(latlng).title(item.getName());
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        googleMap.addMarker(marker);
                        CameraPosition campos = new CameraPosition.Builder().target(latlng).zoom(16).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos));

                        latitude = latlng.latitude;
                        longitude = latlng.longitude;

                        final float[] tempFloatArray = new float[20];

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(delegate, new OnSuccessListener<Location>() {
                                    public void onSuccess(Location value) {
                                        if (value != null) {
                                            Location.distanceBetween(item.getLatitude(), item.getLatitude(), value.getLatitude(), value.getLongitude(), tempFloatArray);
                                            Toast.makeText(delegate, "Approximate Distance to Location: " + (Double.toString(tempFloatArray[0]/1000)), Toast.LENGTH_LONG).show();

                                        }
                                    }});





                    } else {
                        FusedLocationProviderClient client = LocationServices
                                .getFusedLocationProviderClient(delegate);
                        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                                MarkerOptions marker = new MarkerOptions().position(latlng).title(item.getName());
                                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                googleMap.addMarker(marker);
                                CameraPosition campos = new CameraPosition.Builder().target(latlng).zoom(16).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos));

                                latitude = latlng.latitude;
                                longitude = latlng.longitude;


                            }
                        });
                    }
                }
            }
        });
    }

    class CalDataLoader implements Runnable {
        private TextView view;
        public CalDataLoader(TextView textView) {
            view = textView;
        }

        public void run() {
            if (ContextCompat.checkSelfPermission(ItemActivity.this, Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                view.setText("Calendar permissions denied");
                ActivityCompat.requestPermissions(ItemActivity.this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        911);
            } else {
                CalendarProvider calendarProvider = new CalendarProvider(getApplicationContext());
                int nitems = 0;
                List<me.everything.providers.android.calendar.Calendar> cals = calendarProvider.getCalendars().getList();

                SimpleDateFormat comparisonFormat = new SimpleDateFormat("MM/dd/yyyy");
                String itemDateString = itemDate.getText().toString();

                for (me.everything.providers.android.calendar.Calendar cal : cals) {
                    List<Event> events = calendarProvider.getEvents(cal.id).getList();
                    for (Event event : events) {
                        String startdate = comparisonFormat.format(new Date(event.dTStart));
                        String enddate = comparisonFormat.format(new Date(event.dTend));

                        if (startdate.equals(itemDateString) || enddate.equals(itemDateString))
                            nitems++;
                    }
                }

                view.setText(String.format("%d events on the same day", nitems));
            }
        }
    }

    private void checkCalendar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(itemDate.getText().toString());
        TextView view = new TextView(this);

        view.setText("Loading...");
        view.setPadding(50, 50, 50, 50);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);

        builder.setPositiveButton("Add to calendar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addToCalendar();
                dialog.cancel();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(view);
        builder.show();

        Thread dataLoaderThread = new Thread(new CalDataLoader(view));
        dataLoaderThread.start();
    }

    private void addToCalendar() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Calendar");
        final View builderView = addToCalViewBuilder();

        builder.setView(builderView);

        builder.setPositiveButton("Add To Calendar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Spinner spinner = builderView.findViewById(R.id.calendar_options_spinner);
                String calName = (String) spinner.getSelectedItem();
                Thread writerThread = new Thread(new CalendarWriter(calName));
                writerThread.start();
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

    class CalendarWriter implements Runnable {
        private String calName;

        public CalendarWriter(String cn) {
            calName = cn;
        }

        public void run() {
            if (ContextCompat.checkSelfPermission(ItemActivity.this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                Message msg = new Message();
                msg.obj = "Calendar permissions denied";
                msg.what = CAL_WRITE_RESULT;
                calWriteHandler.sendMessage(msg);
                ActivityCompat.requestPermissions(ItemActivity.this,
                        new String[]{Manifest.permission.WRITE_CALENDAR},
                        807);
            } else {
                CalendarProvider provider = new CalendarProvider(ItemActivity.this);
                List<me.everything.providers.android.calendar.Calendar> cals = provider.getCalendars().getList();
                me.everything.providers.android.calendar.Calendar theCal = null;

                for (me.everything.providers.android.calendar.Calendar cal : cals) {
                    if (cal.displayName.equals(calName)) {
                        theCal = cal;
                        break;
                    }
                }

                if (theCal == null) return;

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date theDate = null;
                try {
                    theDate = sdf.parse(itemDate.getText().toString());
                } catch (Exception e) {
                    theDate = new Date();
                }

                long calID = theCal.id;
                long startMillis = theDate.getTime() + (86400 * 500);
                long endMillis = startMillis + 60000;

                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE,
                        String.format("Quicklist: %s", itemName.getText().toString()));
                values.put(CalendarContract.Events.DESCRIPTION, String.format("Quicklist: %s", item.getName()));
                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getDisplayName());
                cr.insert(CalendarContract.Events.CONTENT_URI, values);

                Message msg = new Message();
                msg.what = CAL_WRITE_RESULT;
                msg.obj = String.format("'%s' added to '%s'", itemName.getText().toString(), calName);
                calWriteHandler.sendMessage(msg);
            }
        }
    }

    private View addToCalViewBuilder() {
        View theView = LayoutInflater.from(this).inflate(R.layout.add_to_calendar_layout, null);
        Spinner theSpinner = theView.findViewById(R.id.calendar_options_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, new String[]{ "Loading..." });
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        theSpinner.setAdapter(adapter);

        Thread optionsLoaderThread = new Thread(new CalendarOptionsLoader(theSpinner));
        optionsLoaderThread.start();

        return theView;
    }

    class CalendarOptionsLoader implements Runnable {
        private Spinner spinner;
        public CalendarOptionsLoader(Spinner mSpinner) {
            spinner = mSpinner;
        }

        public void run() {
            if (ContextCompat.checkSelfPermission(ItemActivity.this, Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ArrayAdapter<String> aa = new ArrayAdapter<>(ItemActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, new String[]{
                                "Calendar permissions denied"
                });
                aa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinner.setAdapter(aa);

                ActivityCompat.requestPermissions(ItemActivity.this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        911);
            } else {
                CalendarProvider provider = new CalendarProvider(ItemActivity.this);
                List<me.everything.providers.android.calendar.Calendar> cals = provider.getCalendars().getList();
                List<String> calNames = new ArrayList<>();

                for (me.everything.providers.android.calendar.Calendar cal : cals)
                    calNames.add(cal.displayName);

                ArrayAdapter<String> aa = new ArrayAdapter<>(ItemActivity.this,
                        R.layout.support_simple_spinner_dropdown_item, calNames);
                aa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinner.setAdapter(aa);
            }
        }
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
