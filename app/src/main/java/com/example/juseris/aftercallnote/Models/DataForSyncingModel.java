package com.example.juseris.aftercallnote.Models;

/**
 * Created by juseris on 2/21/2017.
 */

public class DataForSyncingModel {

    private String PhoneNumber;
    private String Notes;
    private String CallDate;
    private String category;
    public DataForSyncingModel(){}

    public DataForSyncingModel(String callDate, String notes, String phoneNumber,String cat) {
        CallDate = callDate;
        Notes = notes;
        PhoneNumber = phoneNumber;
        category = cat;
    }

    public String getCallDate() {
        return CallDate;
    }

    public void setCallDate(String callDate) {
        CallDate = callDate;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
