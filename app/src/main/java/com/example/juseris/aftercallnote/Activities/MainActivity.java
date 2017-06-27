package com.example.juseris.aftercallnote.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.NavigationViewAdapter;
import com.example.juseris.aftercallnote.DataExportation;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.Models.CallStatisticsEntity;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.DataForSyncingModel;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.XmlHandling;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
                    ,GoogleApiClient.OnConnectionFailedListener{
    final Context context = this;
    private RecyclerView ui_listView;
    private ArrayList<ClassNote> noteList = null;
    private CustomAdapter listAdapter = null;
    private int itemIndex;
    private ClassSettings Settings = null;
    private Database db = null;
    protected DrawerLayout drawer;
    private PopupMenu popup;
    private final int IMG_REQUEST_CODE = 0x5654;
    private ImageButton selector;
    private final String PICTURE_URI_TAG = "pic_tag2";
    private FirebaseAuth auth;
    private TextView userName;
    private SearchView searchView = null;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database;
    private TextView login;
    private Menu nav_Menu;
    boolean isArrowDown = true;
    private static final int RC_SIGN_IN = 33;
    private static final String TAG = "LOG IN";
    private GoogleApiClient mGoogleApiClient;
    ArrayList<String> list;
    FirebaseConnection con;
    private boolean hasInitialized = false;
    NavigationView navigationView;
    LinearLayout loggedInLayout;
    LinearLayout loggedOffLayout;
    private Cursor phones;
    FloatingActionButton myFab;
    static boolean active = false;
    DatabaseReference myRef;

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

    ListView lw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings = new ClassSettings(this.context);
        setContentView(R.layout.activity_main);
        active = true;
        database = FirebaseDatabase.getInstance();
        progressLayout = (RelativeLayout)findViewById(R.id.loadingPanel);
        myRef = database.getReference();
        if(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("outgoingCheckBox", false)) {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putBoolean("outgoingCheckBox", false)
                    .apply();
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
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_key))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        // toolbar height

        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                loggedOffLayout.setVisibility(View.GONE);
                loggedInLayout.setVisibility(View.GONE);
                userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.activity_main_drawer);
                isArrowDown = true;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Do whatever you want here
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        selector = (ImageButton) navigationView.findViewById(R.id.selector);
        userName = (TextView) navigationView.findViewById(R.id.nav_username);
        nav_Menu = navigationView.getMenu();
        loggedInLayout = (LinearLayout) navigationView.findViewById(R.id.loggedInLayout);
        loggedOffLayout = (LinearLayout) navigationView.findViewById(R.id.loggedOffLayout);
        View header = navigationView.findViewById(R.id.NavHeader);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            userName.setText(user.getEmail());
        } else {
            // User is signed out
            userName.setText("Add account");
        }

        loggedInLayout.setVisibility(View.GONE);
        loggedOffLayout.setVisibility(View.GONE);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                isArrowDown = !isArrowDown;
                if (isArrowDown) {
                    changeToMainDrawer();
                } else {
                    changeToAccountDrawer(user);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        String imageUri = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PICTURE_URI_TAG, "");
        if (imageUri.equals("")) {
            Picasso.with(this.context)
                    .load(R.drawable.images)
                    .transform(new CircleTransform())
                    .fit().centerCrop()
                    .into(selector);
        } else {
            Picasso.with(this.context)
                    .load(imageUri)
                    .transform(new CircleTransform())
                    .fit().centerCrop()
                    .into(selector);
        }
        selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, IMG_REQUEST_CODE);
            }
        });
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            String syncOccured = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("SyncOccured", "");
            if(syncOccured.equals("")) {
                addMyNotes(fixedEmail);
            }
            fetchDataFromFirebase(fixedEmail);
        }

        myFab = (FloatingActionButton) findViewById(R.id.myFAB);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putBoolean("haveToChooseContact", true).apply();
                Intent i = new Intent(MainActivity.this,ActivityPopupAfter.class);
                startActivity(i);
            }
        });
    }
    private void changeToAccountDrawer(FirebaseUser user){

        navigationView.getMenu().clear();
        userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
        if (user != null) {
            loggedInLayout.setVisibility(View.VISIBLE);
            loggedOffLayout.setVisibility(View.GONE);
            TextView tw = (TextView)navigationView.findViewById(R.id.email);
            tw.setText(user.getEmail());
            lw = (ListView) navigationView.findViewById(R.id.nav_sync_list);
            list = new ArrayList<>();
            final String email = user.getEmail().replace(".",",");
            if(isNetworkAvailable()) {
                loadAccountSyncedList(email);
            }else{
                Toast.makeText(MainActivity.this, "Network unavailable", Toast.LENGTH_SHORT).show();
            }

        } else {
            // User is signed out
            loggedInLayout.setVisibility(View.GONE);
            loggedOffLayout.setVisibility(View.VISIBLE);
        }
    }

    private void changeToMainDrawer(){
        loggedOffLayout.setVisibility(View.GONE);
        loggedInLayout.setVisibility(View.GONE);
        userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
    }

    private void loadAccountSyncedList(final String email){
        database = FirebaseDatabase.getInstance();
        DatabaseReference syncList = myRef.child("SyncList").child(email);
        syncList.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String email = childSnapshot.getKey();
                    String fixed = email.replace(",", ".");
                    list.add(fixed);
                }

                NavigationViewAdapter adapter = new NavigationViewAdapter(context, list);
                lw.setAdapter(adapter);
                lw.setItemsCanFocus(true);
                lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AlertDialog dialog = deleteDialog(email, list.get(position).replace(".", ","));
                        dialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onDestroy() {
        active = false;
        super.onDestroy();
    }

    Intent mServiceIntent;

    private void initialize() {
        hasInitialized = true;
        con = new FirebaseConnection(context);
        db = new Database(this.context);
        noteList = new ArrayList<>();
        noteList = db.getData();
        ui_listView = (RecyclerView) findViewById(R.id.ac_main_listView);
        new XmlHandling(context);
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }



    private void addSyncedUser() {
        View view = getLayoutInflater().inflate(R.layout.edittext_add_user, null);
        final EditText textInputNote = (EditText) view.findViewById(R.id.add_user);
        AlertDialog.Builder dialogAddNote = new AlertDialog.Builder(this);
        dialogAddNote.setTitle("Type email which you want to sync with");
        dialogAddNote.setCancelable(true);
        dialogAddNote.setView(textInputNote);
        textInputNote.setTextColor(Color.BLACK);
        textInputNote.setSelection(textInputNote.length());
        dialogAddNote.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String input = textInputNote.getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (isEmailValid(input)) {
                    if (user != null) {
                        String email = "";
                        email = user.getEmail();
                        String fixedEmail = email.replace(".", ",");
                        String fixedInput = input.replace(".", ",");
                        con.addSyncEmail(fixedEmail, fixedInput);
                    }
                } else {
                    Toast.makeText(context, "Please write proper email adress", Toast.LENGTH_SHORT).show();
                }
                //  Settings.setUserName(input);
                //  userName.setText(input);
            }
        });

        dialogAddNote.create().show();
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private AlertDialog deleteDialog(final String myEmail, final String emailtoDelete)
    {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        con.deleteNode(myEmail,emailtoDelete);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
                databaseRead();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = "";
                if (user != null) {
                    email = user.getEmail();
                    String fixedEmail = email.replace(".", ",");
                    con.addDataToFirebase(fixedEmail);
                    String syncOccured = PreferenceManager.getDefaultSharedPreferences(context)
                            .getString("SyncOccured", "");
                    if(syncOccured.equals("")) {
                        addMyNotes(fixedEmail);
                    }
                }
                databaseRead();
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
        ArrayList<ClassNote> a = new ArrayList<>();
        for (ClassNote note : noteList) {
            if (note.isSynced() == 1) {
                a.add(note);
            }
        }
        listAdapter = new CustomAdapter(this, a);
        ui_listView.setAdapter(listAdapter);
    }

    private void showMyNotesFirst() {
        ArrayList<ClassNote> a = new ArrayList<>();
        for (ClassNote note : noteList) {
            if (note.isSynced() == 0) {
                a.add(note);
            }
        }
        listAdapter = new CustomAdapter(this, a);
        ui_listView.setAdapter(listAdapter);
    }
    private void showAllNotes() {
        databaseRead();
    }
    CheckBox exportCalls;
    CheckBox exportNotes;

    public void showExportDialog(){
        final View dialogView = View.inflate(MainActivity.this, R.layout.export_dialog, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);

        exportCalls = (CheckBox)dialogView.findViewById(R.id.checkBoxCalls);
        exportNotes = (CheckBox)dialogView.findViewById(R.id.checkBoxNotes);

        alertDialog.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataExportation dExp = new DataExportation(context);
                if(exportCalls.isChecked() && exportNotes.isChecked()){
                    dExp.exportBoth();
                }else if(exportCalls.isChecked()){
                    dExp.exportIncomingCalls();
                }else if(exportNotes.isChecked()){
                    dExp.exportData();
                }else{
                    Toast.makeText(context, "Nothing selected", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportCalls.setChecked(false);
                exportNotes.setChecked(false);
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.create().show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(false);
        if (id == R.id.nav_about) {
            Intent i = new Intent(this, WelcomeActivity.class);
            i.putExtra("about",true);
            startActivity(i);
        } else if (id == R.id.nav_exportData) {
            showExportDialog();
        } else if (id == R.id.nav_scheduledEvents) {
            Intent i = new Intent(this, RemindersList.class);
            // Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_selectWeekDays) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_viewStatistics) {
            CallStatisticsEntity cse = db.getStatistics();
            float total = cse.getIncomingCallCount() + cse.getOutgoingCallCount()
                    + cse.getRemindersAddedCount() + cse.getTypedNoteCount();
            if (total != 0) {
                Intent i = new Intent(this, AllStatisticsView.class);
                startActivity(i);
            }else{
                Toast.makeText(context, "No data yet", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_allIncomingCalls) {
            Intent i = new Intent(this, AllCallsActivity.class);
            startActivity(i);
        }else if (id == R.id.nav_acc_signIn) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_acc_reg) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(hasInitialized) {
            databaseRead();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    dialog = ProgressDialog.show(context, "Signing in", "Please wait...", true);
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    Uri uri = account.getPhotoUrl();
                    putImageIntoNavBar(uri);
                    firebaseAuthWithGoogle(account);
                } else {
                    Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
                    // Google Sign In failed, update UI appropriately
                    // ...
                }
            }
        }
        if (requestCode == 1234) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (android.provider.Settings.canDrawOverlays(this)) {
                }
            }
        }

        if (requestCode == 0xe420) {
            databaseRead();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (searchView != null) {
                    if (!searchView.isIconified()) {
                        searchView.setIconified(true);
                        searchView.onActionViewCollapsed();
                    }
                }
            }
        }
        if (requestCode == IMG_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                putImageIntoNavBar(selectedImage);
            }
        }
    }

    public void putImageIntoNavBar(Uri selectedImage) {
        Picasso.with(context)
                .load(selectedImage)
                .transform(new CircleTransform())
                .fit().centerCrop()
                .into(selector);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PICTURE_URI_TAG, selectedImage.toString()).apply();
    }


    LinearLayoutManager mLayoutManager;

    public Date parseOrReturnNull(String date){
        try {
            DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
            Date date2 = formatter.parse(date);
            return date2;
        } catch (ParseException e) {
            return null;
        }
    }

    private void databaseRead() {
        long a = System.nanoTime();
        noteList = db.getData();
        noteList.addAll(db.getSyncedData());
        Collections.sort(noteList,new Comparator<ClassNote>(){
            @Override
            public int compare(ClassNote b, ClassNote a) {
                   Date date2 = parseOrReturnNull(b.getCallDate());
                   Date date1 = parseOrReturnNull(a.getCallDate());
                   if ( date1 == null ) {
                       if ( date2 == null) {
                           return 0;
                       }
                       return 1;
                   }
                   if ( date2 == null ) {
                       return -1;
                   }
                   return date2.compareTo(date1);
            }
        });

        Collections.reverse(noteList);
        listAdapter = new CustomAdapter(this, noteList);
        listAdapter.notifyDataSetChanged();
        ui_listView.setAdapter(listAdapter);
        ui_listView.setFocusable(true);
        mLayoutManager = new LinearLayoutManager(this);
        ui_listView.setLayoutManager(mLayoutManager);
        ui_listView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        ui_listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && myFab.isShown()) {
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

    public void signInWithGoogle(View view) {
        signIn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
    }

    public void registerEmail(View view) {
        Intent i = new Intent(context, SignUpActivity.class);
        startActivity(i);
    }

    public void LogInEmail(View view) {
        Intent i = new Intent(context, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        isArrowDown = true;
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final LinearLayout loggedInLayout = (LinearLayout) navigationView.findViewById(R.id.loggedInLayout);
        final LinearLayout loggedOffLayout = (LinearLayout) navigationView.findViewById(R.id.loggedOffLayout);
        loggedOffLayout.setVisibility(View.GONE);
        loggedInLayout.setVisibility(View.GONE);
        userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        userName.setText("Add account");
        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show();
        db.deleteSyncedNotesTable();
        databaseRead();
    }

    public void AddEmail(View view) {
        addSyncedUser();
    }


    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements Filterable {
        private int selectedPos = 0;

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView text1;
            TextView text2;
            TextView text3;
            TextView text4;
            ImageView overFlow;
            int realPosition;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                text1 = (TextView) itemView.findViewById(R.id.text1);
                text2 = (TextView) itemView.findViewById(R.id.text2);
                text3 = (TextView) itemView.findViewById(R.id.text3);
                text4 = (TextView) itemView.findViewById(R.id.text4);
                overFlow = (ImageView) itemView.findViewById(R.id.overFlow);
                overFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //itemIndex =  ui_listView.getChildLayoutPosition(v);//.getPositionForView(v);
                        popup = new PopupMenu(context, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.open_note, popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.openNote:
                                        Intent intent = new Intent(context, MainListChildItem.class);
                                        intent.putExtra("classNoteobj", objects.get(getAdapterPosition()));
                                        startActivityForResult(intent, 0xe420);
                                        return true;
                                    case R.id.callToContact:
                                        if (objects.get(getAdapterPosition()).getPhoneNumber().equalsIgnoreCase("None")) {
                                            Toast.makeText(context, "Sorry, you cant make a call when number is \"None\""
                                                    , Toast.LENGTH_LONG).show();
                                        } else {
                                            Uri number = Uri.parse("tel:" + objects.get(getAdapterPosition()).getPhoneNumber());
                                            Intent i = new Intent(Intent.ACTION_DIAL, number);
                                            startActivity(i);
                                        }
                                        return true;
                                    case R.id.textToContact:
                                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                                        sendIntent.setData(Uri.parse("sms:" + objects.get(getAdapterPosition()).getPhoneNumber()));
                                        startActivity(sendIntent);
                                        return true;
                                }
                                return true;
                            }
                        });
                    }
                });
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainListChildItem.class);
                intent.putExtra("classNoteobj", objects.get(getAdapterPosition()));
                startActivityForResult(intent, 0xe420);
            }
        }


        private LayoutInflater inflater;
        private ArrayList<ClassNote> objects;

        private CustomAdapter(Context context, ArrayList<ClassNote> objects) {
            this.objects = objects;
            inflater = LayoutInflater.from(context);
        }

        ContactsFilter mContactsFilter;

        @Override
        public Filter getFilter() {
            if (mContactsFilter == null)
                mContactsFilter = new ContactsFilter();

            return mContactsFilter;
        }

        private class ContactsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // Create a FilterResults object
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = noteList;
                    results.count = noteList.size();
                } else {
                    ArrayList<ClassNote> filteredContacts = new ArrayList<>();

                    // We'll go through all the contacts and see
                    // if they contain the supplied string
                    for (ClassNote c : noteList) {
                        if (c.getNotes(false).toUpperCase().contains(constraint.toString().toUpperCase())
                                || c.getName().toUpperCase().contains(constraint.toString().toUpperCase())
                                || c.getCallDate().toUpperCase().contains(constraint.toString().toUpperCase())
                                || c.getPhoneNumber().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            filteredContacts.add(c);
                        }
                    }

                    // Finally set the filtered values and size/count
                    results.values = filteredContacts;
                    results.count = filteredContacts.size();
                }
                // Return our FilterResults object
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                objects = (ArrayList<ClassNote>) results.values;
                notifyDataSetChanged();
            }
        }

        public int getCount() {
            return objects.size();
        }

        public ClassNote getItem(int position) {
            return objects.get(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = inflater.inflate(R.layout.main_list_item, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(hasStableIds);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            if (objects == null || objects.size() == 0) {
                    //holder.text1.setText("No Results");
                } else {
                    if (!objects.get(position).getName().equals("")) {
                        holder.text1.setText(objects.get(position).getName());
                    } else {
                        holder.text1.setText(objects.get(position).getPhoneNumber());
                    }
                    //Log.d("POSITION", String.valueOf(position));

                    int seconds = 0;
                    int minutes = 0;
                    try {
                        seconds = Integer.parseInt(objects.get(position).getCallTime());
                        while (seconds - 60 >= 0) {
                            minutes++;
                            seconds -= 60;
                        }
                    } catch (NumberFormatException e) {
                        //Will Throw exception!
                        //do something! anything to handle the exception.
                    }
                    holder.text2.setText(objects.get(position).getNotes(true));
                    boolean hasCategory = !objects.get(position).getCategory().equals("");
                    String minAndSecTitle = objects.get(position).getCategory()+objects.get(position).getCallDate()
                            + " ; " + minutes + " min " + seconds + " s";
                    String onlySecTitle = objects.get(position).getCategory()+objects.get(position).getCallDate()
                            + " ; " + seconds + " s";
                    String plainTitle = objects.get(position).getCategory()+objects.get(position).getCallDate();

                    if (objects.get(position).isSynced() == 0) {
                        final SpannableStringBuilder sb = new SpannableStringBuilder();
                        ForegroundColorSpan categoryColor = new ForegroundColorSpan(Color.rgb(53, 173, 63));
                        int catLength = 0 ;
                        if(objects.get(position).getCategory().equals("Personal ")){
                            categoryColor = new ForegroundColorSpan(Color.rgb(53, 173, 63));
                            catLength = 9;
                        }else if(objects.get(position).getCategory().equals("Important ")){
                            categoryColor = new ForegroundColorSpan(Color.RED);
                            catLength = 10;
                        }else if(objects.get(position).getCategory().equals("Vip contact ")){
                            categoryColor = new ForegroundColorSpan(Color.MAGENTA);
                            catLength = 12;
                        }

                        if (minutes != 0) {
                            sb.append(minAndSecTitle);
                            if(hasCategory) {
                                sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            }
                            holder.text3.setText(sb);

                        } else {
                            if (seconds == 0) {
                                sb.append(plainTitle);
                                if(hasCategory) {
                                    sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                holder.text3.setText(sb);
                            } else {
                                sb.append(onlySecTitle);
                                if(hasCategory) {
                                    sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                holder.text3.setText(sb);
                            }
                        }

                    } else {
                        setSyncedNoteText(objects, position, holder);
                    }
                    if (!objects.get(holder.getAdapterPosition()).getReminder().equals("")) {
                        //setting reminder text
                        int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                                , getResources().getDisplayMetrics());
                        Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_reminder_icon, null);
                        img.setBounds(0, 0, imgSize, imgSize);
                        holder.text4.setCompoundDrawables(img, null, null, null);
                        holder.text4.setText(objects.get(holder.getAdapterPosition()).getReminder()
                                .substring(5, objects.get(holder.getAdapterPosition()).getReminder().length()));
                    } else {
                        int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
                                , getResources().getDisplayMetrics());
                        holder.text3.setPadding(0, 0, 0, botPadding);
                        holder.text4.setCompoundDrawables(null, null, null, null);
                        holder.text4.setVisibility(View.GONE);
                    }
                    holder.realPosition = holder.getAdapterPosition();
                }
                //convertView.setTag(holder);

            //return convertView;
        }



        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return objects == null ? 0 : objects.size();
        }

       /* public View getView(int position, View convertView, ViewGroup parent) {

        }*/
    }

    private void setSyncedNoteText(ArrayList<ClassNote> objects, int position, CustomAdapter.ViewHolder holder) {
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#58a7c8"));
        ForegroundColorSpan categoryColor = new ForegroundColorSpan(Color.parseColor("#ff4081"));
        int catLength = 0;
        if(objects.get(position).getCategory().equals("Personal ")){
            categoryColor = new ForegroundColorSpan(Color.rgb(53, 173, 63));
            catLength = 9;
        }else if(objects.get(position).getCategory().equals("Important ")){
            categoryColor = new ForegroundColorSpan(Color.RED);
            catLength = 10;
        }else if(objects.get(position).getCategory().equals("Vip contact ")){
            categoryColor = new ForegroundColorSpan(Color.MAGENTA);
            catLength = 12;
        }

        int length = objects.get(position).getCallDate().length() + 1;
        int emailLength = objects.get(position).getFriendEmail().length() + 1;
        String email = objects.get(position).getFriendEmail().replace(",", ".");

        sb.append(objects.get(position).getCategory()+objects.get(position).getCallDate() + ", " + email);
        if(catLength > 0) {
            sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        sb.setSpan(fcs, length + catLength, length + emailLength + catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        holder.text3.setText(sb);
        int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
                , getResources().getDisplayMetrics());
        holder.text3.setPadding(0, 0, 0, botPadding);
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


    ProgressDialog dialog;
    private void signIn() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    RelativeLayout progressLayout;
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            userName.setText(user.getEmail());
                            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                            if (drawer.isDrawerOpen(GravityCompat.START)) {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                            isArrowDown = true;
                            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                            final LinearLayout loggedInLayout = (LinearLayout) navigationView.findViewById(R.id.loggedInLayout);
                            final LinearLayout loggedOffLayout = (LinearLayout) navigationView.findViewById(R.id.loggedOffLayout);
                            loggedOffLayout.setVisibility(View.GONE);
                            loggedInLayout.setVisibility(View.GONE);
                            userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                            navigationView.getMenu().clear();
                            navigationView.inflateMenu(R.menu.activity_main_drawer);
                            final String userId = user.getUid();
                            final String email = user.getEmail();
                            con.addMyEmail(userId,email);
                            String fixedEmail = email.replace(".", ",");
                            fetchDataFromFirebase(fixedEmail);
                            addMyNotes(fixedEmail);
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Successfully logged in",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard, menu);
        menuInflater.inflate(R.menu.sync_refresh, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                searchView = (SearchView) searchItem.getActionView();
            }
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
                    ui_listView.setAdapter(listAdapter);
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }


    public void fetchDataFromFirebase(final String email) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        DatabaseReference SharedList = myRef.child("SyncList").child(email);
        SharedList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // Load data on background
                class LoadContact extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        if(progressLayout != null) {
                            progressLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        ArrayList<String> list = new ArrayList<>();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String email = childSnapshot.getKey();
                            list.add(email);
                        }

                        for (final String item : list) {
                            DatabaseReference othersList = myRef.child("SyncList").child(item);
                            othersList.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(email)) {
                                        DatabaseReference ref = myRef.child("Notes").child(item);
                                        ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                addSyncedData(dataSnapshot, item);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.w("RABARBARAS", "Failed to read value.", databaseError.toException());
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if(progressLayout != null) {
                            progressLayout.setVisibility(View.GONE);
                        }
                    }
                }
                new LoadContact().execute();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("RABARBARAS", "Failed to read value.", error.toException());
            }
        });
    }
    public void addSyncedData(final DataSnapshot dataSnapshot,final String FriendEmail) {

        // Load data on background
        class LoadContact extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(progressLayout != null) {
                    progressLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                GenericTypeIndicator<ArrayList<DataForSyncingModel>> t =
                        new GenericTypeIndicator<ArrayList<DataForSyncingModel>>() {
                        };
                ArrayList<DataForSyncingModel> value = dataSnapshot.getValue(t);
                if (value != null) {
                    ArrayList<ClassNote> list = new ArrayList<>();
                    ArrayList<ClassNote> synced = db.getSyncedData();
                    for (DataForSyncingModel syncedNote : value) {
                        if(syncedNote != null) {
                            list.add(initializeClassNote(syncedNote, FriendEmail));
                        }
                    }
                    Iterator<ClassNote> iter = list.iterator();
                    while (iter.hasNext()) {
                        ClassNote c = iter.next();

                        for (ClassNote n : synced) {
                            if (isEqualNote(c, n) || c.getCategory().equals("Personal ")) {
                                iter.remove();
                                break;
                            }
                        }
                    }
                    db.insertToSyncedTable(list);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            databaseRead();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(progressLayout != null) {
                    progressLayout.setVisibility(View.GONE);
                }
            }
        }
        new LoadContact().execute();
        // db.insertSyncedData(list,1);
    }

    private ClassNote initializeClassNote(DataForSyncingModel syncedNote, String friendEmail) {
        ClassNote note = new ClassNote();
        note.setNotes(syncedNote.getNotes());
        note.setName(getContactName(context,syncedNote.getPhoneNumber()));
        note.setCallDate(syncedNote.getCallDate());
        note.setPhoneNumber(syncedNote.getPhoneNumber());
        note.setSynced(0);
        note.setReminder("");
        if(syncedNote.getCategory() == null || syncedNote.getCategory().equals("null")){
            note.setCategory("");
        }else {
            note.setCategory(syncedNote.getCategory());
        }
        int catchCall = 1;
        if (!db.getCatchCall(syncedNote.getPhoneNumber())) {
            catchCall = 0;
        }
        note.setCatchCall(catchCall);
        note.setFriendEmail(friendEmail);
        return note;
    }

    private boolean isEqualNote(ClassNote c, ClassNote n) {
        return c.getNotes(true).equals(n.getNotes(true))
                && c.getPhoneNumber().equals(n.getPhoneNumber())
                && c.getCallDate().equals(n.getCallDate());
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    public void addMyNotes(final String email){
        DatabaseReference myNotesDb = myRef.child("Notes").child(email);
        myNotesDb.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<DataForSyncingModel>> t =
                        new GenericTypeIndicator<ArrayList<DataForSyncingModel>>() {
                        };
                ArrayList<DataForSyncingModel> value = dataSnapshot.getValue(t);
                if (value != null) {
                    ArrayList<ClassNote> list = new ArrayList<>();
                    ArrayList<ClassNote> allNotes = db.getData();
                    for (DataForSyncingModel note : value) {
                        list.add(initializeClassNote(note, ""));
                    }
                    Iterator<ClassNote> iter = list.iterator();
                    while (iter.hasNext()) {
                        ClassNote c = iter.next();

                        for (ClassNote n : allNotes) {
                            if (isEqualNote(c, n)) {
                                iter.remove();
                                break;
                            }
                        }
                    }
                    db.insertSyncedData(list);
                    databaseRead();
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putString("SyncOccured", "yes").apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseRead();
    }

    private void addOutgoingCallsToDB() {

    }
}

