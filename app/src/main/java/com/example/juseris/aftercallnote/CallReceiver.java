package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.ActivityPopupBefore;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by juseris on 6/19/2017.
 */

public class CallReceiver extends PhoneCallReceiver {

    @Override
    protected void onIncomingCallStarted(final Context ctx,final String number, Date start) {
        boolean checkIncoming = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("incomingCheckBox",true);
        if(catchCall(ctx,number) && checkIncoming) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    //Toast.makeText(context, "hey", Toast.LENGTH_SHORT).show();
                    Database db = new Database(ctx);
                    if (!db.getDataByNumber(number).isEmpty()) {
                        Intent popUpIntent = new Intent(ctx, ActivityPopupBefore.class);
                        popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        popUpIntent.putExtra("NUMBER", number);
                        ctx.startActivity(popUpIntent);
                    }
                }
            }, 1000);
        }
    }

    @Override
    protected void onOutgoingCallStarted(final Context ctx,final String number, Date start) {
        boolean checkOutgoing = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("outgoingCheckBox",false);

        if(catchCall(ctx,number) && checkOutgoing) {
            Handler han = new Handler();
            han.postDelayed(new Runnable() {
                public void run() {
                    Database db = new Database(ctx);
                    if (!db.getDataByNumber(number).isEmpty()) {
                        Intent popUpIntent = new Intent(ctx, ActivityPopupBefore.class);
                        popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(popUpIntent);
                    }
                }
            }, 1000);
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, long start, Date end) {
        boolean checkIncoming = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("incomingCheckBox",true);
        if(catchCall(ctx,number) && checkIncoming) {
            popup_After_Show(start, end, ctx);
        }
        sendBroadcast(ctx);
        pushIncomingToDatabases(number,ctx,start,end);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, long start, Date end) {
        boolean checkOutgoing = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("outgoingCheckBox",false);
        if(catchCall(ctx,number) && checkOutgoing) {
            popup_After_Show(start, end, ctx);
        }
        sendBroadcast(ctx);
        pushOutgoingToDatabases(number,ctx,start,end);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, long start) {
        sendBroadcast(ctx);
    }

    public boolean catchCall(Context ctx,String number){
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
        boolean isDayChecked = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(weekDay, true);
        boolean ifChecked = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean("catchCall", true);
        boolean isNumberChecked = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(number, true);
        return isDayChecked && ifChecked && isNumberChecked;
    }


    private void popup_After_Show(long dateStart,Date dateEnd,final Context ctx) {
        ClassSettings Settings = new ClassSettings(ctx);
        long callTime = (dateEnd.getTime() - dateStart) / 1000;
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
        String _date = String.format("%s", df.format(dateEnd));
        database = new Database(ctx);

        Settings.setDate(_date);
        String time = Settings.getCallTime();
        if (time.equals("")) {
            time += callTime;
        } else {
            time += ";" + callTime;
        }
        Settings.setCallTime(time);

        setNumberToSharedPref(ctx);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
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
    private void pushIncomingToDatabases(String number,Context ctx, long start, Date end){
        database = new Database(ctx);
        long callTime = (end.getTime() - start) / 1000;
        int time = (int) callTime;
        database.createOrUpdateStatistics(fixNumber(number), 1, 0, 0, 0, time, 0);
        String _date = getCallTime(System.currentTimeMillis());
        database.insertIncomingCall(new ContactsEntity
                (getContactName(ctx,number),number,_date,getCallDuration(time)));
        FirebaseApp.initializeApp(ctx);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference myRef = db.getReference();
            final DatabaseReference userRef = myRef.child("IncomingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (getContactName(ctx,number),number,_date,getCallDuration(time)));
        }
    }

    private void pushOutgoingToDatabases(String number,Context ctx, long start, Date end){
        long callTime = (end.getTime() - start) / 1000;
        int time = (int) callTime;
        if(time < 5){
            time = 0;
        }
        String _date = getCallTime(System.currentTimeMillis());
        database = new Database(ctx);
        database.createOrUpdateStatistics(fixNumber(number), 0, 1, 0, 0, 0, time);
        database.insertOutgoingCall((new ContactsEntity
                (getContactName(ctx,number),number,_date,getCallDuration(time))));
        FirebaseApp.initializeApp(ctx);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            email = user.getEmail();
            String fixedEmail = email.replace(".", ",");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference myRef = db.getReference();
            final DatabaseReference userRef = myRef.child("OutgoingCalls").child(fixedEmail);
            userRef.push().setValue(new ContactsEntity
                    (getContactName(ctx,number),number,_date,getCallDuration(time)));
        }
    }

}
