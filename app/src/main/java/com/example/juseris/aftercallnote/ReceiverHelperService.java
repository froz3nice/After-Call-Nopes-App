package com.example.juseris.aftercallnote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.example.juseris.aftercallnote.UtilsPackage.DateUtils;
import com.example.juseris.aftercallnote.UtilsPackage.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Juozas on 2017.10.01.
 */

public class ReceiverHelperService extends Service implements PhoneCallReceiver.Listener {

    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onCallStateChanged(Context context,int state,String nr) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt("state", state).apply();
        if (prefs.getInt("lastState", 0) == prefs.getInt("state", 0)) {
            return;
        }
        //String nr = prefs.getString("LastActiveNr","");
        Calendar calendar = Calendar.getInstance();
        switch (prefs.getInt("state", 0)) {
            case TelephonyManager.CALL_STATE_RINGING:
                new DateUtils(context).removeWrongData();
                prefs.edit().putLong("lastIncomingCallTime",calendar.getTimeInMillis()).apply();
                prefs.edit().putString("LastActiveNr", nr).apply();
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
                    new DateUtils(context).removeWrongData();
                    prefs.edit().putLong("lastOutgoingCallTime",calendar.getTimeInMillis()).apply();
                    prefs.edit().putBoolean("isIncoming", false).apply();
                    prefs.edit().putLong("callStartTime", new Date().getTime()).apply();
                    onOutgoingCallStarted(context, new Date());
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (Utils.isMyServiceRunning(FlyingButton.class, context)) {
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    private void onIncomingCallStarted(final Context ctx, Date start) {
        boolean checkIncoming = prefs.getBoolean("incomingCheckBox", true);
        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkIncoming) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
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
                (Utils.getContactName(ctx, number), number, _date, getCallDuration(time)));
        DatabaseReference myRef = Utils.getDatabase().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            final DatabaseReference userRef = myRef.child("IncomingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (Utils.getContactName(ctx, number), number, _date, getCallDuration(time)));
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
                (Utils.getContactName(ctx, number), number, _date, getCallDuration(time))));
        DatabaseReference myRef = Utils.getDatabase().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            final DatabaseReference userRef = myRef.child("OutgoingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (Utils.getContactName(ctx, number), number, _date, getCallDuration(time)));
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

}
