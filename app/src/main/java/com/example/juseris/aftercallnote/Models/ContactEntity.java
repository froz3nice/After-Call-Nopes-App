package com.example.juseris.aftercallnote.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by juseris on 3/17/2017.
 */

public class ContactEntity  {
    private String name;
    private String number;
    private String callTime;
    private String callDuration;



    public ContactEntity(String name, String number
            , String callTime, String callDuration) {
        this.number = number;
        this.name = name;
        this.callTime = callTime;
        this.callDuration = callDuration;
    }

    public ContactEntity() {

    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public String getCallTime() {
        return callTime;
    }
    public String exportString() {
        return String.format("Phone number: %s\nName: %s\nDate: %s\nCall Duration: %s\n",
                getNumber(),
                getName(),
                getCallTime(),
                getCallDuration()
        );
    }
    public String toExcel() {
        return String.format("%s,%s,%s,%s",
                getNumber(),
                getName(),
                getCallTime(),
                getCallDuration());
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

}
