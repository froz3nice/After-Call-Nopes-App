package com.example.juseris.aftercallnote.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Adapters.ChildItemAdapter;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.Models.Order;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityPopupBefore extends AppCompatActivity {

    private Context context;

    private RecyclerView listViewNotes = null;
    public static boolean active = false;
    private ArrayList<IGenericItem> noteList = null;
    private Database db = null;
    private String number = null;
    private BroadcastReceiver receiver;
    private Integer itemIndex;
    private ChildItemAdapter listAdapter = null;
    private PopupMenu popup;
    private TextView name;
    private TextView nr;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences prefs;

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_popup_before);
        context = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.beforeCallToolbar);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isIncomingCall = prefs.getBoolean("isIncoming", true);
        if (!isIncomingCall) {
            title.setText("Outgoing Call");
        } else {
            title.setText("Incoming Call");
        }
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        //toolbar height
        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);
        nr = (TextView) findViewById(R.id.number);
        name = (TextView) findViewById(R.id.name);
        noteList = new ArrayList<>();
        db = new Database(context);
        Button buttonClose = (Button) findViewById(R.id.bc_button_close);
        listViewNotes = (RecyclerView) findViewById(R.id.ac_main_listView);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityPopupBefore.this.finish();
            }
        });
        Initializing();

        IntentFilter filter = new IntentFilter();

        filter.addAction("com.braz.close");
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                ActivityPopupBefore.this.finish();
            }
        };
        registerReceiver(receiver, filter);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        active = false;
        super.onDestroy();
    }

    private void Initializing() {
        number = prefs.getString("LastActiveNr", "");
        if (getIntent().getStringExtra("NUMBER") != null) {
            if (!getIntent().getStringExtra("NUMBER").equals(number)) {
                number = getIntent().getStringExtra("NUMBER");
            }
        }
        setTitle("");
        refreshList();
    }

    public void refreshList() {
        noteList = db.getDataByNumber(number);
        noteList.addAll(db.getSyncedNotesByNumber(number));
        if (!noteList.isEmpty()) {
            String name1 = ((ClassNote) noteList.get(0)).getName();
            name.setText(name1);
        }
        nr.setText(number);
        noteList = Utils.getSortedList(noteList);
        ArrayList<IGenericItem> newItems = db.getNewPrestaByNr(number, number);
        newItems = Utils.getSortedPrestaList(newItems);
        noteList.addAll(newItems);
        if (!newItems.isEmpty()) {
            name.setText(String.format("%s %s", ((Order) newItems.get(0)).getName(), ((Order) newItems.get(0)).getSurname()));
        }
        ArrayList<IGenericItem> presta = db.getPrestashopByNr(number, number);
        if (!presta.isEmpty()) {
            name.setText(String.format("%s %s", ((Order) presta.get(0)).getName(), ((Order) presta.get(0)).getSurname()));
        }
        noteList.addAll(presta);
        listAdapter = new ChildItemAdapter(this, noteList);

        listAdapter.notifyDataSetChanged();
        listViewNotes.setAdapter(listAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        listViewNotes.setLayoutManager(mLayoutManager);
        listViewNotes.getRecycledViewPool().setMaxRecycledViews(0, 0);
    }
}
