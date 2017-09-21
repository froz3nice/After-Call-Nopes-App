package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.juseris.aftercallnote.Models.CallStatisticsEntity;
import com.example.juseris.aftercallnote.Models.CategoriesAndColors;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.Models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class Database extends SQLiteOpenHelper implements Serializable {
    private Context context;

    private SQLiteDatabase noteDatabase;
    private static final String NoteDatabaseName = "noteDatabase.db";
    private static final int DataBaseVersion = 2;

    private final String TableName = "NotesTable3";
    private final String ID = "ID";
    private final String number = "number";
    private final String note = "note";
    private final String reminder = "reminder";
    private final String callDate = "callDate";
    private final String callTime = "callTime";
    private final String name = "name";
    private final String catchCall = "catchCall";
    private final String incomingCallCount = "incomingCallCount";
    private final String outgoingCallCount = "outgoingCallCount";
    private final String typedNoteCount = "typedNoteCount";
    private final String remindersAddedCount = "remindersAddedCount";
    private final String incomingTimeTotal = "incomingTimeTotal";
    private final String outgoingTimeTotal = "outgoingTimeTotal";
    private final String ContactsTableName = "ContactsTable";
    private final String contactNumber = "contactNumber";
    private final String contactName = "contactName";
    private final String callStatisticsTable = "callStatisticsTable";
    private ArrayList<IGenericItem> dataList;
    private final String ifSynced = "if_synced";
    private final String SyncedTable = "Synced_Table";
    private final String FriendEmail = "FriendEmail";
    private final String category = "Category";
    private final String incomingCallTable = "incomingTable";
    private final String outgoingCallTable = "outogingTable";
    private String categories = "categoriess";
    private String color = "colorr";
    private String PrestaShop = "prestaShop1";
    private String surname = "surname";
    private String orderState = "order_state";
    private String orderNumber = "order_number";
    private String phoneNumber = "phone_number";
    private String date = "date";
    private String PrestaShopNewItems = "newPrestashopItems";

    public Database(Context context) {
        super(context, NoteDatabaseName, null, DataBaseVersion);
        this.context = context;
        CreateDatabase();
        openDatabase();
        closeDatabase();
    }

    public void deleteSyncedNotesTable() {
        openDatabase();
        noteDatabase.delete(SyncedTable, null, null);
        closeDatabase();
    }

    private void CreateDatabase() {
        noteDatabase = this.context.openOrCreateDatabase(NoteDatabaseName, Context.MODE_PRIVATE, null);

        String SQL = "CREATE TABLE IF NOT EXISTS " + TableName +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                callTime + " TEXT, " +
                name + " TEXT, " +
                catchCall + " INTEGER, " +
                reminder + " TEXT, " +
                category + " TEXT " +
                " ) ";
        String SQL2 = "CREATE TABLE IF NOT EXISTS " + ContactsTableName +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                contactNumber + " TEXT, " +
                contactName + " TEXT " +
                " ) ";
        String SQL3 = "CREATE TABLE IF NOT EXISTS " + callStatisticsTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                incomingCallCount + " INTEGER, " +
                outgoingCallCount + " INTEGER, " +
                typedNoteCount + " INTEGER, " +
                remindersAddedCount + " INTEGER, " +
                incomingTimeTotal + " INTEGER," +
                outgoingTimeTotal + " INTEGER" +
                " ) ";

        String SQL4 = "CREATE TABLE IF NOT EXISTS " + SyncedTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT, " +
                FriendEmail + " TEXT," +
                reminder + " TEXT, " +
                category + " TEXT " +
                " ) ";
        String SQL5 = "CREATE TABLE IF NOT EXISTS " + incomingCallTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                callTime + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT " +
                " ) ";
        String SQL6 = "CREATE TABLE IF NOT EXISTS " + outgoingCallTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                callTime + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT " +
                " ) ";
        String SQL7 = "CREATE TABLE IF NOT EXISTS " + categories +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                category + " TEXT, " +
                color + " TEXT " +
                " ) ";
        String SQL8 = "CREATE TABLE IF NOT EXISTS " + PrestaShop +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                name + " TEXT, " +
                surname + " TEXT, " +
                orderState + " TEXT, " +
                orderNumber + " INTEGER, " +
                phoneNumber + " TEXT, " +
                date + " TEXT " +
                " ) ";
        String SQL9 = "CREATE TABLE IF NOT EXISTS " + PrestaShopNewItems +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                name + " TEXT, " +
                surname + " TEXT, " +
                orderState + " TEXT, " +
                orderNumber + " INTEGER, " +
                phoneNumber + " TEXT, " +
                date + " TEXT " +
                " ) ";

        noteDatabase.execSQL(SQL);
        noteDatabase.execSQL(SQL2);
        noteDatabase.execSQL(SQL3);
        noteDatabase.execSQL(SQL4);
        noteDatabase.execSQL(SQL5);
        noteDatabase.execSQL(SQL6);
        noteDatabase.execSQL(SQL7);
        noteDatabase.execSQL(SQL8);
        noteDatabase.execSQL(SQL9);
    }

    public void openDatabase() {
        if (!noteDatabase.isOpen())
            noteDatabase = this.context.openOrCreateDatabase(NoteDatabaseName, Context.MODE_PRIVATE, null);
    }

    public void closeDatabase() {
        if (noteDatabase.isOpen())
            noteDatabase.close();
    }

    public boolean getCatchCall(String nr) {
        openDatabase();

        dataList = new ArrayList<>();
        String SQL = "SELECT * FROM " + TableName + " WHERE " + number + "='" + nr + "'";
        Cursor cursor = noteDatabase.rawQuery(SQL, null);
        boolean isEmpty = true;
        if (cursor.moveToFirst()) {
            do {
                isEmpty = false;
                Integer catchCall = cursor.getInt(6);
                if (catchCall == 1) {
                    return true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        closeDatabase();
        return isEmpty;
    }

    public ArrayList<IGenericItem> getData() {
        openDatabase();

        dataList = new ArrayList<>();

        String SQL = "SELECT * FROM " + TableName;

        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String number = cursor.getString(1);
                String note = cursor.getString(2);
                String callDate = cursor.getString(3);
                String callTime = cursor.getString(4);
                String name = cursor.getString(5);
                Integer catchCall = cursor.getInt(6);
                String reminder = cursor.getString(7);
                String category = cursor.getString(8);
                //String phoneName = phoneNumbers.ReturnName(number);
                //Log.d(T,number);
                //Log.d(T,phoneNumbers.ReturnName(number));

                ClassNote classNote = new ClassNote(id);
                classNote.setPhoneNumber(number);
                classNote.setNotes(note);
                classNote.setDateString(callDate);
                classNote.setCallTime(callTime);
                classNote.setName(name);
                classNote.setCatchCall(catchCall);
                classNote.setReminder(reminder);
                classNote.setCategory(category);
                dataList.add(classNote);

            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDatabase();

        return dataList;
    }

    public ArrayList<IGenericItem> getSyncedNotesByNumber(String nmb) {
        openDatabase();
        dataList = new ArrayList<>();

        String SQL = "SELECT * FROM " + SyncedTable + " WHERE " + number + "='" + nmb + "'";
        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String nr = cursor.getString(1);
                String note = cursor.getString(2);
                String callDate = cursor.getString(3);
                String name = cursor.getString(4);
                String friendEmail = cursor.getString(5);
                String reminder = cursor.getString(6);
                String category = cursor.getString(7);
                if (!"Personal ".equals(category)) {
                    ClassNote classNote = new ClassNote(id);
                    classNote.setPhoneNumber(nr);
                    classNote.setNotes(note);
                    classNote.setDateString(callDate);
                    classNote.setName(name);
                    classNote.setSynced(1);
                    classNote.setFriendEmail(friendEmail);
                    classNote.setCategory(category);
                    classNote.setReminder(reminder);
                    dataList.add(classNote);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDatabase();

        return dataList;
    }

    public ArrayList<IGenericItem> getDataByNumber(String _number) {
        openDatabase();
        dataList = new ArrayList<>();
        String SQL = "SELECT * FROM " + TableName + " WHERE " + number + "='" + _number + "'";
        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String number = cursor.getString(1);
                String note = cursor.getString(2);
                String callDate = cursor.getString(3);
                String callTime = cursor.getString(4);
                String name = cursor.getString(5);
                Integer catchCall = cursor.getInt(6);
                String reminder = cursor.getString(7);
                String category = cursor.getString(8);

                ClassNote classNote = new ClassNote(id);
                classNote.setPhoneNumber(number);
                classNote.setNotes(note);
                classNote.setDateString(callDate);
                classNote.setCallTime(callTime);
                classNote.setName(name);
                classNote.setCatchCall(catchCall);
                classNote.setReminder(reminder);
                classNote.setCategory(category);
                dataList.add(classNote);

            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDatabase();

        return dataList;
    }

    public void updateNote(Integer id, String note, String reminder, String category) {
        openDatabase();
        if (!category.equals("")) {
            category += " ";
        }
        String SQL = "UPDATE " + TableName +
                " SET " + this.note + "='" + note + "' WHERE " + ID + "='" + id + "'";
        String sql2 = "UPDATE " + TableName +
                " SET " + this.reminder + "='" + reminder + "' WHERE " + ID + "='" + id + "'";
        String sql3 = "UPDATE " + TableName +
                " SET " + this.category + "='" + category + "' WHERE " + ID + "='" + id + "'";
        noteDatabase.execSQL(SQL);
        noteDatabase.execSQL(sql2);
        noteDatabase.execSQL(sql3);
        closeDatabase();
    }


    public void updateSyncedNote(Integer id, String note, String reminder, String category) {
        openDatabase();
        if (!category.equals("")) {
            category += " ";
        }

        String SQL = "UPDATE " + SyncedTable +
                " SET " + this.note + "='" + note + "' WHERE " + ID + "='" + id + "'";
        String sql2 = "UPDATE " + SyncedTable +
                " SET " + this.reminder + "='" + reminder + "' WHERE " + ID + "='" + id + "'";
        String sql3 = "UPDATE " + SyncedTable +
                " SET " + this.category + "='" + category + "' WHERE " + ID + "='" + id + "'";
        noteDatabase.execSQL(SQL);
        noteDatabase.execSQL(sql2);
        noteDatabase.execSQL(sql3);
        closeDatabase();
    }

    public void deleteFromSyncedTable(Integer id) {
        openDatabase();
        String SQL = "DELETE FROM " + SyncedTable + " WHERE " + ID + "='" + id + "'";
        noteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void insertNote(String number, String note, String callTime, String name,
                           Integer catchCallState, String reminder, String category) {
        openDatabase();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
        String date = String.format("%s", df.format(calendar.getTime()));
        Integer sync = 0;
        if (!category.equals("")) {
            category += " ";
        }
        String SQL = "INSERT INTO " + TableName + " ( " +
                this.number + " , " +
                this.note + " , " +
                this.callDate + " , " +
                this.callTime + " , " +
                this.name + " , " +
                this.catchCall + " , " +
                this.reminder + " , " +
                this.category +
                " ) VALUES ( '" +
                number + "' , '" +
                note + "' , '" +
                date + "' , '" +
                callTime + "' , '" +
                name + "' , '" +
                catchCallState + "' , '" +
                reminder + "' , '" +
                category +
                "' ) ";

        noteDatabase.execSQL(SQL);

        closeDatabase();
    }

    public CallStatisticsEntity getStatistics() {
        openDatabase();

        String SQL = "SELECT * FROM " + callStatisticsTable;
        Cursor cursor = noteDatabase.rawQuery(SQL, null);
        CallStatisticsEntity csl = new CallStatisticsEntity();
        int incoming = 0;
        int outgoing = 0;
        int typed = 0;
        int reminders = 0;
        int incTime = 0;
        int outTime = 0;
        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String nr = cursor.getString(1);
                Integer IncomingCallCount = cursor.getInt(2);
                Integer OutgoingCallCount = cursor.getInt(3);
                Integer TypedNoteCount = cursor.getInt(4);
                Integer RemindersAddedCount = cursor.getInt(5);
                Integer timeInc = cursor.getInt(6);
                Integer timeOut = cursor.getInt(7);
                incoming += IncomingCallCount;
                outgoing += OutgoingCallCount;
                typed += TypedNoteCount;
                reminders += RemindersAddedCount;
                outTime += timeOut;
                incTime += timeInc;
            } while (cursor.moveToNext());
        }
        csl.setIncomingCallCount(incoming);
        csl.setOutgoingCallCount(outgoing);
        csl.setTypedNoteCount(typed);
        csl.setRemindersAddedCount(reminders);
        csl.setIncomingTimeTotal(incTime);
        csl.setOutgoingTimeTotal(outTime);
        cursor.close();
        closeDatabase();
        return csl;
    }

    public CallStatisticsEntity getStatistics(String number) {
        openDatabase();

        String SQL = "SELECT * FROM " + callStatisticsTable + " WHERE " + this.number + "='" + number + "'";
        CallStatisticsEntity csl = new CallStatisticsEntity(number);
        csl.setIncomingCallCount(0);
        csl.setOutgoingCallCount(0);
        csl.setTypedNoteCount(0);
        csl.setRemindersAddedCount(0);
        csl.setOutgoingTimeTotal(0);
        csl.setIncomingTimeTotal(0);
        Cursor cursor = noteDatabase.rawQuery(SQL, null);
        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String nr = cursor.getString(1);
                Integer IncomingCallCount = cursor.getInt(2);
                Integer OutgoingCallCount = cursor.getInt(3);
                Integer TypedNoteCount = cursor.getInt(4);
                Integer RemindersAddedCount = cursor.getInt(5);
                Integer timeI = cursor.getInt(6);
                Integer timeO = cursor.getInt(7);

                csl.setNumber(nr);
                csl.setIncomingCallCount(IncomingCallCount);
                csl.setOutgoingCallCount(OutgoingCallCount);
                csl.setTypedNoteCount(TypedNoteCount);
                csl.setRemindersAddedCount(RemindersAddedCount);
                csl.setOutgoingTimeTotal(timeO);
                csl.setIncomingTimeTotal(timeI);
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDatabase();
        return csl;
    }

    public boolean createOrUpdateStatistics(String number, Integer incomingCallCount
            , Integer outgoingCallCount, Integer typedNoteCount, Integer remindersAddedCount
            , Integer incTime, Integer outTime) {
        openDatabase();
        String Query = "SELECT * FROM " + callStatisticsTable + " WHERE " + this.number + " = '" + number + "' ";
        Cursor cursor = noteDatabase.rawQuery(Query, null);
        if (cursor.moveToFirst()) {
            if (incomingCallCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + this.incomingCallCount + " = " + this.incomingCallCount + "+1" + " WHERE " + this.number + "='" + number + "'";
                noteDatabase.execSQL(SQL);
            }
            if (outgoingCallCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + this.outgoingCallCount + " = " + this.outgoingCallCount + "+1 WHERE " + this.number + "='" + number + "'";
                noteDatabase.execSQL(SQL);
            }
            if (typedNoteCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + this.typedNoteCount + " = " + this.typedNoteCount + "+1" + " WHERE " + this.number + "='" + number + "'";
                noteDatabase.execSQL(SQL);
            }
            if (remindersAddedCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + this.remindersAddedCount + " = " + this.remindersAddedCount + "+1" + " WHERE " + this.number + "='" + number + "'";
                noteDatabase.execSQL(SQL);
            }
            if (incTime != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + incomingTimeTotal + " = " + incomingTimeTotal + "+" + incTime + " WHERE " + this.number + "='" + number + "'";
                noteDatabase.execSQL(SQL);
            }
            if (outTime != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + outgoingTimeTotal + " = " + outgoingTimeTotal + "+" + outTime + " WHERE " + this.number + "='" + number + "'";
                noteDatabase.execSQL(SQL);
            }
            // return false;
        } else {
            String SQL = "INSERT INTO " + callStatisticsTable + " ( " +
                    this.number + " , " +
                    this.incomingCallCount + " , " +
                    this.outgoingCallCount + " , " +
                    this.typedNoteCount + " , " +
                    this.remindersAddedCount + " , " +
                    this.incomingTimeTotal + " , " +
                    this.outgoingTimeTotal +
                    " ) VALUES ( '" +
                    number + "' , " +
                    incomingCallCount + " , " +
                    outgoingCallCount + " , " +
                    typedNoteCount + " , " +
                    remindersAddedCount + " , " +
                    incTime + " , " +
                    outTime +
                    " ) ";

            noteDatabase.execSQL(SQL);
        }
        cursor.close();
        closeDatabase();
        return true;
    }


    public void Delete_Note(Integer id) {
        openDatabase();
        String SQL = "DELETE FROM " + TableName + " WHERE " + ID + "='" + id + "'";
        noteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void deleteInCall(ContactEntity c) {
        openDatabase();
        String SQL = "DELETE FROM "
                + incomingCallTable + " WHERE "
                + number + "='" + c.getNumber() + "' AND "
                + callTime + "='" + c.getCallDuration() + "' AND "
                + callDate + "='" + c.getCallTime() + "' AND "
                + name + "='" + c.getName() + "'";
        noteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void deleteOutCall(ContactEntity c) {
        openDatabase();
        String SQL = "DELETE FROM "
                + outgoingCallTable + " WHERE "
                + number + "='" + c.getNumber() + "' AND "
                + callTime + "='" + c.getCallDuration() + "' AND "
                + callDate + "='" + c.getCallTime() + "' AND "
                + name + "='" + c.getName() + "'";
        noteDatabase.execSQL(SQL);
        closeDatabase();
    }


    public void insertIncomingCall(ContactsEntity call) {
        openDatabase();
        String SQL = "INSERT INTO " + incomingCallTable + " ( " +
                this.number + " , " +
                this.callTime + " , " +
                this.callDate + " , " +
                this.name +
                " ) VALUES ( '" +
                call.getNumber() + "' , '" +
                call.getCallDuration() + "' , '" +
                call.getCallTime() + "' , '" +
                call.getName() +
                "' ) ";

        noteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void insertOutgoingCall(ContactsEntity call) {
        openDatabase();
        String SQL = "INSERT INTO " + outgoingCallTable + " ( " +
                this.number + " , " +
                this.callTime + " , " +
                this.callDate + " , " +
                this.name +
                " ) VALUES ( '" +
                call.getNumber() + "' , '" +
                call.getCallDuration() + "' , '" +
                call.getCallTime() + "' , '" +
                call.getName() +
                "' ) ";

        noteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public ArrayList<ContactEntity> getIncomingCalls() {
        openDatabase();
        ArrayList<ContactEntity> list = new ArrayList<>();
        String SQL = "SELECT * FROM " + incomingCallTable;
        Cursor cursor = noteDatabase.rawQuery(SQL, null);
        if (cursor.moveToLast()) {
            do {
                ContactEntity csl = new ContactEntity();
                Integer id = cursor.getInt(0);
                String nr = cursor.getString(1);
                String callTime = cursor.getString(2);
                String callDate = cursor.getString(3);
                String name = cursor.getString(4);

                csl.setNumber(nr);
                csl.setCallTime(callDate);
                csl.setCallDuration(callTime);
                csl.setName(name);
                list.add(csl);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        closeDatabase();
        return list;
    }

    public ArrayList<ContactEntity> getOutgoingCalls() {
        openDatabase();
        ArrayList<ContactEntity> list = new ArrayList<>();
        String SQL = "SELECT * FROM " + outgoingCallTable;
        Cursor cursor = noteDatabase.rawQuery(SQL, null);
        if (cursor.moveToLast()) {
            do {
                ContactEntity csl = new ContactEntity();
                Integer id = cursor.getInt(0);
                String nr = cursor.getString(1);
                String callTime = cursor.getString(2);
                String callDate = cursor.getString(3);
                String name = cursor.getString(4);

                csl.setNumber(nr);
                csl.setCallTime(callDate);
                csl.setCallDuration(callTime);
                csl.setName(name);
                list.add(csl);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        closeDatabase();
        return list;
    }

    public void deleteOutgoingCalls() {
        openDatabase();
        noteDatabase.execSQL("delete from " + outgoingCallTable);
        closeDatabase();
    }

    public void deleteIncomingCalls() {
        openDatabase();
        noteDatabase.execSQL("delete from " + incomingCallTable);
        closeDatabase();
    }

    public void insertOutgoingCalls(ArrayList<ContactEntity> calls) {
        openDatabase();
        for (ContactEntity e : calls) {
            String duration = e.getCallDuration();
            String time = e.getCallTime();
            if (!e.getCallDuration().contains("s")) {
                duration = getCallDuration(duration);
            }
            if (!e.getCallTime().contains(":")) {
                time = getCallTime(time);
            }
            String SQL = "INSERT INTO " + outgoingCallTable + " ( " +
                    this.number + " , " +
                    this.callTime + " , " +
                    this.callDate + " , " +
                    this.name +
                    " ) VALUES ( '" +
                    e.getNumber() + "' , '" +
                    duration + "' , '" +
                    time + "' , '" +
                    e.getName() +
                    "' ) ";

            noteDatabase.execSQL(SQL);
        }
        closeDatabase();
    }

    public void insertIncomingCalls(ArrayList<ContactEntity> calls) {
        openDatabase();
        for (ContactEntity e : calls) {
            String duration = e.getCallDuration();
            String time = e.getCallTime();
            if (!e.getCallDuration().contains("s")) {
                duration = getCallDuration(duration);
            }
            if (!e.getCallTime().contains(":")) {
                time = getCallTime(time);
            }
            String SQL = "INSERT INTO " + incomingCallTable + " ( " +
                    this.number + " , " +
                    this.callTime + " , " +
                    this.callDate + " , " +
                    this.name +
                    " ) VALUES ( '" +
                    e.getNumber() + "' , '" +
                    duration + "' , '" +
                    time + "' , '" +
                    e.getName() +
                    "' ) ";

            noteDatabase.execSQL(SQL);
        }
        closeDatabase();
    }

    public void insertCategoryAndColor(CategoriesAndColors cat_color) {
        openDatabase();

        String SQL = "INSERT INTO " + categories + " ( " +
                this.category + " , " +
                this.color +
                " ) VALUES ( '" +
                cat_color.getCategory() + "' , '" +
                cat_color.getColor() +
                "' ) ";
        noteDatabase.execSQL(SQL);
    }


    public ArrayList<CategoriesAndColors> getCatsAndColors() {
        openDatabase();
        String SQL = "SELECT * FROM " + categories;
        Cursor cursor = noteDatabase.rawQuery(SQL, null);
        ArrayList<CategoriesAndColors> cats = new ArrayList<>();
        if (cursor.moveToLast()) {
            do {
                Integer id = cursor.getInt(0);
                String category = cursor.getString(1);
                String color = cursor.getString(2);
                CategoriesAndColors cat = new CategoriesAndColors(category, color);
                cats.add(cat);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        Collections.reverse(cats);
        return cats;
    }

    private String getCallTime(String callTime) {
        String tm = "hey";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(callTime));
            tm = formatter.format(calendar.getTime());
        } catch (Exception e) {
            tm = "";
        }
        return tm;
    }

    public String getCallDuration(String callDuration) {
        int seconds = 0;
        int minutes = 0;
        try {
            seconds = Integer.parseInt(callDuration);
            while (seconds - 60 >= 0) {
                minutes++;
                seconds -= 60;
            }
        } catch (NumberFormatException e) {
            //Will Throw exception!
            //do something! anything to handle the exception.
        }
        String time = String.valueOf(seconds) + " s";
        if (minutes != 0) {
            time = minutes + " min " + seconds + " s";
        }
        return time;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TableName + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                callTime + " TEXT, " +
                name + " TEXT, " +
                catchCall + "INTEGER, " +
                reminder + " TEXT," +
                category + " TEXT" +
                ")");


        db.execSQL("CREATE TABLE IF NOT EXISTS " + ContactsTableName +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                contactNumber + " TEXT, " +
                contactName + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + callStatisticsTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                incomingCallCount + " INTEGER, " +
                outgoingCallCount + " INTEGER, " +
                typedNoteCount + " INTEGER, " +
                remindersAddedCount + " INTEGER, " +
                incomingTimeTotal + " INTEGER," +
                outgoingTimeTotal + " INTEGER" +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SyncedTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT, " +
                FriendEmail + " TEXT," +
                reminder + " TEXT, " +
                category + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + incomingCallTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                callTime + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + outgoingCallTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                callTime + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + categories +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                category + " TEXT, " +
                color + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PrestaShop +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                name + " TEXT, " +
                surname + " TEXT, " +
                orderState + " TEXT, " +
                orderNumber + " INTEGER, " +
                phoneNumber + " TEXT, " +
                date + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PrestaShopNewItems +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                name + " TEXT, " +
                surname + " TEXT, " +
                orderState + " TEXT, " +
                orderNumber + " INTEGER, " +
                phoneNumber + " TEXT, " +
                date + " TEXT " +
                " ) ");
    }

    public void insertPrestashopOrder(Order order) {
        openDatabase();
        String SQL = "INSERT OR REPLACE INTO  " + PrestaShop + " ( " +
                this.name + " , " +
                this.surname + " , " +
                this.orderState + " , " +
                this.orderNumber + " , " +
                this.phoneNumber + " , " +
                this.date +
                " ) VALUES ( '" +
                order.getName() + "' , '" +
                order.getSurname() + "' , '" +
                order.getOrder_state() + "' , '" +
                order.getOrder_nr() + "' , '" +
                order.getPhone_nr() + "' , '" +
                order.getDateString() +
                "' ) ";
        noteDatabase.execSQL(SQL);

        sendBroadcast(context);
        closeDatabase();
    }

    public void insertNewPrestaOrder(Order order) {
        openDatabase();
        String SQL = "INSERT OR REPLACE INTO " + PrestaShopNewItems + " ( " +
                this.name + " , " +
                this.surname + " , " +
                this.orderState + " , " +
                this.orderNumber + " , " +
                this.phoneNumber + " , " +
                this.date +
                " ) VALUES ( '" +
                order.getName() + "' , '" +
                order.getSurname() + "' , '" +
                order.getOrder_state() + "' , '" +
                order.getOrder_nr() + "' , '" +
                order.getPhone_nr() + "' , '" +
                order.getDateString() +
                "' ) ";

        noteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public ArrayList<IGenericItem> getNewPrestaByNr(String nr, String originalNr) {
        openDatabase();
        ArrayList<IGenericItem> orders = new ArrayList<>();
        String SQL = "SELECT * FROM " + PrestaShopNewItems + " WHERE "
                + this.phoneNumber + "='" + nr + "' OR " +
                this.phoneNumber + " = '" + originalNr + "'";
        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String name = cursor.getString(1);
                String surname = cursor.getString(2);
                String orderState = cursor.getString(3);
                String orderNumber = cursor.getString(4);
                String phoneNumber = cursor.getString(5);
                String date = cursor.getString(6);
                if (name.equals("")) {
                    name = phoneNumber;
                    surname = "";
                }
                Order order = new Order(name, surname, phoneNumber, orderNumber, orderState, date);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //closeDatabase();
        return orders;
    }


    public ArrayList<IGenericItem> getPrestashopByNr(String nr, String originalNr) {
        openDatabase();
        ArrayList<IGenericItem> orders = new ArrayList<>();
        String SQL = "SELECT * FROM " + PrestaShop + " WHERE "
                + this.phoneNumber + "='" + nr + "' OR " +
                this.phoneNumber + " = '" + originalNr + "'";
        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String name = cursor.getString(1);
                String surname = cursor.getString(2);
                String orderState = cursor.getString(3);
                String orderNumber = cursor.getString(4);
                String phoneNumber = cursor.getString(5);
                String date = cursor.getString(6);
                if (name.equals("")) {
                    name = phoneNumber;
                    surname = "";
                }
                Order order = new Order(name, surname, phoneNumber, orderNumber, orderState, date);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //closeDatabase();
        return orders;
    }

    public ArrayList<IGenericItem> getPrestashopData() {
        openDatabase();
        ArrayList<IGenericItem> orders = new ArrayList<>();
        String SQL = "SELECT * FROM " + PrestaShop;
        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String name = cursor.getString(1);
                String surname = cursor.getString(2);
                String orderState = cursor.getString(3);
                String orderNumber = cursor.getString(4);
                String phoneNumber = cursor.getString(5);
                String date = cursor.getString(6);
                Order order = new Order(name, surname, phoneNumber, orderNumber, orderState, date);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //closeDatabase();

        return orders;
    }

    public ArrayList<IGenericItem> getNewPrestashopData() {
        openDatabase();
        ArrayList<IGenericItem> orders = new ArrayList<>();
        String SQL = "SELECT * FROM " + PrestaShopNewItems;
        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String name = cursor.getString(1);
                String surname = cursor.getString(2);
                String orderState = cursor.getString(3);
                String orderNumber = cursor.getString(4);
                String phoneNumber = cursor.getString(5);
                String date = cursor.getString(6);
                Order order = new Order(name, surname, phoneNumber, orderNumber, orderState, date);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //closeDatabase();

        return orders;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertSyncedData(ArrayList<ClassNote> syncedNotes) {
        openDatabase();
        for (ClassNote note : syncedNotes) {
            String SQL = "INSERT INTO " + TableName + " ( " +
                    this.number + " , " +
                    this.note + " , " +
                    this.callDate + " , " +
                    this.callTime + " , " +
                    this.name + " , " +
                    this.catchCall + " , " +
                    this.reminder + " , " +
                    this.category +
                    " ) VALUES ( '" +
                    note.getPhoneNumber() + "' , '" +
                    note.getNotes(true) + "' , '" +
                    note.getDateString() + "' , '" +
                    note.getCallTime() + "' , '" +
                    note.getName() + "' , '" +
                    note.getCatchCall() + "' , '" +
                    note.getReminder() + "' , '" +
                    note.getCategory() +
                    "' ) ";

            noteDatabase.execSQL(SQL);
        }
        sendBroadcast(context);
        closeDatabase();
    }

    public void sendBroadcast(Context ctx) {
        Intent local = new Intent();
        local.setAction("com.hello.updateList");
        ctx.sendBroadcast(local);
    }

    public void insertToSyncedTable(ArrayList<ClassNote> syncedNotes) {
        openDatabase();

        for (ClassNote note : syncedNotes) {
            String SQL = "INSERT INTO " + SyncedTable + " ( " +
                    this.number + " , " +
                    this.note + " , " +
                    this.callDate + " , " +
                    this.name + " , " +
                    this.FriendEmail + " , " +
                    this.reminder + " , " +
                    this.category +
                    " ) VALUES ( '" +
                    note.getPhoneNumber() + "' , '" +
                    note.getNotes(true) + "' , '" +
                    note.getDateString() + "' , '" +
                    note.getName() + "' , '" +
                    note.getFriendEmail() + "' , '" +
                    note.getReminder() + "' , '" +
                    note.getCategory() +
                    "' ) ";

            noteDatabase.execSQL(SQL);
        }
        //closeDatabase();
    }

    public ArrayList<IGenericItem> getSyncedData() {
        openDatabase();

        dataList = new ArrayList<>();

        String SQL = "SELECT * FROM " + SyncedTable;

        Cursor cursor = noteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String number = cursor.getString(1);
                String note = cursor.getString(2);
                String callDate = cursor.getString(3);
                String name = cursor.getString(4);
                String email = cursor.getString(5);
                String reminder = cursor.getString(6);
                String category = cursor.getString(7);
                ClassNote classNote = new ClassNote(id);
                if (!category.equals("Personal ")) {
                    classNote.setPhoneNumber(number);
                    classNote.setNotes(note);
                    classNote.setDateString(callDate);
                    classNote.setName(name);
                    classNote.setSynced(1);
                    classNote.setFriendEmail(email);
                    classNote.setCategory(category);
                    classNote.setReminder(reminder);

                    dataList.add(classNote);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        //closeDatabase();

        return dataList;
    }
}
