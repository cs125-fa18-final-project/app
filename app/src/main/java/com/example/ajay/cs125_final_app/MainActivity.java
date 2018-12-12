package com.example.ajay.cs125_final_app;

import com.example.lib.ItemList;
import com.example.lib.Item;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import java.util.Calendar;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;

import com.google.android.gms.common.api.ApiException;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
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
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.squareup.picasso.Transformation;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final MainActivity delegate = this;
    private ItemList currentList;
    private Item currentlyRemoved;
    private GoogleSignInClient signInClient;
    private GoogleSignInAccount account;
    private String username = "";
    private final int RC_SIGN_IN = 111;

    private final int LOAD_LISTS_MESSAGE = 0;
    private final int LOAD_SIGN_IN = 1;
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_LISTS_MESSAGE:
                    updateListsMenu();
                    updateCurrentList();
                    break;
                case LOAD_SIGN_IN:
                    updateSignInUI();
            }
        }
    };

    class ListDataLoader implements Runnable {
        public void run() {
            ListManager.loadLists(MainActivity.this, getApplicationContext());
            for (ItemList list : ListManager.getLists()) {
                if (list.getUsername().equals(username)) {
                    currentList = list;
                    break;
                }
            }

            handler.sendEmptyMessage(LOAD_LISTS_MESSAGE);
        }
    }

    class GoogleClientInitializer implements Runnable {
        public void run() {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail().requestProfile().build();
            signInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        }
    }

    class GoogleClientSignin implements Runnable {
        public void run() {
            account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
            handler.sendEmptyMessage(LOAD_SIGN_IN);
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) { return true; }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float vx, float vy) {
            if (Math.abs(vx) >= 2 * Math.abs(vy)) {
                ListManager.removeItem(delegate, getApplicationContext(), currentList, currentlyRemoved);
                updateCurrentList();
                currentlyRemoved = null;

                return true;
            }

            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        setTitle("Loading...");

        Thread listLoaderThread = new Thread(new ListDataLoader());
        listLoaderThread.start();

        Thread clientInitializerThread = new Thread(new GoogleClientInitializer());
        clientInitializerThread.start();

        ImageView signin = navigationView.getHeaderView(0).findViewById(R.id.signIn);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInClient.signOut();
                signIn();
            }
        });

        Intent serviceIntent = new Intent(this, ItemAlertService.class);
        startService(serviceIntent);
    }

    private void signIn() {
        Intent signinintent = signInClient.getSignInIntent();
        startActivityForResult(signinintent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);
                updateSignInUI();
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Thread signinThread = new Thread(new GoogleClientSignin());
        signinThread.start();
    }

    class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    private void updateSignInUI() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageView accountImage = navigationView.getHeaderView(0).findViewById(R.id.signIn);
        TextView accountEmail = navigationView.getHeaderView(0).findViewById(R.id.accountEmail);
        TextView accountUsername = navigationView.getHeaderView(0).findViewById(R.id.accountUsername);

        if (account == null)
            return;

        Uri imageUri = account.getPhotoUrl();
        Log.d("imageUri", String.format("%s\n", imageUri));
        Picasso.get().load(imageUri).fit().transform(new CircleTransform())
                .placeholder(R.mipmap.ic_launcher_round).into(accountImage);
        accountEmail.setText(account.getEmail());
        accountUsername.setText(account.getDisplayName());

        username = account.getEmail();
        currentList = null;
        for (ItemList list : ListManager.getLists()) {
            if (list.getUsername().equals(username)) {
                currentList = list;
                break;
            }
        }
        updateListsMenu();
        updateCurrentList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateListsMenu();
        updateCurrentList();
    }

    private void updateListsMenu() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu listsMenu = navigationView.getMenu();
        listsMenu.removeGroup(R.id.lists_menu_group);

        for (ItemList list : ListManager.getLists()) {
            if (list.getUsername().equals(username))
                listsMenu.add(R.id.lists_menu_group, list.getID(), 0, list.getName());
        }
    }

    public int colorizeItem(Item item) {
        final int completedColor = Color.argb(150, 220, 220, 220);
        double ming = 80;
        double maxg = 240;
        double g;
        double incompleteSize = 0;

        //depending on time of day, change color scheme.
        for (Item i : currentList.getItems())
            if (! i.isCompleted()) incompleteSize++;

        double proximityQuotient = (double) (currentList.getItems().indexOf(item)) / incompleteSize;
        g = ming + ((maxg - ming) * proximityQuotient);

        final int notCompletedColor = Color.argb(255, 255, (int)g, 50);

        //if the date of the item is nearby, then change the color to bright red
        long itemTime = TimeUnit.MILLISECONDS.toDays(item.getDate().getTime());
        long currentTime = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTime().getTime());
        if (Math.abs((currentTime - itemTime)) < 1 && !item.isCompleted()) {

            int nearbyColor = Color.argb(255,255,255,255);

            return nearbyColor;

        }


        return item.isCompleted() ? completedColor : notCompletedColor;
    }

    private TableRow generateTableRowForItem(final Item item) {
        final TableRow tr = new TableRow(this);

        final TextView itemTextView = new TextView(this);
        TableRow.LayoutParams tvlp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tvlp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        tvlp.column = 1;
        itemTextView.setClickable(true);
        itemTextView.setGravity(Gravity.LEFT);
        itemTextView.setId(item.getID());

        if (colorizeItem(item) == Color.argb(255,255,255,255)) {
            itemTextView.setText("<24 hours to complete: " + item.getName());
            ObjectAnimator animate = ObjectAnimator.ofFloat(itemTextView,"translationX", 50f);
            animate.setDuration(500);
            animate.start();



        } else {
            itemTextView.setText(item.getName());
        }



        itemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        itemTextView.setLayoutParams(tvlp);

        CheckBox itemCheckBox = new CheckBox(this);
        TableRow.LayoutParams cblp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        cblp.gravity = Gravity.CENTER_VERTICAL;
        cblp.column = 1;
        itemCheckBox.setId(item.getID());
        itemCheckBox.setGravity(Gravity.CENTER_VERTICAL);
        itemCheckBox.setPadding(0, 80, 0, 0);
        itemCheckBox.setChecked(item.isCompleted());
        itemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setCompleted(!item.isCompleted());
                ListManager.saveLists(delegate, getApplicationContext());
                updateCurrentList();
            }
        });

        final int color = colorizeItem(item);

        tr.setBackgroundColor(color);
        tr.setPadding(20, 10, 20, 10);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tr.setLayoutParams(trlp);

        tr.addView(itemCheckBox);
        tr.addView(itemTextView);

        final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());

        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int r = Color.red(color) / 2;
                    int g = Color.green(color) / 2;
                    int b = Color.blue(color) / 2;
                    tr.setBackgroundColor(Color.argb(255, r, g, b));
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    return true;
                }

                currentlyRemoved = item;
                boolean swiped = gestureDetector.onTouchEvent(event);
                if (swiped) findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

                if (!swiped && event.getAction() == MotionEvent.ACTION_UP) {
                    tr.setBackgroundColor(color);
                    currentlyRemoved = null;
                    Intent editItemIntent = new Intent(delegate, ItemActivity.class);
                    editItemIntent.putExtra("id", item.getID());
                    startActivity(editItemIntent);
                }

                return true;
            }
        };

        tr.setOnTouchListener(listener);
        itemTextView.setOnTouchListener(listener);

        return tr;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
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
                currentList.setUsername(username);
                ListManager.addList(delegate, getApplicationContext(), currentList);
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

                ListManager.addItem(delegate, getApplicationContext(), currentList, newItem);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        if (id == R.id.delete_list) {
            ListManager.removeList(this, getApplicationContext(), currentList);
            currentList = null;
            if (ListManager.getLists().size() > 0) {
                currentList = ListManager.getLists().get(0);
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
            currentList = ListManager.getListWithID(id);
            updateCurrentList();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
