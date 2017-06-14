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

public class RemindersList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_list);
        setTitle("All reminders");
        ArrayList<ClassNote> noteList = new ArrayList<>();
        Database db = new Database(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.remindersToolbar);
        RecyclerView reminderList = (RecyclerView) findViewById(R.id.reminderList);
        setSupportActionBar(toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();

        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());

        toolbar.setLayoutParams(layoutParams);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        noteList = db.getData();
        for (Iterator<ClassNote> iterator = noteList.iterator(); iterator.hasNext(); ) {
            ClassNote value = iterator.next();
            if (value.getReminder().equals("")) {
                iterator.remove();
            }
        }
        if (!noteList.isEmpty()) {
            findViewById(R.id.noEvents).setVisibility(View.GONE);
        }
        Collections.reverse(noteList);
        ArrayList<ClassNote> listWithSectionHeaders = new ArrayList<>();
        Boolean upcomingHeader = false;
        Boolean historyHeader = false;

        Collections.sort(noteList,new Comparator<ClassNote>(){
            @Override
            public int compare(ClassNote a, ClassNote b) {
                DateFormat formatter = new SimpleDateFormat("yyyy MMM dd HH:mm",Locale.ENGLISH);
                try {
                    Date date1 = formatter.parse(a.getReminder());
                    Date date2 = formatter.parse(b.getReminder());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 1;
                }
            }
        });

        for(ClassNote note :noteList){
            String str_date = note.getReminder();
            Date date ;
            DateFormat formatter = new SimpleDateFormat("yyyy MMM dd HH:mm",Locale.ENGLISH);
            try {

                date = formatter.parse(str_date);
                if(!upcomingHeader) {
                    Date curDate = Calendar.getInstance().getTime();
                    if (date.compareTo(curDate) > 0) {
                        upcomingHeader = true;
                        ClassNote temp = new ClassNote();
                        temp.setName("Upcoming");
                        temp.isSection = true;
                        listWithSectionHeaders.add(temp);
                    }
                }
                if(!historyHeader){
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

        AllRemindersAdapter listAdapter = new AllRemindersAdapter(RemindersList.this, listWithSectionHeaders);
        listAdapter.notifyDataSetChanged();
        reminderList.setAdapter(listAdapter);
        reminderList.setLayoutManager(new LinearLayoutManager(RemindersList.this));
        reminderList.getRecycledViewPool().setMaxRecycledViews(0, 0);

      /*  reminderList.setItemsCanFocus(true);
        reminderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //CustomAdapter.ViewHolder holder = (CustomAdapter.ViewHolder) view.getTag();
                Intent intent = new Intent(getApplicationContext(), MainListChildItem.class);
                intent.putExtra("classNoteobj", listAdapter.getItem(position));
                startActivity(intent);
            }
        });*/
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
