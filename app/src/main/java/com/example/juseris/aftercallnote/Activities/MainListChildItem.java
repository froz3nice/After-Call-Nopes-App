package com.example.juseris.aftercallnote.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.ChildItemAdapter;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainListChildItem extends AppCompatActivity {
    private Database db = null;
    private Context context;
    private String phoneNumber;
    private String originalPhoneNr;
    private RecyclerView contactList;
    private ArrayList<IGenericItem> noteList;
    private CheckBox catchCall;
    private SharedPreferences prefs;
    private FloatingActionButton myFab;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list_child_item);
        context = getApplicationContext();
        db = new Database(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ClassNote classNote = getIntent().getExtras().getParcelable("classNoteobj");
        originalPhoneNr = classNote.getPhoneNumber();
        phoneNumber = Utils.fixNumber(classNote.getPhoneNumber());
        ImageView text = (ImageView) findViewById(R.id.textToContact);
        ImageView call = (ImageView) findViewById(R.id.callToContact);
        contactList = (RecyclerView) findViewById(R.id.ac_child_listView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        layoutParams.height = height;
        toolbar.setLayoutParams(layoutParams);

        setTitle("");
        TextView nr = (TextView) findViewById(R.id.number);
        nr.setText(phoneNumber);
        TextView name = (TextView) findViewById(R.id.name);
        if (classNote.getName().equals("")) {
            name.setText("No name");
        } else {
            name.setText(classNote.getName());
        }
        refreshList();

        catchCall = (CheckBox) findViewById(R.id.catchCallCheckBox);
        catchCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (catchCall.isChecked()) {
                    prefs.edit().putBoolean(phoneNumber, true).apply();
                } else {
                    AlertDialog dialog = alertDialog();
                    dialog.show();
                    double width = getResources().getDisplayMetrics().widthPixels * 0.95;
                    dialog.getWindow().setLayout((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
                    // db.updateCatchCall(classNote.getPhoneNumber(), 0);
                    //PreferenceManager.getDefaultSharedPreferences(context)
                    //      .edit().putBoolean(phoneNumber, false).apply();
                }
            }
        });
        catchCall.setChecked(prefs.getBoolean(phoneNumber, true));

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + phoneNumber));
                startActivity(sendIntent);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView_MakeACall();
            }
        });

        myFab = (FloatingActionButton) findViewById(R.id.myFAB);
        myFab.bringToFront();
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prefs.edit().putBoolean("haveToChooseContact", false).apply();
                Intent i = new Intent(context, ActivityPopupAfter.class);
                i.putExtra("PhoneNumber", phoneNumber);
                prefs.edit().putString("callTime", "").apply();
                startActivity(i);
            }
        });
    }


    private void listView_MakeACall() {
        if (phoneNumber.equalsIgnoreCase("None")) {
            Toast.makeText(context, "Sorry, you cant make a call when number is \"None\"", Toast.LENGTH_LONG).show();
        } else {
            Uri number = Uri.parse("tel:" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(intent);
        }
    }

    private AlertDialog alertDialog() {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setMessage("AfterCallNotes will not show and ask Notes for this contact anymore.\n\nYou can change this in current contact page.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        prefs.edit().putBoolean(phoneNumber, false).apply();
                        dialog.dismiss();
                        catchCall.setChecked(prefs.getBoolean(phoneNumber, true));
                    }

                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        catchCall.setChecked(prefs.getBoolean(phoneNumber, true));
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        catchCall.setChecked(prefs.getBoolean(phoneNumber, true));
                    }
                })
                .create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
        if (catchCall != null) {
            catchCall.setChecked(prefs.getBoolean(phoneNumber, true));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inside_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.showStats:
                Intent intent = new Intent(context, AllStatisticsView.class);
                Log.d("phonenr", phoneNumber);
                intent.putExtra("PhoneNumber", phoneNumber);
                intent.putExtra("showContactStats",false);
                boolean hasNoData = db.getStatistics(phoneNumber).getTypedNoteCount() == 0 &&
                        db.getStatistics(phoneNumber).getIncomingCallCount() == 0 &&
                        db.getStatistics(phoneNumber).getOutgoingCallCount() == 0 &&
                        db.getStatistics(phoneNumber).getRemindersAddedCount() == 0;
                if (hasNoData) {
                    Toast.makeText(context, "no data for this number", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x4233) {
            refreshList();
        }
    }

    public void refreshList() {
        noteList = db.getDataByNumber(phoneNumber);
        noteList.addAll(db.getSyncedNotesByNumber(phoneNumber));
        //parseToDates();
        //sort();
        noteList = Utils.getSortedList(noteList);// sortNotesByDate(parseToDates(noteList));
        Collections.reverse(noteList);

        ArrayList<IGenericItem> newItems = db.getNewPrestaByNr(phoneNumber, originalPhoneNr);
        newItems = Utils.getSortedPrestaList(newItems);
        noteList.addAll(newItems);

        ArrayList<IGenericItem> items = db.getPrestashopByNr(phoneNumber, originalPhoneNr);
        //Collections.reverse(items);
        noteList.addAll(items);
        ChildItemAdapter listAdapter = new ChildItemAdapter(this, noteList);
        listAdapter.notifyDataSetChanged();
        contactList.setAdapter(listAdapter);
        contactList.setFocusable(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        contactList.setLayoutManager(mLayoutManager);
        contactList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        // contactList.setAdapter(listAdapter);
        contactList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        contactList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

}
