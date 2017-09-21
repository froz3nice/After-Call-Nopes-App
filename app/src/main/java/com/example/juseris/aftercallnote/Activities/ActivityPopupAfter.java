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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Adapters.CustomListAdapterDialog;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.FlyingButton;
import com.example.juseris.aftercallnote.KeyboardUtil;
import com.example.juseris.aftercallnote.Models.CategoriesAndColors;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityPopupAfter extends AppCompatActivity {
    private Context context;
    private EditText textNote;
    private String name = "";
    private Database database = null;
    private final Integer catchCall = 1;
    private NotificationManager manager;
    private String number = "";
    private final Integer notificationID = 0x156;
    private String reminder = "";
    private long eventID = 0;
    private long time = 0;
    private String category = "";
    private TextView tw_category;
    private TextView tw_reminder;
    private Dialog dialog;
    CheckBox box;
    private SharedPreferences prefs;
    private TextView name_number;
    private TextView add_edit_note;
    private int minute;
    private int hour;


    // private Spinner spinner;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

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
        final View viewGroup = findViewById(R.id.rootView);
        KeyboardUtil keyboardUtil = new KeyboardUtil(this, viewGroup);
        keyboardUtil.enable();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        database = new Database(context);
        textNote = (EditText) findViewById(R.id.ac_note);
        tw_category = (TextView) findViewById(R.id.category);
        tw_reminder = (TextView) findViewById(R.id.reminder);


        Button buttonAdd = (Button) findViewById(R.id.ac_addNote);
        buttonAdd.bringToFront();
        if (isMyServiceRunning(FlyingButton.class)) {
            context.stopService(new Intent(context, FlyingButton.class));
        }
        //Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/hover.ttf");
        //textNote.setTypeface(custom_font);
        //textNote.getBackground().clearColorFilter();
        //textNote.setHintTextColor(Color.parseColor("#545252"));

        //textNote.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        add_edit_note = (TextView) findViewById(R.id.add_edit_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.afterToolBar);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        //toolbar height
        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);
        box = (CheckBox) findViewById(R.id.toolbarCheckBox);
        name_number = (TextView) findViewById(R.id.name_number);
        box.setVisibility(View.VISIBLE);
        // View categoryLayout = findViewById(R.id.categoryLayout);
        //View calendarLayout = findViewById(R.id.calendarLayout);
        dialog = new Dialog(this);
        dialog.setTitle("Select category");
       /* categoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        calendarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        showCategoryPopup();
        showCalendar();*/

        final String nrs = prefs.getString("NUMBERS", "");
        if (getIntent().getStringExtra("PhoneNumber") == null) {
            number = prefs.getString("LastActiveNr", "");
            createNotification();
        } else {
            number = Utils.fixNumber(getIntent().getStringExtra("PhoneNumber"));
        }

        if (number != null) {
            if (number != null && !number.equals("")) {
                name = getContactName(context, number);
            }
        }
        setTitle("");
        if (getIntent().getStringExtra("category") != null) {
            category = getIntent().getStringExtra("category");
        }
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit note
                if (getIntent().getStringExtra("Note") != null) {
                    if (!textNote.getText().toString().isEmpty()) {
                        String input = textNote.getText().toString();
                        if (getIntent().getIntExtra("isSynced", 0) == 0) {
                            database.updateNote(getIntent().getIntExtra("ID", 0), input, reminder, category);
                        } else {
                            database.updateSyncedNote(getIntent().getIntExtra("ID", 0), input, reminder, category);
                        }

                        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(textNote.getWindowToken(), 0);
                        Intent returnIntent = new Intent();
                        setResult(AppCompatActivity.RESULT_OK, returnIntent);
                        if (time > 1000) {
                            setReminder((int) time / 60000, textNote.getText().toString());
                        }
                        finish();
                    } else {
                        Toast.makeText(context, "Please add note to save", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //add note
                    if (textNote.getText().toString().isEmpty()) {
                        Toast.makeText(context, "Please add note to save", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!number.equals("")) {
                            String callTime = PreferenceManager.getDefaultSharedPreferences(context).getString("callTime", "");
                            database.insertNote(
                                    number,
                                    textNote.getText().toString(),
                                    callTime, name, catchCall, reminder, category);
                            FirebaseConnection con = new FirebaseConnection(context);
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
                            database.createOrUpdateStatistics(number, 0, 0, 1, 0, 0, 0);
                            Intent returnIntent = new Intent();
                            setResult(AppCompatActivity.RESULT_OK, returnIntent);
                            if (time > 1000) {
                                setReminder((int) time / 60000, textNote.getText().toString());
                            }
                            finish();
                            if (MainActivity.active) {
                                Intent i = new Intent(ActivityPopupAfter.this, MainActivity.class);
                                startActivity(i);
                            }
                            manager.cancel(notificationID);
                        } else {
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
        }, 1000 * 60 * 5);
    }

    private void setCategoryColor(int color, String title) {
        Drawable background = tw_category.getBackground();
        try {
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(context, color));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(ContextCompat.getColor(context, color));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(ContextCompat.getColor(context, color));
            }
        } catch (Exception e) {
            String hexColor = String.format("#%06X", (0xFFFFFF & color));
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable) background).getPaint().setColor(Color.parseColor(String.valueOf(hexColor)));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(Color.parseColor(String.valueOf(hexColor)));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(Color.parseColor(String.valueOf(hexColor)));
            }
        }
        tw_category.setText(title);
        tw_category.setVisibility(View.VISIBLE);
    }


    private AlertDialog alertDialog() {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setMessage("AfterCallNotes will not show and ask Notes for this contact anymore.\n\nYou can change this in current contact page.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        prefs.edit().putBoolean(number, false).apply();
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
        if (!prefs.getString("ChosenNumber", "").equals("")) {
            number = Utils.fixNumber(prefs.getString("ChosenNumber", ""));
            box.setChecked(prefs
                    .getBoolean(number, true));
            name = getContactName(context, number);
            if (name.equals("")) {
                name_number.setText(number);
            } else {
                name_number.setText(name);
            }
            prefs.edit().putString("ChosenNumber", "").apply();
        }

        if (prefs.getBoolean("haveToChooseContact", false)) {
            TextView tw = (TextView) findViewById(R.id.choose_contact);
            tw.setVisibility(View.VISIBLE);
            box.setVisibility(View.GONE);
            name_number.setVisibility(View.GONE);
            number = "";
        } else {
            TextView tw = (TextView) findViewById(R.id.choose_contact);
            tw.setVisibility(View.GONE);
            box.setVisibility(View.VISIBLE);
            name_number.setVisibility(View.VISIBLE);
        }

        if (getIntent().getStringExtra("Note") != null) {
            if (getIntent().getStringExtra("category") != null) {
                String cat = getIntent().getStringExtra("category");
                tw_category.setVisibility(View.VISIBLE);
                if (cat.length() > 1) {
                    switch (cat) {
                        case "":
                            tw_category.setVisibility(View.GONE);
                            break;
                        case "Personal ":
                            setCategoryColor(R.color.personal, "PERSONAL");
                            category = cat.substring(0, cat.length() - 1);
                            break;
                        case "Important ":
                            setCategoryColor(R.color.important, "IMPORTANT");
                            category = cat.substring(0, cat.length() - 1);
                            break;
                        default:
                            category = cat;
                            setCategoryColor(Color.BLUE, cat.substring(0, cat.length() - 1));
                            break;
                    }
                } else {
                    tw_category.setVisibility(View.GONE);
                }
            } else {
                tw_category.setVisibility(View.GONE);
            }
            textNote.setText(getIntent().getStringExtra("Note"));
            textNote.setSelection(textNote.getText().length());
            if (name.equals("")) {
                name_number.setText(number);
            } else {
                name_number.setText(name);
            }

            add_edit_note.setText("Edit Note");

        } else {
            if (name.equals("")) {
                name_number.setText(number);
            } else {
                name_number.setText(name);
            }
            add_edit_note.setText("Add Note");
        }
        box.setChecked(prefs.getBoolean(number, true));
        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (box.isChecked()) {
                    prefs.edit().putBoolean(number, true).apply();
                } else {
                    AlertDialog dialog = alertDialog();
                    dialog.show();
                    double width = getResources().getDisplayMetrics().widthPixels * 0.95;
                    dialog.getWindow().setLayout((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
                }
            }
        });
    }

    TimePicker timePicker;
    CalendarDay chosenDate = CalendarDay.today();

    public void createNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_plius)
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

    public void setReminder(int i, String note) {
        if (Build.VERSION.SDK_INT >= 14) {
            //finish();
            Toast.makeText(context, "Reminder saved", Toast.LENGTH_SHORT).show();
            Calendar cal = Calendar.getInstance();
            long startDate = cal.getTimeInMillis() + i * 60 * 1000;
            long endDate = cal.getTimeInMillis() + i * 60 * 1000 + 60 * 1000;
            ContentValues event = new ContentValues();
            event.put("calendar_id", 1);
            if (name == null) {
                event.put("title", "Your note for " + number + " : " + note);
            } else {
                event.put("title", "Your note for " + name + " : " + note);
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

            database.createOrUpdateStatistics(Utils.fixNumber(number), 0, 0, 0, 1, 0, 0);
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
        Intent i = new Intent(ActivityPopupAfter.this, AllCallsActivity.class);
        startActivity(i);
    }

    int color = Color.BLUE;

    private ArrayList<String> createTitles() {
        ArrayList<CategoriesAndColors> cats = database.getCatsAndColors();
        final ArrayList<String> titles = new ArrayList<>();

        titles.add("No category");
        titles.add("PERSONAL (will not sync)");
        titles.add("IMPORTANT");
        for (CategoriesAndColors cat : cats) {
            titles.add(cat.getCategory());
        }
        titles.add("+ add new category");
        return titles;
    }

    public void showCategoryPopup(View view) {
        if (getIntent().getIntExtra("isSynced", 0) == 0) {
            final ArrayList<String> titles = createTitles();
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
                    // TextView tw = (TextView)findViewById(R.id.categoryText);
                    if (position == 0) {
                        tw_category.setVisibility(View.GONE);
                        category = "";
                    } else if (position == 1) {
                        category = "Personal";
                        setCategoryColor(R.color.personal, "PERSONAL");
                    } else if (position == 2) {
                        category = "Important";
                        setCategoryColor(R.color.important, "IMPORTANT");
                    }
                    for (int i = 3; i < titles.size() + 1; i++) {
                        if (position == i) {
                            if (i == titles.size() - 1) {
                                View v = getLayoutInflater().inflate(R.layout.edittext_add_user, null);
                                final EditText textInputNote = (EditText) v.findViewById(R.id.add_user);
                                final AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityPopupAfter.this);
                                dialog.setTitle("Enter a new category");
                                dialog.setView(textInputNote);
                                textInputNote.setTextColor(Color.BLACK);
                                textInputNote.setSelection(textInputNote.length());
                           /* dialog.setNeutralButton("Choose color", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ColorPickerDialogBuilder
                                            .with(ActivityPopupAfter.this)
                                            .setTitle("Choose color")
                                            .initialColor(Color.BLUE)
                                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                            .density(12)
                                            .setPositiveButton("ok", new ColorPickerClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                                    color = selectedColor;
                                                }
                                            })
                                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .build()
                                            .show();
                                }
                            });*/
                                dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String input = textInputNote.getText().toString().toUpperCase();
                                        database.insertCategoryAndColor(new CategoriesAndColors(input, String.valueOf(color)));
                                    }
                                });
                                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                dialog.create().show();
                                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                            } else {
                                ArrayList<CategoriesAndColors> cats = database.getCatsAndColors();
                                int a = 3;
                                for (CategoriesAndColors cat : cats) {
                                    if (a == i) {
                                        category = cat.getCategory();
                                        setCategoryColor(Integer.parseInt(cat.getColor()), category);
                                    }
                                    a++;
                                }
                            }
                        }
                    }

                    dialog.dismiss();
                }
            });

            dialog.setContentView(v);
            double width = getResources().getDisplayMetrics().widthPixels * 0.95;
            dialog.getWindow().setLayout((int) width, WindowManager.LayoutParams.WRAP_CONTENT);

            dialog.show();
        } else {
            Toast.makeText(context, "Sorry, you can't change the category of synced note", Toast.LENGTH_LONG).show();
        }
    }

    public void showCalendar(View view) {

        final View dialogView = View.inflate(ActivityPopupAfter.this, R.layout.date_time_picker, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityPopupAfter.this, R.style.MyAlertDialogStyle);

        MaterialCalendarView mcv = (MaterialCalendarView) dialogView.findViewById(R.id.calendarView);
        ;
        mcv.state().edit()
                .setFirstDayOfWeek(Calendar.WEDNESDAY)
                .setMinimumDate(CalendarDay.from(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                ))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        mcv.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                chosenDate = date;
            }
        });

        timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        ScrollView sv = (ScrollView) dialogView.findViewById(R.id.scrollView);
        sv.smoothScrollTo(0, 0);
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //datePicker.setBackgroundColor(Color.CYAN);
                Calendar dateTime;
                if (Build.VERSION.SDK_INT < 23) {
                    dateTime = new GregorianCalendar(chosenDate.getYear(),
                            chosenDate.getMonth(),
                            chosenDate.getDay(),
                            timePicker.getCurrentHour(),
                            timePicker.getCurrentMinute());
                } else {
                    dateTime = new GregorianCalendar(chosenDate.getYear(),
                            chosenDate.getMonth(),
                            chosenDate.getDay(),
                            timePicker.getHour(),
                            timePicker.getMinute());
                }

                SimpleDateFormat df = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.ENGLISH);

                time = dateTime.getTimeInMillis() - System.currentTimeMillis();
                if (time > 1000) {
                    reminder = String.format("%s", df.format(dateTime.getTime()));
                    int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13
                            , getResources().getDisplayMetrics());
                    Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_reminder_icon, null);
                    img.setBounds(0, 0, imgSize, imgSize);
                    tw_reminder.setCompoundDrawables(img, null, null, null);
                    tw_reminder.setText(reminder.substring(5, reminder.length()));
                    tw_reminder.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(context, "Sorry, you can't set date to past", Toast.LENGTH_SHORT).show();
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
}

