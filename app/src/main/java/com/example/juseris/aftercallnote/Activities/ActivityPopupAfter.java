package com.example.juseris.aftercallnote.Activities;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.CustomListAdapterDialog;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.FlyingButton;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.example.juseris.aftercallnote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityPopupAfter extends AppCompatActivity {
    private Context context;
    private EditText textNote;
    private String name = "";
    private Database database = null;
    private ClassSettings Settings = null;
    private final Integer catchCall = 1;
    private NotificationManager manager;
    private String number = "";
    private final Integer notificationID = 0x156;
    private String reminder = "";
    private long eventID = 0;
    private long time = 0;
    private String category = "";
    private Dialog dialog;
    CheckBox box;
    private SharedPreferences prefs;

    // private Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        setContentView(R.layout.activity_popup_after);
        context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        database = new Database(context);
        Settings = new ClassSettings(context);
        textNote = (EditText) findViewById(R.id.ac_note);
        Button buttonAdd = (Button) findViewById(R.id.ac_addNote);

        if(isMyServiceRunning(FlyingButton.class)) {
            context.stopService(new Intent(context, FlyingButton.class));
        }
        //Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/hover.ttf");
        //textNote.setTypeface(custom_font);
        //textNote.getBackground().clearColorFilter();
        //textNote.setHintTextColor(Color.parseColor("#545252"));

        //textNote.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.afterToolBar);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        //toolbar height
        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);
        box = (CheckBox) findViewById(R.id.toolbarCheckBox);
        box.setVisibility(View.VISIBLE);
        View categoryLayout = findViewById(R.id.categoryLayout);
        View calendarLayout = findViewById(R.id.calendarLayout);
        dialog = new Dialog(this);
        dialog.setTitle("Select category");
        categoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategoryPopup();
            }
        });

        calendarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar();
            }
        });
        setTitle("");

        final String nrs = prefs
                .getString("NUMBERS", "");
        if (getIntent().getStringExtra("PhoneNumber") == null) {
            number = prefs
                    .getString("LastActiveNr", "");
            createNotification();
        } else {
            number = fixNumber(getIntent().getStringExtra("PhoneNumber"));
        }

        if(number == null){
            box.setText("Please make a call");
        }else {
            if(number != null && !number.equals("")) {
                name = getContactName(context, number);
            }
        }

        if(getIntent().getStringExtra("Note") != null) {
            textNote.setText(getIntent().getStringExtra("Note"));
            textNote.setSelection(textNote.getText().length());
            if (name.equals("")) {
                box.setText("Edit note for " + number);
            } else {
                box.setText("Edit note for " + name);
            }
        }else {
            if (name.equals("")) {
                box.setText("Add note for " + number);
            } else {
                box.setText("Add note for " + name);
            }
        }
        box.setChecked(prefs
                .getBoolean(number, true));
        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (box.isChecked()) {
                    database.updateCatchCall(number, 1);
                    prefs
                            .edit().putBoolean(number, true).apply();
                } else {
                    AlertDialog dialog = alertDialog();
                    dialog.show();
                    double width = getResources().getDisplayMetrics().widthPixels * 0.95;
                    dialog.getWindow().setLayout((int)width, WindowManager.LayoutParams.WRAP_CONTENT);
                }
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getStringExtra("Note") != null){
                    String input = textNote.getText().toString();
                    database.Update_Note(getIntent().getIntExtra("ID",0), input,reminder,category);
                    ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(textNote.getWindowToken(), 0);
                    Intent returnIntent = new Intent();
                    setResult(AppCompatActivity.RESULT_OK, returnIntent);
                    if (time > 1000) {
                        setReminder((int) time / 60000,textNote.getText().toString());

                    }
                    finish();
                }else {
                    if(textNote.getText().toString().isEmpty()){
                        Toast.makeText(context, "Please add note to save", Toast.LENGTH_SHORT).show();
                    }else {
                        if (!number.equals("")) {
                            String[] timeArray = Settings.getCallTime().split(";");
                            String[] numbers = nrs.split(";");
                            String nums = "";
                            String t = "";
                            for (int i = 0; i < numbers.length; i++) {
                                if (i != numbers.length - 1) {
                                    if (i == 0) {
                                        nums += numbers[i];
                                        t += timeArray[i];
                                    } else {
                                        nums += ";" + numbers[i];
                                        t += ";" + timeArray[i];
                                    }
                                }
                            }
                            Log.d("name", nums);
                            Log.d("time", t);
                            Settings.setCallTime(t);
                            prefs
                                    .edit().putString("NUMBERS", nums).apply();

                            //Settings.setNumbers(nums);
                            if (nums.equals("")) {

                                manager.cancel(notificationID);
                                //prefs
                                //   .edit().putString("LastActiveNr","").apply();
                            }

                            database.Insert_Note(
                                    number,
                                    textNote.getText().toString(),
                                    timeArray[timeArray.length - 1], name, catchCall, reminder, category);
                            FirebaseConnection con = new FirebaseConnection(context);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String email = "";
                            if (user != null) {
                                email = user.getEmail();
                                String fixedEmail = email.replace(".", ",");
                                con.addDataToFirebase(fixedEmail);
                                String syncOccured = prefs
                                        .getString("SyncOccured", "");
                                if (syncOccured.equals("")) {
                                    con.addMyNotes(fixedEmail);
                                }
                            }
                            database.createOrUpdateStatistics(number, 0, 0, 1, 0, 0, 0);
                            Settings.setName(name);
                            Intent returnIntent = new Intent();
                            setResult(AppCompatActivity.RESULT_OK, returnIntent);
                            if (time > 1000) {
                                setReminder((int) time / 60000, textNote.getText().toString());
                            }
                            finish();
                            if(MainActivity.active){
                                Intent i = new Intent(ActivityPopupAfter.this,MainActivity.class);
                                startActivity(i);
                            }
                        }else{
                            Toast.makeText(context, "Please choose a contact", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 1000 * 60*5);
    }

    private AlertDialog alertDialog()
    {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setMessage("AfterCallNotes will not show and ask Notes for this contact anymore.\n\nYou can change this in current contact page.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        database.updateCatchCall(number, 0);
                        prefs
                                .edit().putBoolean(number, false).apply();
                        dialog.dismiss();
                        box.setChecked(prefs.getBoolean(number, true));
                    }

                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        box.setChecked(prefs
                                .getBoolean(number, true));
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        box.setChecked(prefs
                                .getBoolean(number, true));
                    }
                })
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();
         if(!prefs.getString("ChosenNumber", "").equals("")) {
             number = fixNumber(prefs
                     .getString("ChosenNumber", ""));
             box.setChecked(prefs
                     .getBoolean(number, true));
             name = getContactName(context, number);
             if (name.equals("")) {
                 box.setText("Add note for " + number);
             } else {
                 box.setText("Add note for " + name);
             }
             prefs
                     .edit().putString("ChosenNumber", "").apply();
         }

        if(prefs
                .getBoolean("haveToChooseContact", false)){
            TextView tw = (TextView)findViewById(R.id.nameOrNumber);
            tw.setVisibility(View.VISIBLE);
            box.setVisibility(View.GONE);
            number = "";
        }else{
            TextView tw = (TextView)findViewById(R.id.nameOrNumber);
            tw.setVisibility(View.GONE);
            box.setVisibility(View.VISIBLE);
        }
    }

    private void showCategoryPopup() {
        final ArrayList<String> titles = new ArrayList<>();
        titles.add("No category");
        titles.add("Personal (will not sync)");
        titles.add("Important");
        titles.add("Vip contact");
        View v = getLayoutInflater().inflate(R.layout.dialog_main, null);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        //getWindow().setAttributes(params);
        v.setLayoutParams(params);
        ListView lv = (ListView) v.findViewById(R.id.custom_list);
        CustomListAdapterDialog clad = new CustomListAdapterDialog(ActivityPopupAfter.this, titles);
        lv.setAdapter(clad);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tw = (TextView)findViewById(R.id.categoryText);
                switch(position){
                    case 0:
                        tw.setTextColor(Color.parseColor("#808080"));
                        tw.setText("Category");
                        break;
                    case 1:
                        category="Personal";
                        tw.setTextColor(Color.rgb(53, 173, 63));
                        tw.setText(category);
                        break;
                    case 2:
                        category = "Important";
                        tw.setTextColor(Color.RED);
                        tw.setText(category);
                        break;
                    case 3:
                        category = "Vip contact";
                        tw.setTextColor(Color.MAGENTA);
                        tw.setText(category);
                        break;
                }
                dialog.dismiss();
            }
        });

        dialog.setContentView(v);
        double width = getResources().getDisplayMetrics().widthPixels * 0.95;
        dialog.getWindow().setLayout((int)width, WindowManager.LayoutParams.WRAP_CONTENT);

        dialog.show();
    }
    TimePicker timePicker;
    private void showCalendar() {
        final View dialogView = View.inflate(ActivityPopupAfter.this, R.layout.date_time_picker, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityPopupAfter.this, R.style.MyAlertDialogStyle);
        timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        ScrollView sv = (ScrollView) dialogView.findViewById(R.id.scrollView);
        sv.smoothScrollTo(0, 0);
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);

                datePicker.setBackgroundColor(Color.CYAN);
                Calendar dateTime;
                if (Build.VERSION.SDK_INT < 23) {
                    dateTime = new GregorianCalendar(datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth(),
                            timePicker.getCurrentHour(),
                            timePicker.getCurrentMinute());
                } else {
                    dateTime = new GregorianCalendar(datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth(),
                            timePicker.getHour(),
                            timePicker.getMinute());
                }

                SimpleDateFormat df = new SimpleDateFormat("yyyy MMM dd HH:mm",Locale.ENGLISH);

                time = dateTime.getTimeInMillis() - System.currentTimeMillis();
                if (time > 1000) {
                    reminder = String.format("%s", df.format(dateTime.getTime()));
                    TextView tw = (TextView) findViewById(R.id.calendarText);
                    tw.setText(reminder);
                } else {
                    Toast.makeText(context, "You can't set date to past", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reminder = "";
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.create().show();
    }

    public void createNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Don't forget to add notes")
                .setContentText("Add note for last called");

        Intent resultIntent = new Intent(context, ActivityPopupAfter.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ActivityPopupAfter.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = notificationID;
        manager.notify(mNotificationId, mBuilder.build());
    }

    public void setReminder(int i,String note) {
        if (Build.VERSION.SDK_INT >= 14) {
            //finish();
            Toast.makeText(context, "Reminder saved", Toast.LENGTH_SHORT).show();
            Calendar cal = Calendar.getInstance();
            long startDate = cal.getTimeInMillis() + i * 60 * 1000;
            long endDate = cal.getTimeInMillis() + i * 60 * 1000 + 60 * 1000;
            ContentValues event = new ContentValues();
            event.put("calendar_id", 1);
            if (name == null) {
                event.put("title", "Your note for " + number + " : "+note);
            } else {
                event.put("title", "Your note for " + name + " : "+note);
            }
            event.put("eventTimezone", TimeZone.getDefault().getID());
            event.put("dtstart", startDate);
            event.put("dtend", endDate);

            event.put("allDay", 0); // 0 for false, 1 for true
            event.put("eventStatus", 1);
            event.put("hasAlarm", 1); // 0 for false, 1 for true

            String eventUriString = "content://com.android.calendar/events";
            Uri eventUri = context.getApplicationContext()
                    .getContentResolver()
                    .insert(Uri.parse(eventUriString), event);

            if (eventUri != null) {
                eventID = Long.parseLong(eventUri.getLastPathSegment());
            }

            int minutes = 0;
            // add reminder for the event
            ContentValues reminders = new ContentValues();
            reminders.put("event_id", eventID);
            reminders.put("method", "1");
            reminders.put("minutes", minutes);

            String reminderUriString = "content://com.android.calendar/reminders";
            context.getApplicationContext().getContentResolver()
                    .insert(Uri.parse(reminderUriString), reminders);

            database.createOrUpdateStatistics(fixNumber(number), 0, 0, 0, 1, 0, 0);
        } else {
            Toast.makeText(context, "Cannot add reminder, android version too low", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.close_notes, menu);
        return true;
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.date_pick, menu);
        return true;
    }*/
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_date:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    private String fixNumber(String number) {
        String Number = ""; //= number;
        if (number.length() < 2) return "";
        try {
            Number = number.replaceAll("[ ()#~!-]", "");
            String FirstNumbers = Number.substring(0, 2);
            if (FirstNumbers.equalsIgnoreCase("86")) {
                Number = "+3706" + Number.substring(2, Number.length());
            }
            if (FirstNumbers.equalsIgnoreCase("85")) {
                Number = "+3705" + Number.substring(2, Number.length());
            }
            if (FirstNumbers.equalsIgnoreCase("83")) {
                Number = "+3703" + Number.substring(2, Number.length());
            }
        } catch (Exception ex) {
            Toast.makeText(context,
                    String.valueOf(Number) + "`" + ex.toString() + "`" + String.valueOf(number),
                    Toast.LENGTH_LONG).show();
        }

        return Number;
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void chooseContact(View view) {
        Intent i = new Intent(ActivityPopupAfter.this,AllCallsActivity.class);
        startActivity(i);
    }
}

