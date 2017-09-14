package com.example.juseris.aftercallnote;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.MainActivity;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.Models.IGenericItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by juseris on 3/17/2017.
 */

public class DataExportation {
    Context context;
    Database db;
    StringBuilder sbIncoming;
    StringBuilder sbNotes;

    public DataExportation(Context ctx) {
        context = ctx;
        db = new Database(context);

        sbIncoming = new StringBuilder();

        sbIncoming.append("Phone number");
        sbIncoming.append(',');

        sbIncoming.append("Name");
        sbIncoming.append(',');

        sbIncoming.append("Date");
        sbIncoming.append(',');
        sbIncoming.append("Duration");
        sbIncoming.append(',');
        sbIncoming.append('\n');

        sbNotes = new StringBuilder();

        sbNotes.append("Phone number");
        sbNotes.append(',');

        sbNotes.append("Name");
        sbNotes.append(',');

        sbNotes.append("Date");
        sbNotes.append(',');
        sbNotes.append("Note");
        sbNotes.append(',');
        sbNotes.append("Category");
        sbNotes.append(',');
        sbNotes.append('\n');
    }

    public void exportNote(ClassNote note) {
        CachedFileProvider provider = new CachedFileProvider();
        context.startActivity(Intent.createChooser(provider.getSendEmailIntent(note.ExportString(), "call_data.csv"), "Send mail..."));
    }

    public void exportBoth() {
        Log.i("Send email", "");
        try {
            File folder = new File(Environment.getExternalStorageDirectory()
                    + "/afterCallFolder");
            boolean var = false;
            if (!folder.exists())
                var = folder.mkdir();

            for (Integer i = 0; i < db.getData().size(); i++) {
                sbNotes.append(((ClassNote) db.getData().get(i)).toExcel()).append("\n");
            }
            for (ContactEntity call : getIncomingCalls()) {
                sbIncoming.append(call.toExcel()).append("\n");
            }

            try {
                CachedFileProvider provider = new CachedFileProvider();
                CachedFileProvider.createCachedFile(context, "call_data.csv", "incoming_calls.csv"
                        , sbNotes.toString(), sbIncoming.toString());
                context.startActivity(Intent.createChooser
                        (provider.getMultipleAttachmentIntent("call_data.csv", "incoming_calls.csv"), "Send mail..."));
                //finish();
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportData() {
        Log.i("Send email", "");
        try {
            File folder = new File(Environment.getExternalStorageDirectory()
                    + "/afterCallFolder");
            boolean var = false;
            if (!folder.exists())
                var = folder.mkdir();

            for (Integer i = 0; i < db.getData().size(); i++) {
                sbNotes.append(((ClassNote) db.getData().get(i)).toExcel()).append("\n");
            }

            try {
                CachedFileProvider provider = new CachedFileProvider();
                CachedFileProvider.createCachedFile(context, "call_data.csv", "", sbNotes.toString(), "");
                context.startActivity(Intent.createChooser(provider.getSendEmailIntent("", "call_data.csv"), "Send mail..."));
                //finish();
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<ContactEntity> getIncomingCalls() {
        ArrayList<ContactEntity> calls = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor phones = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    null, null, null, CallLog.Calls.DATE + " DESC");
            if (phones != null) {
                Log.e("count", "" + phones.getCount());
                if (phones.getCount() == 0) {
                    Toast.makeText(context, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                }
                int number = phones.getColumnIndex(CallLog.Calls.NUMBER);
                int name = phones.getColumnIndex(CallLog.Calls.CACHED_NAME);
                int type = phones.getColumnIndex(CallLog.Calls.TYPE);
                int date = phones.getColumnIndex(CallLog.Calls.DATE);
                int duration = phones.getColumnIndex(CallLog.Calls.DURATION);
                while (phones.moveToNext()) {
                    String callType = phones.getString(type);
                    String dir = null;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.INCOMING_TYPE:
                            String phNumber = phones.getString(number);
                            String _name;
                            if (phones.getString(name) == null) {
                                _name = "";
                            } else {
                                _name = phones.getString(name);
                            }
                            String callDate = phones.getString(date);
                            String callDuration = phones.getString(duration);
                            calls.add(new ContactEntity(_name, phNumber, getCallTime(callDate), getCallDuration(callDuration)));
                            break;
                    }
                }
                phones.close();
            } else {
                Log.e("Cursor close 1", "----------------");
            }
        }
        return calls;
    }

    public void exportIncomingCalls() {

        try {
            File folder = new File(Environment.getExternalStorageDirectory()
                    + "/afterCallFolder");
            boolean var = false;
            if (!folder.exists())
                var = folder.mkdir();

            String allLines = "";
            if (!db.getIncomingCalls().isEmpty()) {
                for (ContactEntity call : db.getIncomingCalls()) {
                    allLines += call.exportString() + "\n";
                    sbIncoming.append(call.toExcel()).append("\n");
                }
            } else {
                for (ContactEntity call : getIncomingCalls()) {
                    allLines += call.exportString() + "\n";
                    sbIncoming.append(call.toExcel()).append("\n");
                }
            }
            try {
                CachedFileProvider provider = new CachedFileProvider();
                CachedFileProvider.createCachedFile(context, "incoming_calls.csv", "", sbIncoming.toString(), "");
                context.startActivity(Intent.createChooser(provider.getSendEmailIntent("", "incoming_calls.csv"), "Send mail..."));
                //finish();
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
