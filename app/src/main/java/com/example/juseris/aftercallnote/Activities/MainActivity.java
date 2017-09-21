package com.example.juseris.aftercallnote.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.MainAdapter;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.MainDrawerClass;
import com.example.juseris.aftercallnote.Models.CallStatisticsEntity;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.PhoneCallReceiver;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.Utils;
import com.example.juseris.aftercallnote.XmlHandling;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , GoogleApiClient.OnConnectionFailedListener {
    final Context context = this;
    private RecyclerView recyclerView;
    private ArrayList<IGenericItem> noteList = null;
    private MainAdapter listAdapter = null;
    private Database db = null;
    protected DrawerLayout drawer;
    private final int IMG_REQUEST_CODE = 0x5654;
    private SearchView searchView = null;
    private static final int RC_SIGN_IN = 33;
    private static final String TAG = "LOG IN";
    FirebaseConnection con;
    private boolean hasInitialized = false;
    FloatingActionButton myFab;
    static boolean active = false;
    DatabaseReference myRef;
    private SharedPreferences prefs;
    private MainDrawerClass mainDrawer;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            } else {
                Toast.makeText(MainActivity.this, "Permissions denied, some app features may not work", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23 && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        active = true;
        myRef = Utils.getDatabase().getReference();
        con = new FirebaseConnection(context, MainActivity.this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!prefs.getBoolean("outgoingCheckBox", false)) {
            prefs.edit().putBoolean("outgoingCheckBox", false).apply();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALL_LOG
            };
            if (!hasPermissions(context, permissions)) {
                if (savedInstanceState == null) {
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
                }
            } else {
                initialize();
            }
        } else {
            initialize();
        }
        setTitle("After Call Notes");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        myFab = (FloatingActionButton) findViewById(R.id.myFAB);
        myFab.bringToFront();
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prefs.edit().putBoolean("haveToChooseContact", true).apply();
                Intent i = new Intent(MainActivity.this, ActivityPopupAfter.class);
                prefs.edit().putString("callTime", "").apply();
                startActivity(i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        active = false;
        super.onDestroy();
    }

    private void initialize() {
        hasInitialized = true;
        db = new Database(this.context);
        recyclerView = (RecyclerView) findViewById(R.id.ac_main_listView);
        mainDrawer = new MainDrawerClass(context, MainActivity.this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            String syncOccured = prefs.getString("SyncOccured", "");
            if (syncOccured.equals("")) {
                con.addMyNotes(fixedEmail);
            }
            con.fetchDataFromFirebase(fixedEmail);
            new XmlHandling(context);
        }
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = "";
                if (user != null) {
                    email = user.getEmail();
                    String fixedEmail = email.replace(".", ",");
                    con.addDataToFirebase(fixedEmail);
                    String syncOccured = prefs.getString("SyncOccured", "");
                    if (syncOccured.equals("")) {
                        con.addMyNotes(fixedEmail);
                    }
                }
                refreshList();
                return true;
            case R.id.action_show_all:
                showAllNotes();
                return true;
            case R.id.action_show_mine:
                showMyNotesFirst();
                return true;
            case R.id.action_showSynced:
                showSyncedNotesFirst();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSyncedNotesFirst() {
        ArrayList<IGenericItem> a = new ArrayList<>();
        for (IGenericItem note : noteList) {
            if (note instanceof ClassNote) {
                if (((ClassNote) note).isSynced() == 1) {
                    a.add(note);
                }
            }
        }
        listAdapter = new MainAdapter(this, a);
        recyclerView.setAdapter(listAdapter);
    }

    private void showMyNotesFirst() {
        ArrayList<IGenericItem> a = new ArrayList<>();
        for (IGenericItem note : noteList) {
            if (note instanceof ClassNote) {
                if (((ClassNote) note).isSynced() == 0) {
                    a.add(note);
                }
            }
        }
        listAdapter = new MainAdapter(this, a);
        recyclerView.setAdapter(listAdapter);
    }

    private void showAllNotes() {
        refreshList();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(false);
        if (id == R.id.nav_about) {
            Intent i = new Intent(this, WelcomeActivity.class);
            i.putExtra("about", true);
            startActivity(i);
        } else if (id == R.id.nav_exportData) {
            mainDrawer.showExportDialog();
        } else if (id == R.id.nav_scheduledEvents) {
            startActivity(new Intent(this, RemindersList.class));
        } else if (id == R.id.nav_selectWeekDays) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_viewStatistics) {
            CallStatisticsEntity cse = db.getStatistics();
            float total = cse.getIncomingCallCount() + cse.getOutgoingCallCount()
                    + cse.getRemindersAddedCount() + cse.getTypedNoteCount();
            if (total != 0) {
                Intent i = new Intent(this, AllStatisticsView.class);
                startActivity(i);
            } else {
                Toast.makeText(context, "No data yet", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_allIncomingCalls) {
            Intent i = new Intent(this, AllCallsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_acc_signIn) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasInitialized) {
            refreshList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    dialog = ProgressDialog.show(context, "Signing in", "Please wait...", true);
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    Uri uri = account.getPhotoUrl();
                    mainDrawer.putImageIntoNavBar(uri);
                    mainDrawer.firebaseAuthWithGoogle(account);
                } else {
                    Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
                    // Google Sign In failed, update UI appropriately
                    // ...
                }
            }
        }

        if (requestCode == 0xe420) {
            if (searchView != null) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                    searchView.onActionViewCollapsed();
                }
            }
        }
        if (requestCode == IMG_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                mainDrawer.putImageIntoNavBar(selectedImage);
            }
        }
    }

    LinearLayoutManager mLayoutManager;

    public Date parseOrReturnNull(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private ArrayList<IGenericItem> sortNotesByDate(ArrayList<IGenericItem> list) {
        Collections.sort(list, new Comparator<IGenericItem>() {
            @Override
            public int compare(IGenericItem a, IGenericItem b) {
                Date date2 = b.getDateObject();//parseOrReturnNull(((ClassNote) b).getDateString());
                Date date1 = a.getDateObject();// parseOrReturnNull(((ClassNote) a).getDateString());
                if (date1 == null) {
                    if (date2 == null) {
                        return 0;
                    }
                    return 1;
                }
                if (date2 == null) {
                    return -1;
                }
                return date2.compareTo(date1);
            }
        });
        Collections.reverse(list);
        return list;
    }
    private Parcelable recyclerViewState;

    public void refreshList() {
        noteList = db.getData();
        noteList.addAll(db.getSyncedData());
        noteList = Utils.getSortedList(noteList);// sortNotesByDate(parseToDates(noteList));
        Collections.reverse(noteList);
        ArrayList<IGenericItem> newItems = db.getNewPrestashopData();
        newItems = Utils.getSortedPrestaList(newItems);// sortNotesByDate(parseToDates(newItems));
         noteList.addAll(newItems);

        ArrayList<IGenericItem> items = db.getPrestashopData();
        //Collections.reverse(items);
        noteList.addAll(items);
        listAdapter = new MainAdapter(this, noteList);
        //listAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(listAdapter);
        recyclerView.setFocusable(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        addOnScrollListener();
    }

    private void addOnScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && myFab.isShown()) {
                    myFab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    myFab.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public ProgressDialog dialog;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard, menu);
        menuInflater.inflate(R.menu.sync_refresh, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true; // handled
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    listAdapter.getFilter().filter(newText);
                    listAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(listAdapter);
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

}

