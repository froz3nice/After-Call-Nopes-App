package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;

/**
 * Created by Juozas on 2017.11.17.
 */

public class StyledTimePicker extends TimePicker {

    public StyledTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        Class<?> internalRID = null;
        try {
            internalRID = Class.forName("com.android.internal.R$id");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Field hour = null;
        try {
            hour = internalRID.getField("hour");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        NumberPicker npMonth = null;
        try {
            npMonth = (NumberPicker) findViewById(hour.getInt(null));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Field minute = null;
        try {
            minute = internalRID.getField("minute");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        NumberPicker npDay = null;
        try {
            npDay = (NumberPicker) findViewById(minute.getInt(null));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Field year = null;
        try {
            year = internalRID.getField("amPm");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        NumberPicker npYear = null;
        try {
            npYear = (NumberPicker) findViewById(year.getInt(null));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Class<?> numberPickerClass = null;
        try {
            numberPickerClass = Class.forName("android.widget.NumberPicker");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Field selectionDivider = null;
        try {
            selectionDivider = numberPickerClass.getDeclaredField("mSelectionDivider");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            selectionDivider.setAccessible(true);
            selectionDivider.set(npMonth, getResources().getDrawable(
                    R.color.white));
            selectionDivider.set(npDay, getResources().getDrawable(
                    R.color.white));
            selectionDivider.set(npYear, getResources().getDrawable(
                    R.color.white));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
