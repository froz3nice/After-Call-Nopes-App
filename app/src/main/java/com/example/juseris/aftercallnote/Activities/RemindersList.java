package com.example.juseris.aftercallnote.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Adapters.AllRemindersAdapter;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RemindersList extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_list);
        setTitle("All reminders");
        Database db = new Database(getApplicationContext());
        RecyclerView reminderList = (RecyclerView) findViewById(R.id.ac_main_listView);

        setUpToolbar();
        ArrayList<IGenericItem> noteList = db.getData();
        ArrayList<IGenericItem> noteSynced = db.getSyncedData();
        noteList.addAll(noteSynced);
        noteList = getOnlyReminderNotes(noteList);
        if (!noteList.isEmpty()) {
            findViewById(R.id.noEvents).setVisibility(View.GONE);
        }
        // Collections.reverse(noteList);
        ArrayList<IGenericItem> listWithSectionHeaders = new ArrayList<>();
        sortNotesByDate(noteList);
        //Date date2 = parseOrReturnNull(((ClassNote) noteList.get(0)).getReminder());

        listWithSectionHeaders = getListWithSectionHeaders(noteList);
        setUpRecyclerView(listWithSectionHeaders,reminderList);
      /*  reminderList.setItemsCanFocus(true);
        reminderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) view.getTag();
                Intent intent = new Intent(getApplicationContext(), MainListChildItem.class);
                intent.putExtra("classNoteobj", listAdapter.getItem(position));
                startActivity(intent);
            }
        });*/
    }

    private void setUpRecyclerView(ArrayList<IGenericItem> reminders, RecyclerView reminderList){
        AllRemindersAdapter listAdapter = new AllRemindersAdapter(RemindersList.this, reminders);
        listAdapter.notifyDataSetChanged();
        reminderList.setAdapter(listAdapter);
        reminderList.setLayoutManager(new LinearLayoutManager(RemindersList.this));
        reminderList.getRecycledViewPool().setMaxRecycledViews(0, 0);
    }

    private ArrayList<IGenericItem> getListWithSectionHeaders(ArrayList<IGenericItem> noteList){
        Boolean upcomingHeader = false;
        Boolean historyHeader = false;
        ArrayList<IGenericItem> listWithSectionHeaders = new ArrayList<>();
        for (IGenericItem note : noteList) {
            if (note instanceof ClassNote) {
                String str_date = ((ClassNote) note).getReminder();
                Date date;
                DateFormat formatter = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.ENGLISH);
                try {

                    date = formatter.parse(str_date);
                    if (!upcomingHeader) {
                        Date curDate = Calendar.getInstance().getTime();
                        if (date.compareTo(curDate) > 0) {
                            upcomingHeader = true;
                            ClassNote temp = new ClassNote();
                            temp.setName("Upcoming");
                            temp.isSection = true;
                            listWithSectionHeaders.add(temp);
                        }
                    }
                    if (!historyHeader) {
                        if (date.compareTo(Calendar.getInstance().getTime()) <= 0) {
                            historyHeader = true;
                            ClassNote temp = new ClassNote();
                            temp.setName("History");
                            temp.isSection = true;
                            listWithSectionHeaders.add(temp);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                listWithSectionHeaders.add(note);
            }
        }
        return listWithSectionHeaders;
    }

    private ArrayList<IGenericItem> getOnlyReminderNotes(ArrayList<IGenericItem> noteList){
        for (Iterator<IGenericItem> iterator = noteList.iterator(); iterator.hasNext(); ) {
            IGenericItem value = iterator.next();
            if (value instanceof ClassNote) {
                if (((ClassNote) value).getReminder().equals("")) {
                    iterator.remove();
                }
            }
        }
        return noteList;
    }
    private void setUpToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.remindersToolbar);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();

        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());

        toolbar.setLayoutParams(layoutParams);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public Date parseOrReturnNull(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.US);
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private void sortNotesByDate(ArrayList<IGenericItem> noteList) {
        Collections.sort(noteList, new Comparator<IGenericItem>() {
            @Override
            public int compare(IGenericItem b, IGenericItem a) {
                Date date2 = parseOrReturnNull(((ClassNote) b).getReminder());
                Date date1 = parseOrReturnNull(((ClassNote) a).getReminder());
                if (date1 == null) {
                    if (date2 == null) {
                        return 0;
                    }
                    return 1;
                }
                if (date2 == null) {
                    return -1;
                }
                return date2.compareTo(date1);
            }
        });
        Collections.reverse(noteList);
    }

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
}
