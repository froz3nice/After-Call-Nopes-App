package com.example.juseris.aftercallnote.Fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.MainListChildItem;
import com.example.juseris.aftercallnote.Adapters.InCallsAdapter;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by juseris on 3/24/2017.
 */

public class IncomingCalls extends Fragment {
    public static final String ARG_OBJECT = "object";
    public Cursor phones;
    public ContentResolver resolver;
    private ArrayList<ContactEntity> contacts;
    private ListView incomingLog = null;
    private RelativeLayout layout;
    Database db;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.outgoing_calls_fragment, container, false);
        layout = (RelativeLayout) rootView.findViewById(R.id.loadingPanel);
        incomingLog = (ListView) rootView.findViewById(R.id.incomingCalls);
        db = new Database(getActivity());
        new LoadContact().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
       /* Bundle args = getArguments();
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                Integer.toString(args.getInt(ARG_OBJECT)));*/
        return rootView;
    }


    // Load data on background
    class LoadContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone
            if (!db.getIncomingCalls().isEmpty()) {
                contacts = db.getIncomingCalls();
            } else {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                    phones = getActivity().getContentResolver().query(CallLog.Calls.CONTENT_URI,
                            null, null, null, CallLog.Calls.DATE + " DESC");
                    resolver = getActivity().getContentResolver();
                }
                contacts = new ArrayList<>();

                if (phones != null) {
                    Log.e("count", "" + phones.getCount());
                    if (phones.getCount() == 0) {
                        Toast.makeText(getActivity(), "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                    }
                    int number = phones.getColumnIndex(CallLog.Calls.NUMBER);
                    int name = phones.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    int type = phones.getColumnIndex(CallLog.Calls.TYPE);
                    int date = phones.getColumnIndex(CallLog.Calls.DATE);
                    int duration = phones.getColumnIndex(CallLog.Calls.DURATION);
                    while (phones.moveToNext()) {
                        String callType = phones.getString(type);
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
                                contacts.add(new ContactEntity(_name, phNumber, getCallTime(callDate), getCallDuration(callDuration)));
                                break;
                        }
                    }
                    phones.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            layout.setVisibility(View.GONE);
            if (getActivity() != null) {

                final InCallsAdapter listAdapter = new InCallsAdapter(getActivity(), contacts);
                listAdapter.notifyDataSetChanged();
                incomingLog.setAdapter(listAdapter);
                incomingLog.setItemsCanFocus(true);
                boolean chooseContact = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("haveToChooseContact", false);
                if (chooseContact) {
                    incomingLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) view.getTag();
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().putBoolean("haveToChooseContact", false).apply();
                            //Intent intent = new Intent(getActivity(), ActivityPopupAfter.class);
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().putString("ChosenNumber", listAdapter.getItem(position).getNumber()).apply();
                            //startActivity(intent);
                            getActivity().finish();
                        }
                    });
                } else {
                    incomingLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) view.getTag();
                            Intent intent = new Intent(getActivity(), MainListChildItem.class);
                            ClassNote note = new ClassNote();
                            note.setPhoneNumber(Utils.fixNumber(listAdapter.getItem(position).getNumber()));
                            note.setName(listAdapter.getItem(position).getName());
                            intent.putExtra("classNoteobj", note);
                            startActivity(intent);
                        }
                    });
                }
                incomingLog.setFastScrollEnabled(true);
            }
        }
    }


    private String getCallTime(String callTime) {
        String tm = "hey";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm");
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
        String time = "";
        if (minutes != 0) {
            time = String.valueOf(minutes) + " min " + String.valueOf(seconds) + " s";
        } else {
            time = String.valueOf(seconds) + " s";
        }

        return time;
    }
}
