package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.juseris.aftercallnote.Models.CallStatisticsEntity;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.Models.ContactsEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.Serializable;
import java.util.Locale;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class Database extends SQLiteOpenHelper implements Serializable {
    private Context context;

    private SQLiteDatabase NoteDatabase;

    private static final String NoteDatabaseName = "NoteDatabase.db";
    private static final int DataBaseVersion = 3;

    private final String TableName = "NotesTable3";
    private final String ID = "ID";
    private final String number = "number";
    private final String note = "note";
    private final String Reminder = "Reminder";
    private final String callDate = "callDate";
    private final String callTime = "callTime";
    private final String name = "name";
    private final String catchCall = "catchCall";
    private final String IncomingCallCount = "incomingCallCount";
    private final String OutgoingCallCount = "outgoingCallCount";
    private final String TypedNoteCount = "typedNoteCount";
    private final String RemindersAddedCount = "remindersAddedCount";
    private final String IncomingTimeTotal = "incomingTimeTotal";
    private final String OutgoingTimeTotal = "outgoingTimeTotal";
    private final String ContactsTableName = "ContactsTable";
    private final String ContactNumber = "ContactNumber";
    private final String ContactName = "ContactName";
    private final String callStatisticsTable = "callStatisticsTable";
    private ArrayList<ClassNote> dataList;
    private final String ifSynced = "if_synced";
    private final String SyncedTable = "Synced_Table";
    private final String FriendEmail = "FriendEmail";
    private final String category = "Category";
    private final String incomingCallTable = "incomingTable";
    private final String outgoingCallTable = "outogingTable";

    public Database(Context context) {
        super(context, NoteDatabaseName, null, DataBaseVersion);
        this.context = context;
        CreateDatabase();
        openDatabase();
        closeDatabase();
    }

    public void deleteSyncedNotesTable() {
        openDatabase();
        NoteDatabase.delete(SyncedTable, null, null);
        closeDatabase();
    }

    private void CreateDatabase() {
        NoteDatabase = this.context.openOrCreateDatabase(NoteDatabaseName, Context.MODE_PRIVATE, null);

        String SQL = "CREATE TABLE IF NOT EXISTS " + TableName +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                callTime + " TEXT, " +
                name + " TEXT, " +
                catchCall + " INTEGER, " +
                Reminder + " TEXT, " +
                category+" TEXT "+
                " ) ";

        String SQL2 = "CREATE TABLE IF NOT EXISTS " + ContactsTableName +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContactNumber + " TEXT, " +
                ContactName + " TEXT " +
                " ) ";
        String SQL3 = "CREATE TABLE IF NOT EXISTS " + callStatisticsTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                IncomingCallCount + " INTEGER, " +
                OutgoingCallCount + " INTEGER, " +
                TypedNoteCount + " INTEGER, " +
                RemindersAddedCount + " INTEGER, " +
                IncomingTimeTotal + " INTEGER," +
                OutgoingTimeTotal + " INTEGER" +
                " ) ";

        String SQL4 = "CREATE TABLE IF NOT EXISTS " + SyncedTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT, " +
                FriendEmail +" TEXT,"+
                category+" TEXT "+
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
        NoteDatabase.execSQL(SQL);
        NoteDatabase.execSQL(SQL2);
        NoteDatabase.execSQL(SQL3);
        NoteDatabase.execSQL(SQL4);
        NoteDatabase.execSQL(SQL5);
        NoteDatabase.execSQL(SQL6);
    }

    public void openDatabase() {
        if (!NoteDatabase.isOpen())
            NoteDatabase = this.context.openOrCreateDatabase(NoteDatabaseName, Context.MODE_PRIVATE, null);
    }

    public void closeDatabase() {
        if (NoteDatabase.isOpen())
            NoteDatabase.close();
    }

    public boolean getCatchCall(String nr) {
        openDatabase();

        dataList = new ArrayList<>();
        String SQL = "SELECT * FROM " + TableName + " WHERE " + number + "='" + nr + "'";
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);
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
        //closeDatabase();
        return isEmpty;
    }

    public ArrayList<ClassNote> getData() {
        openDatabase();

        dataList = new ArrayList<>();

        String SQL = "SELECT * FROM " + TableName;

        Cursor cursor = NoteDatabase.rawQuery(SQL, null);

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
                classNote.setCallDate(callDate);
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

    public ArrayList<ClassNote> getSyncedNotesByNumber(String nmb){
        openDatabase();
        dataList = new ArrayList<>();

        String SQL = "SELECT * FROM " + SyncedTable + " WHERE " + number + "='" + nmb + "'";
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String nr = cursor.getString(1);
                String note = cursor.getString(2);
                String callDate = cursor.getString(3);
                String name = cursor.getString(4);
                String friendEmail = cursor.getString(5);
                String category = cursor.getString(6);
                if(!"Personal ".equals(category)) {
                    ClassNote classNote = new ClassNote(id);
                    classNote.setPhoneNumber(nr);
                    classNote.setNotes(note);
                    classNote.setCallDate(callDate);
                    classNote.setName(name);
                    classNote.setSynced(1);
                    classNote.setFriendEmail(friendEmail);
                    classNote.setCategory(category);
                    classNote.setReminder("");
                    dataList.add(classNote);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        closeDatabase();

        return dataList;
    }

    public ArrayList<ClassNote> getDataByNumber(String _number) {
        openDatabase();
        dataList = new ArrayList<>();
        String SQL = "SELECT * FROM " + TableName + " WHERE " + number + "='" + _number + "'";
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);

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
                classNote.setCallDate(callDate);
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

    public void updateCatchCall(String number, Integer catchCallState) {
        openDatabase();
        String SQL = "UPDATE " + TableName +
                " SET " + catchCall + "=" + catchCallState + " WHERE " + this.number + "='" + number + "'";
        NoteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void Update_Note(Integer id, String note,String reminder,String category) {
        openDatabase();
        if(!category.equals("")){
            category+=" ";
        }
        String SQL = "UPDATE " + TableName +
                " SET " + this.note + "='" + note + "' WHERE " + ID + "='" + id + "'";
        String sql2 = "UPDATE " + TableName +
                " SET " + this.Reminder + "='" + reminder + "' WHERE " + ID + "='" + id + "'";
        String sql3 = "UPDATE " + TableName +
                " SET " + this.category + "='" + category + "' WHERE " + ID + "='" + id + "'";
        NoteDatabase.execSQL(SQL);
        NoteDatabase.execSQL(sql2);
        NoteDatabase.execSQL(sql3);
        closeDatabase();
    }

    public void deleteFromSyncedTable(Integer id){
        openDatabase();
        String SQL = "DELETE FROM " + SyncedTable + " WHERE " + ID + "='" + id + "'";
        NoteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void Insert_Note(String number, String note, String callTime, String name,
                            Integer catchCallState, String reminder,String category) {
        openDatabase();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
        String date = String.format("%s", df.format(calendar.getTime()));
        Integer sync = 0;
        if(!category.equals("")){
            category+=" ";
        }
        String SQL = "INSERT INTO " + TableName + " ( " +
                this.number + " , " +
                this.note + " , " +
                this.callDate + " , " +
                this.callTime + " , " +
                this.name + " , " +
                this.catchCall + " , " +
                this.Reminder + " , "+
                this.category+
                " ) VALUES ( '" +
                number + "' , '" +
                note + "' , '" +
                date + "' , '" +
                callTime + "' , '" +
                name + "' , '" +
                catchCallState + "' , '" +
                reminder + "' , '"+
                category+
                "' ) ";

        NoteDatabase.execSQL(SQL);

        closeDatabase();
    }

    public CallStatisticsEntity getStatistics() {
        openDatabase();

        String SQL = "SELECT * FROM " + callStatisticsTable;
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);
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
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);
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
        Cursor cursor = NoteDatabase.rawQuery(Query, null);
        if (cursor.moveToFirst()) {
            if (incomingCallCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + this.IncomingCallCount + " = " + this.IncomingCallCount + "+1" + " WHERE " + this.number + "='" + number + "'";
                NoteDatabase.execSQL(SQL);
            }
            if (outgoingCallCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + OutgoingCallCount + " = " + OutgoingCallCount + "+1 WHERE " + this.number + "='" + number + "'";
                NoteDatabase.execSQL(SQL);
            }
            if (typedNoteCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + TypedNoteCount + " = " + TypedNoteCount + "+1" + " WHERE " + this.number + "='" + number + "'";
                NoteDatabase.execSQL(SQL);
            }
            if (remindersAddedCount != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + RemindersAddedCount + " = " + RemindersAddedCount + "+1" + " WHERE " + this.number + "='" + number + "'";
                NoteDatabase.execSQL(SQL);
            }
            if (incTime != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + IncomingTimeTotal + " = " + IncomingTimeTotal + "+" + incTime + " WHERE " + this.number + "='" + number + "'";
                NoteDatabase.execSQL(SQL);
            }
            if (outTime != 0) {
                String SQL = "UPDATE " + callStatisticsTable +
                        " SET " + OutgoingTimeTotal + " = " + OutgoingTimeTotal + "+" + outTime + " WHERE " + this.number + "='" + number + "'";
                NoteDatabase.execSQL(SQL);
            }
            // return false;
        } else {
            String SQL = "INSERT INTO " + callStatisticsTable + " ( " +
                    this.number + " , " +
                    this.IncomingCallCount + " , " +
                    this.OutgoingCallCount + " , " +
                    this.TypedNoteCount + " , " +
                    this.RemindersAddedCount + " , " +
                    this.IncomingTimeTotal + " , " +
                    this.OutgoingTimeTotal +
                    " ) VALUES ( '" +
                    number + "' , " +
                    incomingCallCount + " , " +
                    outgoingCallCount + " , " +
                    typedNoteCount + " , " +
                    remindersAddedCount + " , " +
                    incTime + " , " +
                    outTime +
                    " ) ";

            NoteDatabase.execSQL(SQL);
        }
        cursor.close();
        closeDatabase();
        return true;
    }


    public void Delete_Note(Integer id) {
        openDatabase();
        String SQL = "DELETE FROM " + TableName + " WHERE " + ID + "='" + id + "'";
        NoteDatabase.execSQL(SQL);
        closeDatabase();
    }
    public void deleteInCall(ContactEntity c) {
        openDatabase();
        String SQL = "DELETE FROM "
                + incomingCallTable + " WHERE "
                + number  + "='" + c.getNumber() + "' AND "
                + callTime + "='" +c.getCallDuration()+"' AND "
                + callDate + "='" +c.getCallTime()+"' AND "
                + name     + "='" +c.getName()+"'" ;
        NoteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public void deleteOutCall(ContactEntity c) {
        openDatabase();
        String SQL = "DELETE FROM "
                + outgoingCallTable + " WHERE "
                + number  + "='" + c.getNumber() + "' AND "
                + callTime + "='" +c.getCallDuration()+"' AND "
                + callDate + "='" +c.getCallTime()+"' AND "
                + name     + "='" +c.getName()+"'" ;
        NoteDatabase.execSQL(SQL);
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

        NoteDatabase.execSQL(SQL);
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

        NoteDatabase.execSQL(SQL);
        closeDatabase();
    }

    public ArrayList<ContactEntity> getIncomingCalls(){
        openDatabase();
        ArrayList<ContactEntity> list = new ArrayList<>();
        String SQL = "SELECT * FROM " + incomingCallTable ;
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);
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

    public ArrayList<ContactEntity> getOutgoingCalls(){
        openDatabase();
        ArrayList<ContactEntity> list = new ArrayList<>();
        String SQL = "SELECT * FROM " + outgoingCallTable ;
        Cursor cursor = NoteDatabase.rawQuery(SQL, null);
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
    public void deleteOutgoingCalls(){
        NoteDatabase.execSQL("delete from "+ outgoingCallTable);
    }
    public void deleteIncomingCalls(){
        NoteDatabase.execSQL("delete from "+ incomingCallTable);
    }
    public void insertOutgoingCalls(ArrayList<ContactEntity> calls){
        openDatabase();
        for(ContactEntity e : calls) {
            String duration = e.getCallDuration();
            String time = e.getCallTime();
            if(!e.getCallDuration().contains("s")){
                duration = getCallDuration(duration);
            }
            if(!e.getCallTime().contains(":")){
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

            NoteDatabase.execSQL(SQL);
        }
        closeDatabase();
    }

    public void insertIncomingCalls(ArrayList<ContactEntity> calls){
        openDatabase();
        for(ContactEntity e : calls) {
            String duration = e.getCallDuration();
            String time = e.getCallTime();
            if(!e.getCallDuration().contains("s")){
                duration = getCallDuration(duration);
            }
            if(!e.getCallTime().contains(":")){
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

            NoteDatabase.execSQL(SQL);
        }
        closeDatabase();
    }
    private String getCallTime(String callTime) {
        String tm = "hey";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(callTime));
            tm = formatter.format(calendar.getTime());
        }catch (Exception e) {
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
        String time = String.valueOf(seconds)+" s";
        if(minutes != 0){
            time = minutes+" min "+seconds+" s";
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
                Reminder + " TEXT,"+
                category+" TEXT"+
                ")");


        db.execSQL("CREATE TABLE IF NOT EXISTS " + ContactsTableName +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContactNumber + " TEXT, " +
                ContactName + " TEXT " +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + callStatisticsTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                IncomingCallCount + " INTEGER, " +
                OutgoingCallCount + " INTEGER, " +
                TypedNoteCount + " INTEGER, " +
                RemindersAddedCount + " INTEGER, " +
                IncomingTimeTotal + " INTEGER," +
                OutgoingTimeTotal + " INTEGER" +
                " ) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SyncedTable +
                " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                number + " TEXT, " +
                note + " TEXT, " +
                callDate + " TEXT, " +
                name + " TEXT, " +
                FriendEmail+" TEXT,"+
                category+" TEXT "+
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
                    this.Reminder + " , " +
                    this.category+
                    " ) VALUES ( '" +
                    note.getPhoneNumber() + "' , '" +
                    note.getNotes(true) + "' , '" +
                    note.getCallDate() + "' , '" +
                    note.getCallTime() + "' , '" +
                    note.getName() + "' , '" +
                    note.getCatchCall() + "' , '" +
                    note.getReminder() + "' , '" +
                    note.getCategory()+
                    "' ) ";

            NoteDatabase.execSQL(SQL);
        }
        sendBroadcast(context);
        closeDatabase();
    }

    public void sendBroadcast(Context ctx){
        Intent local = new Intent();
        local.setAction("com.hello.updateList");
        ctx.sendBroadcast(local);
    }

    public void insertToSyncedTable(ArrayList<ClassNote> syncedNotes) {
        openDatabase();

        for(ClassNote note : syncedNotes) {
            String SQL = "INSERT INTO " + SyncedTable + " ( " +
                    this.number + " , " +
                    this.note + " , " +
                    this.callDate + " , " +
                    this.name + " , " +
                    this.FriendEmail + " , " +
                    this.category +
                    " ) VALUES ( '" +
                    note.getPhoneNumber() + "' , '" +
                    note.getNotes(true)   + "' , '" +
                    note.getCallDate()    + "' , '" +
                    note.getName()        + "' , '" +
                    note.getFriendEmail() + "' , '" +
                    note.getCategory()    +
                    "' ) ";

            NoteDatabase.execSQL(SQL);
        }
        //closeDatabase();
    }
    public ArrayList<ClassNote> getSyncedData() {
        openDatabase();

        dataList = new ArrayList<>();

        String SQL = "SELECT * FROM " + SyncedTable;

        Cursor cursor = NoteDatabase.rawQuery(SQL, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String number = cursor.getString(1);
                String note = cursor.getString(2);
                String callDate = cursor.getString(3);
                String name = cursor.getString(4);
                String email = cursor.getString(5);
                String category = cursor.getString(6);
                ClassNote classNote = new ClassNote(id);
                if(!category.equals("Personal ")) {
                    classNote.setPhoneNumber(number);
                    classNote.setNotes(note);
                    classNote.setCallDate(callDate);
                    classNote.setName(name);
                    classNote.setSynced(1);
                    classNote.setFriendEmail(email);
                    classNote.setCategory(category);
                    classNote.setReminder("");

                    dataList.add(classNote);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        //closeDatabase();

        return dataList;
    }
}
