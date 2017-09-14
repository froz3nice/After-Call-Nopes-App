package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by juseris on 6/19/2017.
 */

public class CallReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallStarted(final Context ctx, Date start) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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

    @Override
    protected void onOutgoingCallStarted(final Context ctx, Date start) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

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

    @Override
    protected void onIncomingCallEnded(Context ctx, long start, Date end) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean checkIncoming = prefs.getBoolean("incomingCheckBox", true);
        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkIncoming) {
            popupAfterShow(start, end, ctx);
        }
        sendBroadcast(ctx);
        pushIncomingToDatabases(prefs.getString("LastActiveNr", ""), ctx, start, end);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, long start, Date end) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean checkOutgoing = prefs.getBoolean("outgoingCheckBox", false);
        if (catchCall(ctx, prefs.getString("LastActiveNr", "")) && checkOutgoing) {
            popupAfterShow(start, end, ctx);
        }
        sendBroadcast(ctx);
        pushOutgoingToDatabases(prefs.getString("LastActiveNr", ""), ctx, start, end);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, long start) {
        sendBroadcast(ctx);
    }

    public boolean catchCall(Context ctx, String number) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString("callTime", String.valueOf(time)).apply();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PreferenceManager.getDefaultSharedPreferences(ctx)
                        .edit().putBoolean("haveToChooseContact", false).apply();
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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
