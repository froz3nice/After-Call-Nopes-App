package com.example.juseris.aftercallnote;

import android.os.AsyncTask;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by juseris on 8/23/2017.
 */
public class Utils {
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static String fixNumber(String number) {
        String nr = ""; //= number;
        if (number.length() < 2) return "";
        try {
            nr = number.replaceAll("[ ()#~!-]", "").trim();
            String firstNumbers = nr.substring(0, 2);
            if (firstNumbers.equalsIgnoreCase("86")) {
                nr = "+3706" + nr.substring(2, nr.length());
            }
            if (firstNumbers.equalsIgnoreCase("85")) {
                nr = "+3705" + nr.substring(2, nr.length());
            }
        } catch (Exception ex) {
            nr = number.replaceAll("[ ()#~!-]", "").trim();
        }
        return nr;
    }

}
