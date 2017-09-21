package com.example.juseris.aftercallnote;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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

    private static ArrayList<IGenericItem> parseToDates(ArrayList<IGenericItem> list) {
        ArrayList<IGenericItem> newItems = new ArrayList<>();
        for (IGenericItem item : list) {
            Date date = parseOrReturnNull(item.getDateString());
            try {
                item.setDateObject(date);
            } catch (Exception e) {
                Calendar dateTime = new GregorianCalendar(2000, 5, 5, 4, 20);
                item.setDateObject(new Date(dateTime.getTimeInMillis()));
            }
            newItems.add(item);
        }
        return newItems;
    }

    private static ArrayList<IGenericItem> parsePrestaToDates(ArrayList<IGenericItem> list) {
        ArrayList<IGenericItem> newItems = new ArrayList<>();
        for (IGenericItem item : list) {
            Date date = parsePrestaOrReturnNull(item.getDateString());
            try {
                item.setDateObject(date);
            } catch (Exception e) {
                Calendar dateTime = new GregorianCalendar(2000, 5, 5, 4, 20);
                item.setDateObject(new Date(dateTime.getTimeInMillis()));
            }
            newItems.add(item);
        }
        return newItems;
    }

    private static Date parseOrReturnNull(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
            return formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("pizdec","no parse, error");
            return null;
        }
    }
    private static Date parsePrestaOrReturnNull(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("pizdec","no parse, error");
            return null;
        }
    }

    private static ArrayList<IGenericItem> sort(ArrayList<IGenericItem> list) {
        Collections.sort(list, new Comparator<IGenericItem>() {
            @Override
            public int compare(IGenericItem a, IGenericItem b) {
                Date date2 = b.getDateObject();//parseOrReturnNull(((ClassNote) b).getDateString());
                Date date1 = a.getDateObject();// parseOrReturnNull(((ClassNote) a).getDateString());
                if (date1 == null) {
                    if (date2 == null) {
                        return 0;
                    }
                    return 1;
                }
                if (date2 == null) {
                    return -1;
                }
                return date2.compareTo(date1);
            }
        });
        Collections.reverse(list);
        return list;
    }


    public static ArrayList<IGenericItem> getSortedList(ArrayList<IGenericItem> list){
        ArrayList<IGenericItem> items = parseToDates(list);
        return sort(items);
    }

    public static ArrayList<IGenericItem> getSortedPrestaList(ArrayList<IGenericItem> list){
        ArrayList<IGenericItem> parsed = parsePrestaToDates(list);
        ArrayList<IGenericItem> sorted = sort(parsed);
        Collections.reverse(sorted);
        return sorted;
    }

}
