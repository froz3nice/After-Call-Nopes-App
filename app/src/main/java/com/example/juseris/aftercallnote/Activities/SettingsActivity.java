package com.example.juseris.aftercallnote.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.example.juseris.aftercallnote.Adapters.WeekListAdapter;
import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingsActivity extends AppCompatActivity {
    private WeekListAdapter dataAdapter = null;
    private CheckBox outgoing;
    private CheckBox incoming;
    private CheckBox showPurplePlus;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_week_days);
        showPurplePlus = (CheckBox) findViewById(R.id.showAddNoteCheckbox);
        incoming = (CheckBox) findViewById(R.id.incomingCheckBox);
        outgoing = (CheckBox) findViewById(R.id.outgoingCheckBox);

        incoming.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("incomingCheckBox", true));
        outgoing.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("outgoingCheckBox", true));
        showPurplePlus.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("purpleBox", false));

        outGoingCbListener();
        incomingCbListener();
        showPurplePlusListener();
        setUpToolbar();
        setTitle("Settings");
        setUpListView();
    }

    private void setUpToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        //toolbar height
        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setUpListView(){
        ArrayList<String> weekDays = new ArrayList<>();
        weekDays.add("Monday");
        weekDays.add("Tuesday");
        weekDays.add("Wednesday");
        weekDays.add("Thursday");
        weekDays.add("Friday");
        weekDays.add("Saturday");
        weekDays.add("Sunday");
        weekDays.add(getResources().getString(R.string.week_days_explanation));

        dataAdapter = new WeekListAdapter(this,
                R.layout.week_days, weekDays);
        ListView listView = (ListView) findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
    }
    private void showPurplePlusListener(){
        showPurplePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(SettingsActivity.this)) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1234);
                    }
                }
                if (showPurplePlus.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit().putBoolean("purpleBox", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit().putBoolean("purpleBox", false).apply();
                }
            }
        });
    }

    private void incomingCbListener(){
        incoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (incoming.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean("incomingCheckBox", true)
                            .apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean("incomingCheckBox", false)
                            .apply();
                }
            }
        });
    }
    private void outGoingCbListener(){
        outgoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outgoing.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean("outgoingCheckBox", true)
                            .apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean("outgoingCheckBox", false)
                            .apply();
                }
            }
        });
    }

}





