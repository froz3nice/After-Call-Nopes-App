package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.LoginActivity;
import com.example.juseris.aftercallnote.Activities.MainActivity;
import com.example.juseris.aftercallnote.Adapters.NavigationViewAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by juseris on 9/6/2017.
 */

public class MainDrawerClass {
    private final Context context;
    private final MainActivity activity;
    private final RelativeLayout progressLayout;
    private final SharedPreferences prefs;
    private final NavigationView navigationView;
    private final ImageButton selector;
    private final Menu nav_Menu;
    private final LinearLayout loggedInLayout;
    private final LinearLayout loggedOffLayout;
    private final GoogleApiClient mGoogleApiClient;
    private CheckBox exportCalls;
    private CheckBox exportNotes;
    private FirebaseAuth auth;
    private TextView userName;
    private boolean isArrowDown = true;
    private DrawerLayout drawer;
    private FirebaseConnection con;
    private DatabaseReference myRef;
    private Database db;
    private ListView lw;
    private ArrayList<String> list;
    private static final int RC_SIGN_IN = 33;
    private final int IMG_REQUEST_CODE = 0x5654;
    private final String PICTURE_URI_TAG = "pic_tag2";

    public MainDrawerClass(Context ctx, final MainActivity activity) {
        context = ctx;
        this.activity = activity;
        con = new FirebaseConnection(context, activity);
        db = new Database(this.context);
        TextView tw_logOut = (TextView) activity.findViewById(R.id.tw_logOut);
        TextView signInWithGoogle = (TextView) activity.findViewById(R.id.signInWithGoogle);
        TextView logInWithEmail = (TextView) activity.findViewById(R.id.logInWithEmail);
        TextView tw_addNewEmail = (TextView) activity.findViewById(R.id.tw_addNewEmail);
        progressLayout = (RelativeLayout) activity.findViewById(R.id.loadingPanel);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        myRef = Utils.getDatabase().getReference();
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.web_client_key))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(activity, activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        // toolbar height

        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, context.getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);

        drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

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
        navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        userName = (TextView) navigationView.findViewById(R.id.nav_username);

        selector = (ImageButton) navigationView.findViewById(R.id.selector);
        userName = (TextView) navigationView.findViewById(R.id.nav_username);
        nav_Menu = navigationView.getMenu();
        loggedInLayout = (LinearLayout) navigationView.findViewById(R.id.loggedInLayout);
        loggedOffLayout = (LinearLayout) navigationView.findViewById(R.id.loggedOffLayout);
        View header = navigationView.findViewById(R.id.NavHeader);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String imageUri = prefs.getString(PICTURE_URI_TAG, "");
        if (imageUri.equals("")) {
            Picasso.with(this.context).load(R.drawable.images).transform(new CircleTransformation()).fit().centerCrop().into(selector);
        } else {
            Picasso.with(this.context).load(imageUri).transform(new CircleTransformation()).fit().centerCrop().into(selector);
        }
        selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activity.startActivityForResult(i, IMG_REQUEST_CODE);
            }
        });

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

        navigationView.setNavigationItemSelectedListener(activity);

        tw_logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
        signInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        logInWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, LoginActivity.class));
            }
        });
        tw_addNewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSyncedUser();
            }
        });
    }

    private void addSyncedUser() {
        View view = activity.getLayoutInflater().inflate(R.layout.edittext_add_user, null);
        final EditText textInputNote = (EditText) view.findViewById(R.id.add_user);
        AlertDialog.Builder dialogAddNote = new AlertDialog.Builder(activity);
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

            }
        });

        dialogAddNote.create().show();
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void signIn() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void putImageIntoNavBar(Uri selectedImage) {
        Picasso.with(context)
                .load(selectedImage)
                .transform(new CircleTransformation())
                .fit().centerCrop()
                .into(selector);
        prefs.edit().putString(PICTURE_URI_TAG, selectedImage.toString()).apply();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        isArrowDown = true;
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
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
        activity.refreshList();
    }

    private void changeToMainDrawer() {
        loggedOffLayout.setVisibility(View.GONE);
        loggedInLayout.setVisibility(View.GONE);
        userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
    }

    private void changeToAccountDrawer(FirebaseUser user) {
        navigationView.getMenu().clear();
        userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up2, 0);
        if (user != null) {
            loggedInLayout.setVisibility(View.VISIBLE);
            loggedOffLayout.setVisibility(View.GONE);
            TextView tw = (TextView) navigationView.findViewById(R.id.email);
            tw.setText(user.getEmail());
            lw = (ListView) navigationView.findViewById(R.id.nav_sync_list);
            list = new ArrayList<>();
            final String email = user.getEmail().replace(".", ",");
            if (isNetworkAvailable()) {
                loadAccountSyncedList(email);
            } else {
                Toast.makeText(context, "Network unavailable", Toast.LENGTH_SHORT).show();
            }

        } else {
            // User is signed out
            loggedInLayout.setVisibility(View.GONE);
            loggedOffLayout.setVisibility(View.VISIBLE);
        }
    }

    private void loadAccountSyncedList(final String email) {
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


    private AlertDialog deleteDialog(final String myEmail, final String emailtoDelete) {
        return new AlertDialog.Builder(activity)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        con.deleteNode(myEmail, emailtoDelete);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showExportDialog() {
        final View dialogView = View.inflate(activity, R.layout.export_dialog, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);

        exportCalls = (CheckBox) dialogView.findViewById(R.id.checkBoxCalls);
        exportNotes = (CheckBox) dialogView.findViewById(R.id.checkBoxNotes);

        alertDialog.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataExportation dExp = new DataExportation(context);
                if (exportCalls.isChecked() && exportNotes.isChecked()) {
                    dExp.exportBoth();
                } else if (exportCalls.isChecked()) {
                    dExp.exportIncomingCalls();
                } else if (exportNotes.isChecked()) {
                    dExp.exportData();
                } else {
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


    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            activity.dialog.dismiss();
                        } else {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            userName.setText(user.getEmail());
                            drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
                            if (drawer.isDrawerOpen(GravityCompat.START)) {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                            isArrowDown = true;
                            NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
                            final LinearLayout loggedInLayout = (LinearLayout) navigationView.findViewById(R.id.loggedInLayout);
                            final LinearLayout loggedOffLayout = (LinearLayout) navigationView.findViewById(R.id.loggedOffLayout);
                            loggedOffLayout.setVisibility(View.GONE);
                            loggedInLayout.setVisibility(View.GONE);
                            userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                            navigationView.getMenu().clear();
                            navigationView.inflateMenu(R.menu.activity_main_drawer);
                            final String userId = user.getUid();
                            final String email = user.getEmail();
                            con.addMyEmail(userId, email);
                            String fixedEmail = email.replace(".", ",");
                            fetchDataFromFirebase(fixedEmail);
                            con.addMyNotes(fixedEmail);
                            activity.dialog.dismiss();
                            Toast.makeText(activity, "Successfully logged in",
                                    Toast.LENGTH_SHORT).show();
                            new XmlHandling(context);
                        }
                    }
                });
    }


    public void fetchDataFromFirebase(final String email) {
        myRef = Utils.getDatabase().getReference();
        DatabaseReference SharedList = myRef.child("SyncList").child(email);
        SharedList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // Load data on background
                class LoadContact extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        if (progressLayout != null) {
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
                                        ref.keepSynced(true);
                                        //ref.limitToFirst(1);
                                        ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                con.addSyncedData(dataSnapshot, item);
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
                        if (progressLayout != null) {
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
}
