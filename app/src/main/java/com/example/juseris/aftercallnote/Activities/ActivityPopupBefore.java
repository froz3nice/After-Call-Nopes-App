package com.example.juseris.aftercallnote.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Adapters.ChildItemAdapter;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ClassSettings;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ActivityPopupBefore extends AppCompatActivity {

    private Context context;

    private ListView listViewNotes = null;
    public static boolean active = false;
    private ArrayList<ClassNote> noteList = null;
    private Database db = null;
    private ClassSettings Settings = null;
    private String number = null;
    private BroadcastReceiver receiver;
    private Integer itemIndex;
    private ChildItemAdapter listAdapter = null;
    private PopupMenu popup;
    TextView title;

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
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_popup_before);
        context = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.beforeCallToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        //toolbar height
        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        toolbar.setLayoutParams(layoutParams);
        title = (TextView)toolbar.findViewById(R.id.toolbar_title);
        Settings = new ClassSettings(context);
        noteList = new ArrayList<>();
        db = new Database(context);
        Button buttonClose = (Button) findViewById(R.id.bc_button_close);
        listViewNotes = (ListView) findViewById(R.id.bc_listview_notes);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityPopupBefore.this.finish();
                // wm.removeViewImmediate(mTopView);
            }
        });
        Initializing();
        //wm.addView(mTopView, params);

        IntentFilter filter = new IntentFilter();

        filter.addAction("com.hello.action");
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                ActivityPopupBefore.this.finish();
                // wm.removeViewImmediate(mTopView);
            }
        };
        registerReceiver(receiver, filter);
        if (noteList.size() < 1) {
            ActivityPopupBefore.this.finish();
        }

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
        number = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("LastActiveNr", "");
        if (getIntent().getStringExtra("NUMBER") != null) {
            if(!getIntent().getStringExtra("NUMBER").equals(number)){
                number = getIntent().getStringExtra("NUMBER");
            }
        }

        noteList = db.getDataByNumber(number);
        noteList.addAll(db.getSyncedNotesByNumber(number));
        setTitle("");

        if (!noteList.isEmpty()) {
            String name = noteList.get(0).getName();
            title.setText("History for "+number + " " + name);
        } else {
            title.setText("History for "+number);
        }
        sort();
        listAdapter = new ChildItemAdapter(this, noteList);
        listAdapter.notifyDataSetChanged();
        listViewNotes.setAdapter(listAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x4233) {
            updateListView();
        }
    }

    public void updateListView() {
        noteList.clear();
        noteList = db.getDataByNumber(number);
        noteList.addAll(db.getSyncedNotesByNumber(number));
        sort();
        listAdapter = new ChildItemAdapter(this, noteList);
        listAdapter.notifyDataSetChanged();
        listViewNotes.setAdapter(listAdapter);
    }

    public void sort(){
        Collections.sort(noteList,new Comparator<ClassNote>(){

            @Override
            public int compare(ClassNote a, ClassNote b) {
                DateFormat formatter = new SimpleDateFormat("MMMM dd HH:mm", Locale.US);
                try {
                    Date date1 = formatter.parse(a.getCallDate());
                    Date date2 = formatter.parse(b.getCallDate());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 1;
                }
            }
        });
    }

}
