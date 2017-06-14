package com.example.juseris.aftercallnote.Activities;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.Fragments.Contacts;
import com.example.juseris.aftercallnote.Fragments.IncomingCalls;
import com.example.juseris.aftercallnote.Fragments.OutgoingCalls;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AllCallsActivity extends AppCompatActivity {
    private ArrayList<ContactEntity> contacts;
    private long contactID = 0;
   // private ListView incomingLog;
    public Cursor phones;
    public ContentResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_incoming_calls);
        setTitle("All Calls");
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(AllCallsActivity.this)
                .add(R.string.titleB, IncomingCalls.class)
                .add(R.string.titleA, OutgoingCalls.class)
                .add(R.string.titleC, Contacts.class)
                .create());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAllIncoming);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();

        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);
        setSupportActionBar(toolbar);

        //add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        boolean hasSyncedCall = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("IncomingSynced", false);
        if(!hasSyncedCall) {
            LoadContact async = new LoadContact();
            async.execute();
        }
    }
   /* @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit().putBoolean("haveToChooseContact", false).apply();
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ArrayList<ContactEntity> incoming;
    ArrayList<ContactEntity> outgoing;

    // Load data on background
    public class LoadContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                phones = getContentResolver().query(CallLog.Calls.CONTENT_URI,
                        null, null, null, CallLog.Calls.DATE + " DESC");
            }
            incoming = new ArrayList<>();
            outgoing = new ArrayList<>();
            if (phones != null) {
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
                            incoming.add(new ContactEntity(_name, phNumber, getCallTime(callDate), getCallDuration(callDuration)));
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            String phNumber1 = phones.getString(number);
                            String _name1;
                            if (phones.getString(name) == null) {
                                _name1 = "";
                            } else {
                                _name1 = phones.getString(name);
                            }
                            String callDate1 = phones.getString(date);
                            String callDuration1 = phones.getString(duration);
                            outgoing.add(new ContactEntity(_name1, phNumber1, getCallTime(callDate1), getCallDuration(callDuration1)));
                            break;
                    }
                }
                phones.close();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            FirebaseConnection con = new FirebaseConnection(getApplicationContext());
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String email = "";
            if (user != null) {
                email = user.getEmail();
                String fixedEmail = email.replace(".", ",");
                con.addIncomingCalls(fixedEmail, incoming);
                con.addOutgoingCalls(fixedEmail, outgoing);
            }

        }
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

}
