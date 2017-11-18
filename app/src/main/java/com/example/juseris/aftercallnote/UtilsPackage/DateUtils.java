package com.example.juseris.aftercallnote.UtilsPackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by Juozas on 2017.11.17.
 */

public class DateUtils {
    Context context;
    public DateUtils(Context context) {
        this.context = context;
    }

    public void removeWrongData(){
        Calendar c = Calendar.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Long lastOutgoingCallTime = prefs.getLong("lastOutgoingCallTime",c.getTimeInMillis());
        c.setTimeInMillis(lastOutgoingCallTime);

        int outgoingValuesToDelete;
        Calendar calForNow = Calendar.getInstance();

        // deleting if there weren't any calls in a time period

        if(calForNow.get(Calendar.DAY_OF_YEAR) < c.get(Calendar.DAY_OF_YEAR)){
            outgoingValuesToDelete = calForNow.get(Calendar.DAY_OF_YEAR) - (365 - c.get(Calendar.DAY_OF_YEAR));
        }else {
            outgoingValuesToDelete = calForNow.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR);
        }

        Long lastIncomingCallTime = prefs.getLong("lastIncomingCallTime",calForNow.getTimeInMillis());
        c.setTimeInMillis(lastIncomingCallTime);
        int incomingValuesToDelete;
        if(calForNow.get(Calendar.DAY_OF_YEAR) < c.get(Calendar.DAY_OF_YEAR)){
            incomingValuesToDelete = calForNow.get(Calendar.DAY_OF_YEAR) - (365 - c.get(Calendar.DAY_OF_YEAR));
        }else {
            incomingValuesToDelete = calForNow.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR);
        }

        int day = calForNow.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < 7; i++) {
            if(outgoingValuesToDelete > 0){
                prefs.edit().putInt("outgoing." + day, 0).apply();
                outgoingValuesToDelete--;
            }
            if(incomingValuesToDelete > 0){
                prefs.edit().putInt("incoming." + day, 0).apply();
                incomingValuesToDelete--;
            }
            day--;
            if (day < 1) {
                day = 7;
            }
        }
    }
}
