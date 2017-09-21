package com.example.juseris.aftercallnote;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PhoneCallReceiver extends BroadcastReceiver {

    private SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            String nr = Utils.fixNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            prefs.edit().putString("LastActiveNr", nr).apply();
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            //String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_IDLE;
                prefs.edit().putInt("state", state).apply();

            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {

                state = TelephonyManager.CALL_STATE_OFFHOOK;
                prefs.edit().putInt("state", state).apply();

            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
                prefs.edit().putInt("state", state).apply();
                String nr = Utils.fixNumber(intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
                prefs.edit().putString("LastActiveNr", nr).apply();
            }
            onCallStateChanged(context);
        }
    }

    public void onCallStateChanged(Context context) {
        if (prefs.getInt("lastState", 0) == prefs.getInt("state", 0)) {
            return;
        }
        //String nr = prefs.getString("LastActiveNr","");
        switch (prefs.getInt("state", 0)) {
            case TelephonyManager.CALL_STATE_RINGING:
                prefs.edit().putBoolean("isIncoming", true).apply();
                prefs.edit().putLong("callStartTime", new Date().getTime()).apply();
                onIncomingCallStarted(context, new Date());
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                boolean show = prefs.getBoolean("purpleBox", false);
                if (show) {
                    context.startService(new Intent(context, FlyingButton.class));
                }
                if (prefs.getInt("lastState", 0) != TelephonyManager.CALL_STATE_RINGING) {
                    prefs.edit().putBoolean("isIncoming", false).apply();
                    prefs.edit().putLong("callStartTime", new Date().getTime()).apply();
                    onOutgoingCallStarted(context, new Date());
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (isMyServiceRunning(FlyingButton.class, context)) {
                    context.stopService(new Intent(context, FlyingButton.class));
                }
                if (prefs.getInt("lastState", 0) == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context);
                } else {
                    if (prefs.getBoolean("isIncoming", true)) {
                        onIncomingCallEnded(context, prefs.getLong("callStartTime", new Date().getTime()), new Date());
                    } else {
                        onOutgoingCallEnded(context, prefs.getLong("callStartTime", new Date().getTime()), new Date());
                    }
                }
                break;
        }
        prefs.edit().putInt("lastState", prefs.getInt("state", 0)).apply();
    }

    public String getCallDuration(int callDuration) {
        int seconds = callDuration;
        int minutes = 0;
        while (seconds - 60 >= 0) {
            minutes++;
            seconds -= 60;
        }
        String time = String.valueOf(seconds) + " s";
        if (minutes != 0) {
            time = minutes + " min " + seconds + " s";
        }
        return time;
    }

    public String getCallTime(long callTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(callTime);
            return formatter.format(calendar.getTime());
        } catch (Exception e) {
            return "";
        }
    }

    public void sendBroadcast(Context ctx) {
        if (ActivityPopupBefore.active) {
            Intent local = new Intent();
            local.setAction("com.braz.close");
            ctx.sendBroadcast(local);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void onIncomingCallStarted(final Context ctx, Date start) {
        boolean checkIncoming = prefs.getBoolean("incomingCheckBox", true);
        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkIncoming) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    //Toast.makeText(context, "hey", Toast.LENGTH_SHORT).show();
                    Database db = new Database(ctx);

                    boolean isNotEmpty = !db.getDataByNumber(prefs.getString("LastActiveNr", "")).isEmpty() ||
                            !db.getSyncedNotesByNumber(prefs.getString("LastActiveNr", "")).isEmpty() ||
                            !db.getPrestashopByNr(prefs.getString("LastActiveNr", ""), prefs.getString("LastActiveNr", "")).isEmpty() ||
                            !db.getNewPrestaByNr(prefs.getString("LastActiveNr", ""), prefs.getString("LastActiveNr", "")).isEmpty();
                    if (isNotEmpty) {
                        Intent popUpIntent = new Intent(ctx, ActivityPopupBefore.class);
                        popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        popUpIntent.putExtra("NUMBER", prefs.getString("LastActiveNr", ""));
                        ctx.startActivity(popUpIntent);
                    }
                }
            }, 1000);
        }
    }

    private void onOutgoingCallStarted(final Context ctx, Date start) {
        boolean checkOutgoing = prefs.getBoolean("outgoingCheckBox", false);

        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkOutgoing) {
            Handler han = new Handler();
            han.postDelayed(new Runnable() {
                public void run() {
                    Database db = new Database(ctx);
                    boolean isNotEmpty = !db.getDataByNumber(prefs.getString("LastActiveNr", "")).isEmpty() ||
                            !db.getSyncedNotesByNumber(prefs.getString("LastActiveNr", "")).isEmpty() ||
                            !db.getPrestashopByNr(prefs.getString("LastActiveNr", ""), prefs.getString("LastActiveNr", "")).isEmpty() ||
                            !db.getNewPrestaByNr(prefs.getString("LastActiveNr", ""), prefs.getString("LastActiveNr", "")).isEmpty();
                    if (isNotEmpty) {
                        Intent popUpIntent = new Intent(ctx, ActivityPopupBefore.class);
                        popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        popUpIntent.putExtra("NUMBER", prefs.getString("LastActiveNr", ""));
                        ctx.startActivity(popUpIntent);
                    }
                }
            }, 1000);
        }
    }

    private void onIncomingCallEnded(Context ctx, long start, Date end) {
        boolean checkIncoming = prefs.getBoolean("incomingCheckBox", true);
        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkIncoming) {
            popupAfterShow(start, end, ctx);
        }
        sendBroadcast(ctx);
        pushIncomingToDatabases(prefs.getString("LastActiveNr", ""), ctx, start, end);
    }

    private void onOutgoingCallEnded(Context ctx, long start, Date end) {
        boolean checkOutgoing = prefs.getBoolean("outgoingCheckBox", false);
        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkOutgoing) {
            popupAfterShow(start, end, ctx);
        }
        sendBroadcast(ctx);
        pushOutgoingToDatabases(prefs.getString("LastActiveNr", ""), ctx, start, end);
    }

    private void onMissedCall(Context ctx) {
        sendBroadcast(ctx);
    }

    public boolean catchCall(Context ctx, String number) {
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
        boolean isDayChecked = prefs.getBoolean(weekDay, true);
        boolean ifChecked = prefs.getBoolean("catchCall", true);
        boolean isNumberChecked = prefs.getBoolean(number, true);
        return isDayChecked && ifChecked && isNumberChecked;
    }


    private void popupAfterShow(long dateStart, Date dateEnd, final Context ctx) {
        long callTime = (dateEnd.getTime() - dateStart) / 1000;
        int time = (int) callTime;
        prefs.edit().putString("callTime", String.valueOf(time)).apply();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                prefs.edit().putBoolean("haveToChooseContact", false).apply();
                Intent popUpIntent = new Intent(ctx, ActivityPopupAfter.class);
                popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(popUpIntent);
            }
        }, 250);
    }

    private Database database;

    private void pushIncomingToDatabases(String number, Context ctx, long start, Date end) {
        database = new Database(ctx);
        long callTime = (end.getTime() - start) / 1000;
        int time = (int) callTime;
        database.createOrUpdateStatistics(Utils.fixNumber(number), 1, 0, 0, 0, time, 0);
        String _date = getCallTime(System.currentTimeMillis());
        database.insertIncomingCall(new ContactsEntity
                (getContactName(ctx, number), number, _date, getCallDuration(time)));
        DatabaseReference myRef = Utils.getDatabase().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            final DatabaseReference userRef = myRef.child("IncomingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (getContactName(ctx, number), number, _date, getCallDuration(time)));
        }
        //linear chart statistics
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int tomorrow;
        if (today == 7) {
            tomorrow = 1;
        } else {
            tomorrow = today + 1;
        }
        //for stats in linear graph
        if (prefs.getInt("incomingTemp." + today, 0) != 0) {
            prefs.edit().putInt("incoming." + today, 0).apply();
            prefs.edit().putInt("incomingTemp." + today, 0).apply();
        }
        //prefs.edit().putInt("outgoing."+tomorrow,0).apply();
        prefs.edit().putInt("incoming." + today, prefs.getInt("incoming." + today, 0) + 1).apply();
        if (prefs.getInt("incoming." + tomorrow, 0) != 0) {
            prefs.edit().putInt("incomingTemp." + tomorrow, prefs.getInt("incoming." + tomorrow, 0)).apply();
        }

    }

    private void pushOutgoingToDatabases(String number, Context ctx, long start, Date end) {
        long callTime = (end.getTime() - start) / 1000;
        int time = (int) callTime;
        if (time < 5) {
            time = 0;
        }
        String _date = getCallTime(System.currentTimeMillis());
        database = new Database(ctx);
        database.createOrUpdateStatistics(Utils.fixNumber(number), 0, 1, 0, 0, 0, time);
        database.insertOutgoingCall((new ContactsEntity
                (getContactName(ctx, number), number, _date, getCallDuration(time))));
        DatabaseReference myRef = Utils.getDatabase().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            final DatabaseReference userRef = myRef.child("OutgoingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (getContactName(ctx, number), number, _date, getCallDuration(time)));
        }

        //linear chart statistics
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int tomorrow;
        if (today == 7) {
            tomorrow = 1;
        } else {
            tomorrow = today + 1;
        }

        if (prefs.getInt("outgoingTemp." + today, 0) != 0) {
            prefs.edit().putInt("outgoing." + today, 0).apply();
            prefs.edit().putInt("outgoingTemp." + today, 0).apply();
        }
        //prefs.edit().putInt("outgoing."+tomorrow,0).apply();
        prefs.edit().putInt("outgoing." + today, prefs.getInt("outgoing." + today, 0) + 1).apply();
        if (prefs.getInt("outgoing." + tomorrow, 0) != 0) {
            prefs.edit().putInt("outgoingTemp." + tomorrow, prefs.getInt("outgoing." + tomorrow, 0)).apply();
        }
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

