package com.example.juseris.aftercallnote.Models;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;

import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ClassNote implements Parcelable,IGenericItem {

    private int id;
    private String phoneNumber;
    private String notes;
    private String name;
    private String lastName;
    private String callTime;
    private String dateString;
    private String email;
    private int catchCall;
    private String reminder;
    private int isSynced;
    public boolean isSection;
    private Date dateObject;
    private String timeTitle;
    private String categoryTitle;

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
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getNotes(Boolean Shorted) {
        Integer msgSize = 90;
        if (notes == null) return "";

        if (Shorted) {
            if (notes.length() < msgSize)
                return notes;
            else
                return notes.substring(0, msgSize);
        }

        return notes;
    }

    public Date getDate(){
        DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
        Date date2 = new Date();
        try {
            date2 = formatter.parse(getDateString());
        } catch (ParseException ignored) {

        }
        return date2;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        if (phoneNumber == null || phoneNumber.equalsIgnoreCase("null")) return "";
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (name == null || name.equalsIgnoreCase("null")) return "";
        return name;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getCallTime() {
        if (callTime == null || callTime.equalsIgnoreCase("null")) return "";
        return callTime;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    @Override
    public String getDateString() {
        if (dateString == null || dateString.equalsIgnoreCase("null")) return "";
        return dateString;
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
                id,
                phoneNumber,
                name,
                notes,
                callTime);
    }

    public String toExcel() {
        if(isSynced() == 0){
            return String.format("%s,%s,%s,%s,%s",
                    getPhoneNumber(),
                    getName(),
                    getDateString(),
                    getNotes(false),
                    getCategory());
        }else{
            String fixedEmail = getFriendEmail().replace(",",".");
            return String.format("%s,%s,%s,%s,%s,%s",
                    getPhoneNumber(),
                    getName(),
                    getDateString(),
                    getNotes(false),
                    getCategory(),
                    "Synced note from "+fixedEmail);
        }
    }

    // Export string (returning phone number, name, date, note)
    public String ExportString() {
        if (isSynced() == 0) {
            return String.format("Phone number: %s\nname: %s\nDate: %s\nNote: %s\nCategory: %s\n",
                    getPhoneNumber(),
                    getName(),
                    getDateString(),
                    getNotes(false),
                    getCategory()
            );
        } else {
            String fixedEmail = getFriendEmail().replace(",",".");
            return String.format("Phone number: %s\nname: %s\nDate: %s\nNote: %s\nCategory: %s\nSynced note with %s\n",
                    getPhoneNumber(),
                    getName(),
                    getDateString(),
                    getNotes(false),
                    getCategory(),
                    fixedEmail
            );
        }
    }

    protected ClassNote(Parcel in) {
        phoneNumber = in.readString();
        notes = in.readString();
        name = in.readString();
        lastName = in.readString();
        callTime = in.readString();
        dateString = in.readString();
        email = in.readString();
        id = in.readInt();
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
        dest.writeString(phoneNumber);
        dest.writeString(notes);
        dest.writeString(name);
        dest.writeString(lastName);
        dest.writeString(callTime);
        dest.writeString(dateString);
        dest.writeString(email);
        dest.writeInt(id);
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

    public Date getDateObject() {
        return dateObject;
    }


    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public String getTimeTitle() {

        int seconds = 0;
        int minutes = 0;
        try {
            seconds = Integer.parseInt(getCallTime());
            while (seconds - 60 >= 0) {
                minutes++;
                seconds -= 60;
            }
        } catch (NumberFormatException e) {
            //Will Throw exception!
            //do something! anything to handle the exception.
        }

        final String OLD_FORMAT = "MMM dd HH:mm";
        final String NEW_FORMAT = "MMM dd HH:mm";
        Date d = null;

        String minAndSecTitle;
        String onlySecTitle;
        String plainTitle;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, Locale.ENGLISH);
            d = sdf.parse(getDateString());
            sdf.applyPattern(NEW_FORMAT);
            String newDateString = sdf.format(d);
            minAndSecTitle = newDateString + " , " + minutes + " min " + seconds + " s";
            onlySecTitle = newDateString + " , " + seconds + " s";
            plainTitle = newDateString;
        } catch (ParseException e) {
            e.printStackTrace();
            minAndSecTitle = getDateString()
                    + " , " + minutes + " min " + seconds + " s";
            onlySecTitle = getDateString()
                    + " , " + seconds + " s";
            plainTitle = getDateString();
        }
        if (minutes != 0) {
            return minAndSecTitle;
        } else {
            if (seconds == 0) {
                return plainTitle;
            } else {
                return onlySecTitle;
            }
        }
    }

    public void setTimeTitle(String timeTitle) {
        this.timeTitle = timeTitle;
    }
    private void setCategoryColor(Context context,Drawable background,int color){
        try {
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(context, color));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(ContextCompat.getColor(context, color));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(ContextCompat.getColor(context, color));
            }
        }catch (Exception e) {
            String hexColor = String.format("#%06X", (0xFFFFFF & color));
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable) background).getPaint().setColor(Color.parseColor(String.valueOf(hexColor)));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(Color.parseColor(String.valueOf(hexColor)));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(Color.parseColor(String.valueOf(hexColor)));
            }
        }
    }

    private String makeCategoryTitle(Context context,Drawable background) {
        String cat = category;//categories stored in db with space in the end
        if (cat.equals("Personal ")) {
            setCategoryColor(context, background, R.color.personal);
            return "PERSONAL";
        } else if (cat.equals("Important ")) {
            setCategoryColor(context, background, R.color.important);
            return "IMPORTANT";
        } else {
            /*Database db = new Database(context);
            ArrayList<CategoriesAndColors> cats = db.getCatsAndColors();
            for (CategoriesAndColors cat_color : cats) {
                if (cat_color.getCategory().equals(cat.substring(0, cat.length() - 1))) {

                }
            }*/
            setCategoryColor(context, background, Color.BLUE);
            return cat.substring(0, cat.length() - 1);

        }
    }

    public String getCategoryTitle(Context context,Drawable background) {
        return makeCategoryTitle(context,background);
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }
}
