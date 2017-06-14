package com.example.juseris.aftercallnote.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.ChildItemAdapter;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainListChildItem extends AppCompatActivity {
    private Database db = null;
    private Context context;
    private String phoneNumber;
    private ClassNote classNote;
    private TextView note;
    private ListView contactList;
    private ChildItemAdapter listAdapter = null;
    private PopupMenu popup;
    private ArrayList<ClassNote> noteList = null;
    private ViewPager mPager;

    private Integer itemIndex;
    private CheckBox catchCall;
    private String name;
    private ClassSettings Settings;
    boolean hasInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list_child_item);
        context = getApplicationContext();
        db = new Database(context);
        Settings = new ClassSettings(context);
        noteList = new ArrayList<>();
        classNote = getIntent().getExtras().getParcelable("classNoteobj");
        phoneNumber = classNote.getPhoneNumber();
        noteList = db.getDataByNumber(phoneNumber);
        noteList.addAll(db.getSyncedNotesByNumber(phoneNumber));
        contactList = (ListView) findViewById(R.id.contactList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());

        layoutParams.height = height;
        toolbar.setLayoutParams(layoutParams);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (classNote.getName().equals("")) {
            setTitle(classNote.getPhoneNumber());
        } else {
            setTitle(classNote.getName());
        }
        sort();
        listAdapter = new ChildItemAdapter(this, noteList);
        listAdapter.notifyDataSetChanged();
        contactList.setAdapter(listAdapter);

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
    private AlertDialog alertDialog()
    {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setMessage("AfterCallNotes will not show and ask Notes for this contact anymore.\n\nYou can change this in current contact page.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        db.updateCatchCall(phoneNumber, 0);
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putBoolean(phoneNumber, false).apply();
                        dialog.dismiss();
                        catchCall.setChecked(PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(phoneNumber, true));
                    }

                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        catchCall.setChecked(PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(phoneNumber, true));
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        catchCall.setChecked(PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean(phoneNumber, true));
                    }
                })
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
        if (catchCall != null){
            catchCall.setChecked(PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(phoneNumber, true));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inside_contact, menu);
        getMenuInflater().inflate(R.menu.catch_call_in_contact, menu);
        getMenuInflater().inflate(R.menu.add_new_note, menu);

        catchCall = (CheckBox)menu.findItem(R.id.menuShowDue).getActionView().findViewById(R.id.checkboxWhite);
        catchCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (catchCall.isChecked()) {
                    db.updateCatchCall(classNote.getPhoneNumber(), 1);
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putBoolean(phoneNumber, true).apply();
                } else {
                    AlertDialog dialog = alertDialog();
                    dialog.show();
                    double width = getResources().getDisplayMetrics().widthPixels * 0.95;
                    dialog.getWindow().setLayout((int)width, WindowManager.LayoutParams.WRAP_CONTENT);
                    // db.updateCatchCall(classNote.getPhoneNumber(), 0);
                    //PreferenceManager.getDefaultSharedPreferences(context)
                    //       .edit().putBoolean(phoneNumber, false).apply();
                }
            }
        });
        catchCall.setChecked(PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(phoneNumber, true));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.showStats:
                Intent intent = new Intent(context, AllStatisticsView.class);
                Log.d("phonenr", phoneNumber);
                intent.putExtra("PhoneNumber", phoneNumber);
                boolean hasNoData = db.getStatistics(phoneNumber).getTypedNoteCount() == 0 &&
                        db.getStatistics(phoneNumber).getIncomingCallCount() == 0 &&
                        db.getStatistics(phoneNumber).getOutgoingCallCount() == 0 &&
                        db.getStatistics(phoneNumber).getRemindersAddedCount() == 0 ;
                if (hasNoData) {
                    Toast.makeText(context, "no data for this number", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(intent);
                }
                return true;
            case R.id.call:
                listView_MakeACall();
                return true;
            case R.id.text:
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + phoneNumber));
                startActivity(sendIntent);
                return true;
            case R.id.action_addNote:
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putBoolean("haveToChooseContact", false).apply();
                Intent i = new Intent(context, ActivityPopupAfter.class);
                i.putExtra("PhoneNumber", phoneNumber);
                startActivityForResult(i, 0x4233);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x4233) {
            updateListView();
        }
    }
    public Date parseOrReturnNull(String date){
        try {
            DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
            Date date2 = formatter.parse(date);
            return date2;
        } catch (ParseException e) {
            return null;
        }
    }
    public void sort(){
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
    }

    public void updateListView() {
        noteList = db.getDataByNumber(phoneNumber);
        noteList.addAll(db.getSyncedNotesByNumber(phoneNumber));
        sort();
        listAdapter = new ChildItemAdapter(this, noteList);
        listAdapter.notifyDataSetChanged();
        contactList.setAdapter(listAdapter);
    }

}
