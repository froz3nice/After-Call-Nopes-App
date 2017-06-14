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

import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    private MyCustomAdapter dataAdapter = null;
    private ClassSettings Settings;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_week_days);
        final CheckBox showAddNote = (CheckBox) findViewById(R.id.showAddNoteCheckbox);
        final CheckBox incoming = (CheckBox) findViewById(R.id.incomingCheckBox);
        final CheckBox outgoing = (CheckBox) findViewById(R.id.outgoingCheckBox);
        Settings = new ClassSettings(getApplicationContext());

        incoming.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("incomingCheckBox", true));
        outgoing.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("outgoingCheckBox", true));
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

        showAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(SettingsActivity.this)) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1234);
                    }
                }
                Settings.setCatchCall(showAddNote.isChecked());
                if (showAddNote.isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit().putBoolean("purpleBox", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit().putBoolean("purpleBox", false).apply();
                }
            }
        });

        showAddNote.setChecked(Settings.getCatchCall());
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
        setTitle("Settings");
        ArrayList<String> weekDays = new ArrayList<>();
        weekDays.add("Monday");
        weekDays.add("Tuesday");
        weekDays.add("Wednesday");
        weekDays.add("Thursday");
        weekDays.add("Friday");
        weekDays.add("Saturday");
        weekDays.add("Sunday");
        weekDays.add(getResources().getString(R.string.week_days_explanation));

        dataAdapter = new MyCustomAdapter(this,
                R.layout.week_days, weekDays);
        ListView listView = (ListView) findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
    }

    private class MyCustomAdapter extends ArrayAdapter<String> {

        private ArrayList<String> weekList;

        MyCustomAdapter(Context context, int textViewResourceId,
                        ArrayList<String> countryList) {
            super(context, textViewResourceId, countryList);
            this.weekList = new ArrayList<>();
            this.weekList.addAll(countryList);
        }

        private class ViewHolder {
            CheckBox name;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                if (position < 7) {
                    convertView = vi.inflate(R.layout.week_days, parent, false);

                    holder = new ViewHolder();
                    holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                    convertView.setTag(holder);
                    holder.name.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = (CheckBox) v;
                            if (cb.isChecked()) {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                        .edit()
                                        .putBoolean(cb.getText().toString(), true)
                                        .apply();
                            } else {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                        .edit()
                                        .putBoolean(cb.getText().toString(), false)
                                        .apply();
                            }
                        }
                    });
                    String day = weekList.get(position);
                    holder.name.setText(day);
                    holder.name.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean(day, true));
                } else {
                    convertView = vi.inflate(R.layout.settings_string, parent, false);
                }
            }
            return convertView;
        }
    }
}





