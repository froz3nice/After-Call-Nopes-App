package com.example.juseris.aftercallnote.Models;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntegerRes;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.widget.TextView;

import com.example.juseris.aftercallnote.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClassNote implements Parcelable {

    private int ID;
    private String PhoneNumber;
    // uzrasas, kuris buvo pridetas
    private String Notes;
    // Zmogaus vardas, tas pats kas su last name... prideti buttona editui ir kad keisti varda
    private String Name;
    // Zmogaus last name (reik prideti buttona editint informacijai, ir prideti toki field'a
    private String LastName;
    // Laikas, kiek buvo kalbeta
    private String CallTime;
    // Data, kada buvo pradeta kalbeti
    private String CallDate;
    private String Email;
    private String City;
    private int catchCall;
    private String reminder;
    private int isSynced;
    public boolean isSection;

    public String getCategory() {
        return category;
    }

    private String category;

    public String getFriendEmail() {
        return friendEmail;
    }

    private String friendEmail;

    public int isSynced() {
        return isSynced;
    }

    public void setSynced(int synced) {
        isSynced = synced;
    }


    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public ClassNote() {
    }

    public ClassNote(int id) {
        ID = id;
    }

    public int getID() {
        return ID;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }
    public String getNotes(Boolean Shorted) {
        Integer msgSize = 90;
        if (Notes == null) return "";

        if (Shorted) {
            if (Notes.length() < msgSize)
                return Notes;
            else
                return Notes.substring(0, msgSize);
        }

        return Notes;
    }

    public Date getDate(){
        DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
        Date date2 = new Date();
        try {
            date2 = formatter.parse(getCallDate());
        } catch (ParseException e) {

        }
        return date2;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        if (PhoneNumber == null || PhoneNumber.equalsIgnoreCase("null")) return "";
        return PhoneNumber;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        if (Name == null || Name.equalsIgnoreCase("null")) return "";
        return Name;
    }

    public void setCallTime(String callTime) {
        CallTime = callTime;
    }

    public String getCallTime() {
        if (CallTime == null || CallTime.equalsIgnoreCase("null")) return "";
        return CallTime;
    }

    public void setCallDate(String callDate) {
        CallDate = callDate;
    }

    public String getCallDate() {
        if (CallDate == null || CallDate.equalsIgnoreCase("null")) return "";
        return CallDate;
    }

    public void setCatchCall(int catchCall) {
        this.catchCall = catchCall;
    }

    public int getCatchCall() {
        return catchCall;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s @ %s @ %s @ %s",
                ID,
                PhoneNumber,
                Name,
                Notes,
                CallTime);
    }

    public String toExcel() {
        if(isSynced() == 0){
            return String.format("%s,%s,%s,%s,%s",
                    getPhoneNumber(),
                    getName(),
                    getCallDate(),
                    getNotes(false),
                    getCategory());
        }else{
            String fixedEmail = getFriendEmail().replace(",",".");
            return String.format("%s,%s,%s,%s,%s,%s",
                    getPhoneNumber(),
                    getName(),
                    getCallDate(),
                    getNotes(false),
                    getCategory(),
                    "Synced note from "+fixedEmail);
        }
    }

    // Export string (returning phone number, name, date, note)
    public String ExportString() {
        if (isSynced() == 0) {
            return String.format("Phone number: %s\nName: %s\nDate: %s\nNote: %s\nCategory: %s\n",
                    getPhoneNumber(),
                    getName(),
                    getCallDate(),
                    getNotes(false),
                    getCategory()
            );
        } else {
            String fixedEmail = getFriendEmail().replace(",",".");
            return String.format("Phone number: %s\nName: %s\nDate: %s\nNote: %s\nCategory: %s\nSynced note with %s\n",
                    getPhoneNumber(),
                    getName(),
                    getCallDate(),
                    getNotes(false),
                    getCategory(),
                    fixedEmail
            );
        }
    }

    protected ClassNote(Parcel in) {
        PhoneNumber = in.readString();
        Notes = in.readString();
        Name = in.readString();
        LastName = in.readString();
        CallTime = in.readString();
        CallDate = in.readString();
        Email = in.readString();
        City = in.readString();
        ID = in.readInt();
        catchCall = in.readInt();
        reminder =in.readString();
        isSynced = in.readInt();
        friendEmail = in.readString();
        category = in.readString();
    }



    public static final Creator<ClassNote> CREATOR = new Creator<ClassNote>() {
        @Override
        public ClassNote createFromParcel(Parcel in) {
            return new ClassNote(in);
        }

        @Override
        public ClassNote[] newArray(int size) {
            return new ClassNote[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(PhoneNumber);
        dest.writeString(Notes);
        dest.writeString(Name);
        dest.writeString(LastName);
        dest.writeString(CallTime);
        dest.writeString(CallDate);
        dest.writeString(Email);
        dest.writeString(City);
        dest.writeInt(ID);
        dest.writeInt(catchCall);
        dest.writeString(reminder);
        dest.writeInt(isSynced);
        dest.writeString(friendEmail);
        dest.writeString(category);
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
