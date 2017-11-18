package com.example.juseris.aftercallnote.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Patterns;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.MainAdapter;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.MainDrawerClass;
import com.example.juseris.aftercallnote.Models.CallStatisticsEntity;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.Models.Order;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.ServiceNotificationRemover;
import com.example.juseris.aftercallnote.StateChecker;
import com.example.juseris.aftercallnote.UtilsPackage.Utils;
import com.example.juseris.aftercallnote.XmlHandling;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Dialog prestaDialog;
    android.support.v7.widget.Toolbar toolbar;

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
        checkPermissions(savedInstanceState);
        setTitle(getString(R.string.app_name));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            con.addMyNotes(fixedEmail);
            con.fetchDataFromFirebase(fixedEmail);
            String key = prefs.getString("web_key","");
            String url = prefs.getString("web_url","");
            if(!key.equals("") && !url.equals("")) {
                if(Utils.isNetworkAvailable(context)) {
                    new XmlHandling(this, url, key);
                }
            }
        }

        startService(new Intent(context, StateChecker.class));
        startService(new Intent(context, ServiceNotificationRemover.class));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setUpFabListener();
        toolbar = findViewById(R.id.toolbar);
        setUpSearch();
    }


    private void setUpSearch() {
        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
        searchView = toolbar.findViewById(R.id.search_view);
        final TextView title = toolbar.findViewById(R.id.appName);

        if (searchView != null) {
            searchView.setMaxWidth( Integer.MAX_VALUE );
            LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame);
            ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = 0;
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    title.setVisibility(View.GONE);
                }
            });

            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    title.setVisibility(View.VISIBLE);
                    return false;
                }
            });

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
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mainDrawer != null) {
            mainDrawer.changeEmail();
        }
    }

    private void setUpFabListener() {
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

    private void checkPermissions(Bundle savedInstanceState) {
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
        if(noteList != null) {
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
                case R.id.action_testPresta:
                    testPresta();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    private void testPresta(){
        ArrayList<IGenericItem> a = new ArrayList<>();
        for (IGenericItem note : noteList) {
            if (note instanceof Order) {
                a.add(note);
            }
        }
        listAdapter = new MainAdapter(this, a);
        recyclerView.setAdapter(listAdapter);
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
        }else if (id == R.id.nav_prestashop) {
            prestaDialog = new Dialog(context,R.style.AlertDialogCustom);
            prestaDialog.setContentView(R.layout.ecommerse_layout);
            prestaDialog.setTitle("Enter url and web api key");
            final EditText key = (EditText) prestaDialog.findViewById(R.id.key);
            final EditText url = (EditText) prestaDialog.findViewById(R.id.url);

            Button dialogButton = (Button) prestaDialog.findViewById(R.id.presta_button);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String urlStr = url.getText().toString();
                    String keyStr = key.getText().toString();
                    if(urlStr.length() > 2) {
                        if (urlStr.substring(0, 3).equals("www")) {
                            urlStr = "https://" + urlStr;
                        }
                    }
                    if(!isValidUrl(urlStr)) {
                        Toast.makeText(context, "wrong Url", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        validateKey(keyStr,urlStr);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            });
            String key_str = prefs.getString("web_key","");
            String url_str = prefs.getString("web_url","");
            if(key_str.equals("") && url_str.equals("")) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                double width = getResources().getDisplayMetrics().widthPixels * 0.95;
                prestaDialog.getWindow().setLayout((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
                prestaDialog.show();
            }else{
                Toast.makeText(context, "Prestashop already connected", Toast.LENGTH_SHORT).show();
            }
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

    private boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }
    boolean isValid = false;

    private void validateKey(final String key,final String url) throws ExecutionException, InterruptedException {
        Authentication asyncTask = (Authentication) new Authentication(new Authentication.AsyncResponse(){

            @Override
            public void processFinish(Boolean output) {
                if(output){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null) {
                        prefs.edit().putString("web_key",key).apply();
                        prefs.edit().putString("web_url",url).apply();
                        new XmlHandling(context, url, key);
                        Toast.makeText(context, "Getting prestashop data...", Toast.LENGTH_SHORT).show();
                        prestaDialog.dismiss();
                    }else{
                        Toast.makeText(context, "Please log in and try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, "Wrong web api key or url", Toast.LENGTH_SHORT).show();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new MyTaskParams(key,url));

    }

    private class MyTaskParams {
        String key;
        String url;
        MyTaskParams(String key,String url) {
            this.key = key;
            this.url = url;
        }
    }

    public static class Authentication extends AsyncTask<MyTaskParams, Void, Boolean> {

        public interface AsyncResponse {
            void processFinish(Boolean output);
        }
        public AsyncResponse delegate = null;

        public Authentication(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(MyTaskParams... params) {
            String authToBytes = params[0].key + ":" + "";
            String authBytesString = Base64.encodeToString(authToBytes.getBytes(), Base64.DEFAULT);// I keep it generic
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) new URL(params[0].url)
                        .openConnection();
                urlConnection.setDoOutput(false);
                urlConnection.setRequestProperty("Authorization", "Basic " + authBytesString);

                int statusCode = urlConnection.getResponseCode();
                return statusCode == 200 || statusCode == 203 || statusCode == 206 || statusCode == 226;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isValidKey) {
            super.onPostExecute(isValidKey);
            delegate.processFinish(isValidKey);
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
                    // Google Sign In was successful, authenticate with Firebase
                    Toast.makeText(context, "Logging in...", Toast.LENGTH_SHORT).show();
                    GoogleSignInAccount account = result.getSignInAccount();
                    Uri uri = account.getPhotoUrl();
                    mainDrawer.putImageIntoNavBar(uri);
                    mainDrawer.firebaseAuthWithGoogle(account);
                } else {
                    Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
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

    public ProgressBar dialog;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.sync_refresh, menu);

        return super.onCreateOptionsMenu(menu);
    }

}

