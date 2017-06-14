package com.example.juseris.aftercallnote;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PhoneCallReceiver extends BroadcastReceiver {
    private Context context;
    private ClassSettings Settings;
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static long callStartTime;
    private static boolean isIncoming = false;
    private static boolean wasOffHook = false;
    private static int lastState2 = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming2 = false;
    private static boolean wasOffHook2 = false;
    private Database database;

    @SuppressLint("SwitchIntDef")
    @Override
    public void onReceive(Context ctx, Intent intent) {
        context = ctx;
        String stateString = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        int st = 0;
        String nr;
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            nr = fixNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString("LastActiveNr", nr).apply();
        }else {
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateString)) {
                st = TelephonyManager.CALL_STATE_IDLE;
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateString)) {
                st = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateString)) {
                st = TelephonyManager.CALL_STATE_RINGING;
                nr = fixNumber(intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putString("LastActiveNr", nr).apply();
            }
        }
        nr = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("LastActiveNr", "");
        FirebaseApp.initializeApp(context);
        checkEveryCall(st,nr);
        //checking day of week
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String weekDay = "";
        switch (day) {
            case Calendar.SUNDAY:
                weekDay = "Sunday";
                break;
            case Calendar.MONDAY:
                weekDay = "Monday";
                break;
            case Calendar.TUESDAY:
                weekDay = "Tuesday";
                break;
            case Calendar.WEDNESDAY:
                weekDay = "Wednesday";
                break;
            case Calendar.THURSDAY:
                weekDay = "Thursday";
                break;
            case Calendar.FRIDAY:
                weekDay = "Friday";
                break;
            case Calendar.SATURDAY:
                weekDay = "Saturday";
                break;
        }

        boolean isDayChecked = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(weekDay, true);
        boolean ifChecked = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("catchCall", true);
        if (ifChecked && isDayChecked) {
            int state = 0;
            String savedNumber;
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = fixNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putString("LastActiveNr", fixNumber(savedNumber)).apply();
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putString("CallType", "outgoing").apply();
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                if ((TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr))) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                    savedNumber = fixNumber(intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putString("LastActiveNr", savedNumber).apply();

                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putString("CallType", "incoming").apply();
                }

            }
            String numb = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("LastActiveNr", "");
            //boolean isNumberChecked = Settings.getIsNumberChecked(numb);
            boolean isNumberChecked = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(numb, true);
           // boolean isNumberChecked = true;//  database.getCatchCall(numb);
            String callType = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("CallType", "");
            if (isNumberChecked) {
                switch (callType) {
                    case "incoming":
                        if (PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean("incomingCheckBox", true)) {
                            onCallStateChanged(context, state, numb);
                        }
                        break;
                    case "outgoing":
                        if (PreferenceManager.getDefaultSharedPreferences(context)
                                .getBoolean("outgoingCheckBox", false)) {
                            onCallStateChanged(context, state, numb);
                        }
                        break;
                    default:
                        onCallStateChanged(context, state, numb);
                        break;
                }
            }
    }
    }

    public void checkEveryCall(int state,final String number){
        if (lastState2 == state) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming2 = true;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                wasOffHook2 = true;
                if (isIncoming2) {
                    callStartTime = System.currentTimeMillis();
                } else {
                    callStartTime = System.currentTimeMillis();
                    isIncoming2 = false;
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                database = new Database(context);
                if (lastState2 == TelephonyManager.CALL_STATE_RINGING && !wasOffHook) {

                } else if (isIncoming2 && wasOffHook2) {
                    pushIncomingToDatabases(number);
                } else {
                    pushOutgoingToDatabases(number);
                }
                wasOffHook2 = false;
                isIncoming2 = false;
                break;
        }
        lastState2 = state;
    }

    public void onCallStateChanged(final Context ctx, int state,final String number) {
        if (lastState == state) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        //Toast.makeText(context, "hey", Toast.LENGTH_SHORT).show();
                        Database db = new Database(ctx);
                        if(!db.getDataByNumber(number).isEmpty()) {
                            Intent popUpIntent = new Intent(context, ActivityPopupBefore.class);
                            popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            popUpIntent.putExtra("NUMBER",number);
                            context.startActivity(popUpIntent);
                        }
                    }
                }, 1000);

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                wasOffHook = true;
                boolean show = PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean("purpleBox", false);
                if(show){
                    context.startService(new Intent(context, FlyingButton.class));
                }
                if (isIncoming) {
                    callStartTime = System.currentTimeMillis();
                } else {
                    callStartTime = System.currentTimeMillis();
                    isIncoming = false;
                    Log.d("LAST STATE",String.valueOf(lastState));
                    if(lastState == TelephonyManager.CALL_STATE_IDLE) {
                        Handler han = new Handler();
                        han.postDelayed(new Runnable() {
                            public void run() {
                                Database db = new Database(ctx);
                                if(!db.getDataByNumber(number).isEmpty()) {
                                    Intent popUpIntent = new Intent(context, ActivityPopupBefore.class);
                                    popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(popUpIntent);
                                }
                            }
                        }, 1000);
                    }
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(isMyServiceRunning(FlyingButton.class)) {
                    context.stopService(new Intent(context, FlyingButton.class));
                }
                database = new Database(context);
                if (lastState == TelephonyManager.CALL_STATE_RINGING && !wasOffHook) {
                    //onMissedCall(ctx, savedNumber, callStartTime);
                    sendBroadcast(ctx);
                } else if (isIncoming && wasOffHook) {
                    Popup_After_Show(callStartTime, System.currentTimeMillis());
                    sendBroadcast(ctx);
                } else {
                    Popup_After_Show(callStartTime, System.currentTimeMillis());
                    sendBroadcast(ctx);
                }

                wasOffHook = false;
                isIncoming = false;
                break;
        }
        lastState = state;
    }

    private void pushIncomingToDatabases(String number){
        long callTime = (System.currentTimeMillis() - callStartTime) / 1000;
        int time = (int) callTime;
        database.createOrUpdateStatistics(fixNumber(number), 1, 0, 0, 0, time, 0);
        String _date = getCallTime(System.currentTimeMillis());
        database.insertIncomingCall(new ContactsEntity
                (getContactName(context,number),number,_date,getCallDuration(time)));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference myRef = db.getReference();
            final DatabaseReference userRef = myRef.child("IncomingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (getContactName(context,number),number,_date,getCallDuration(time)));
        }
    }

    private void pushOutgoingToDatabases(String number){
        long callTime = (System.currentTimeMillis() - callStartTime) / 1000;
        int time = (int) callTime;
        String _date = getCallTime(System.currentTimeMillis());
        database.createOrUpdateStatistics(fixNumber(number), 0, 1, 0, 0, 0, time);
        database.insertOutgoingCall((new ContactsEntity
                (getContactName(context,number),number,_date,getCallDuration(time))));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference myRef = db.getReference();
            final DatabaseReference userRef = myRef.child("OutgoingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (getContactName(context,number),number,_date,getCallDuration(time)));
        }
    }

    public String getCallDuration(int callDuration) {
        int seconds = callDuration;
        int minutes = 0;
        while (seconds - 60 >= 0) {
            minutes++;
            seconds -= 60;
        }
        String time = String.valueOf(seconds)+" s";
        if(minutes != 0){
            time = minutes+" min "+seconds+" s";
        }
        return time;
    }

    public String getCallTime(long callTime) {
        String tm = "hey";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(callTime);
            tm = formatter.format(calendar.getTime());
        }catch (Exception e) {
            tm = "";
        }
        return tm;
    }

    public void sendBroadcast(Context ctx){
        if (ActivityPopupBefore.active) {
            Intent local = new Intent();
            local.setAction("com.hello.action");
            ctx.sendBroadcast(local);
        }
    }

    private void Popup_After_Show(long start, long end) {
        Settings = new ClassSettings(context);
        long callTime = (end - start) / 1000;
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
        String _date = String.format("%s", df.format(Calendar.getInstance().getTime()));
        database = new Database(context);

        Settings.setDate(_date);
        String time = Settings.getCallTime();
        if (time.equals("")) {
            time += callTime;
        } else {
            time += ";" + callTime;
        }
        Settings.setCallTime(time);

        SetNumberToSharedPref();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putBoolean("haveToChooseContact", false).apply();
                    Intent popUpIntent = new Intent(context, ActivityPopupAfter.class);
                    popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(popUpIntent);
                }
            }, 250);
    }

    public void SetNumberToSharedPref() {
        String numbers = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("NUMBERS", "");
        if (numbers.equals("")) {
            numbers += PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("LastActiveNr", "");
        } else {
            numbers += ";" + PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("LastActiveNr", "");
        }
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString("NUMBERS", numbers).apply();
    }

    private String fixNumber(String number) {
        String Number = null;
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
        } catch (Exception ex) {
            Log.d("PhoneContacts", ex.toString());
        }

        return Number;
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
}

