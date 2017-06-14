package com.example.juseris.aftercallnote.Models;

/**
 * Created by juseris on 3/17/2017.
 */


public class ContactsEntity {
    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    private String name;

    public void setNumber(String number) {
        this.number = number;
    }

    private String number;

    public void setName(String name) {
        this.name = name;
    }

    private String callTime;
    private String callDuration;

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public ContactsEntity() {}

    public ContactsEntity(String name, String number
            , String callTime, String callDuration) {
        this.number = number;
        this.name = name;
        this.callTime = callTime;
        this.callDuration = callDuration;
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
}