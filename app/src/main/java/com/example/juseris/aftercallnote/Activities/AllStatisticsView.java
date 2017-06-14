package com.example.juseris.aftercallnote.Activities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Models.CallStatisticsEntity;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by juseris on 12/25/2016.
 */

public class AllStatisticsView extends AppCompatActivity  {
    private PieChart mChart;
    private Database db;
    private CallStatisticsEntity cse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_statistics);
        db = new Database(getApplicationContext());
        if (getIntent().getStringExtra("PhoneNumber") == null) {
            cse = db.getStatistics();
            setTitle("Statistics");
        } else {
            cse = db.getStatistics(getIntent().getStringExtra("PhoneNumber"));
            setTitle("Statistics of " + getContactName(getApplicationContext(), cse.getNumber()));
        }

        int[] colorCodes = {
                Color.rgb(204, 0, 204), Color.rgb(51, 0, 102)};
        mChart = (PieChart) findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 50, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);
        mChart.setUsePercentValues(true);
        mChart.setCenterTextColor(Color.WHITE);
        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);
        float calls = cse.getIncomingCallCount() + cse.getOutgoingCallCount();
        float notes = cse.getTypedNoteCount();
        if(calls != 0) {
            float percentage = (notes / calls) * 100;
            //String.format("%.2f", floatValue);
            mChart.setCenterText(String.format(java.util.Locale.US,"%.0f", percentage) + " %");
            mChart.setCenterTextSize(22f);
            mChart.setCenterTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
        mChart.invalidate();
        mChart.setHoleRadius(40f);
        mChart.setTransparentCircleRadius(35f);
        mChart.setDrawCenterText(true);
        //mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
       // mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);
        //mChart.getLegend().setWordWrapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
       // mChart.setOnChartValueSelectedListener(this);
        setData(colorCodes);
        mChart.animateY(1400, Easing.EasingOption.EaseInSine);
        // mChart.spin(2000, 0, 360);

        LinearLayout layout = (LinearLayout) findViewById(R.id.table);

        String[] titles = new String[]{"Total incoming calls "
                , "Total outgoing calls", "Notes typed", "Reminders added", "Incoming calls time", "Outgoing calls time"};
        Integer[] values = new Integer[]{cse.getIncomingCallCount(), cse.getOutgoingCallCount(), cse.getTypedNoteCount(),
                cse.getRemindersAddedCount(), cse.getIncomingTimeTotal(), cse.getOutgoingTimeTotal()};

        for (int i = 0; i < values.length; i++) {
            LinearLayout.LayoutParams parms_left_layout = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            parms_left_layout.weight = 1F;
            LinearLayout left_layout = new LinearLayout(getApplicationContext());
            left_layout.setOrientation(LinearLayout.HORIZONTAL);
            left_layout.setGravity(Gravity.START | Gravity.CENTER);

            LinearLayout.LayoutParams parms_legen_layout = new LinearLayout.LayoutParams(
                    20, 20);

            parms_legen_layout.setMargins(0, 0, 20, 0);
            LinearLayout legend_layout = new LinearLayout(getApplicationContext());
            legend_layout.setLayoutParams(parms_legen_layout);
            legend_layout.setOrientation(LinearLayout.HORIZONTAL);
            if (i < 2) {
                legend_layout.setBackgroundColor(colorCodes[i]);
            }
            left_layout.addView(legend_layout);

            TextView txt_unit = new TextView(getApplicationContext());
            txt_unit.setSingleLine();
            txt_unit.setTextColor(Color.BLACK);
            txt_unit.setText(titles[i]);
            left_layout.addView(txt_unit);
            left_layout.setLayoutParams(parms_left_layout);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams parms_middle_layout = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, height);
            parms_middle_layout.weight = 1F;
            LinearLayout middle_layout = new LinearLayout(this);
            middle_layout.setOrientation(LinearLayout.HORIZONTAL);
            middle_layout.setGravity(Gravity.CENTER | Gravity.START);
            middle_layout.setLayoutParams(parms_middle_layout);
            int leftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

            TextView txt_leads = new TextView(this);
            txt_leads.setPadding(leftPadding, 0, 0, 0);
            txt_leads.setTextColor(Color.BLACK);
            if (i == 2) {
                float percent = (notes / calls) * 100;
                //String.format("%.2f", floatValue);
                mChart.setCenterText(String.format(java.util.Locale.US,"%.0f", percent) + " %");
                String str = String.format(java.util.Locale.US,"%d (%.0f%%)",values[i],percent);
                txt_leads.setText(str);
            } else {
                txt_leads.setText(String.valueOf(values[i]));
            }

            if (i > 3) {
                int seconds = 0;
                int minutes = 0;
                int hours = 0;
                try {
                    seconds = values[i];
                    while (seconds - 60 >= 0) {
                        minutes++;
                        seconds -= 60;
                        if (minutes == 60) {
                            hours++;
                            minutes = 0;
                        }
                    }
                } catch (NumberFormatException e) {
                    //Will Throw exception!
                    //do something! anything to handle the exception.
                }
                if (minutes == 0 && hours == 0) {
                    txt_leads.setText(String.valueOf(seconds) + " s");
                } else if (hours == 0 && minutes != 0) {
                    txt_leads.setText(String.valueOf(minutes) + " min " + String.valueOf(seconds) + " s");
                } else {
                    txt_leads.setText(String.valueOf(hours) + " h " + String.valueOf(minutes) + " min " + String.valueOf(seconds) + " s");
                }
            }
            middle_layout.addView(txt_leads);

            LinearLayout a = new LinearLayout(this);
            a.setOrientation(LinearLayout.HORIZONTAL);
            View v = new View(this);
            v.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            ));
            v.setBackgroundColor(Color.parseColor("#B3B3B3"));
            a.addView(left_layout);
            a.addView(middle_layout);

            layout.addView(a);
            layout.addView(v);
        }
        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(true);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setEnabled(false);
        l.setExtra(colorCodes, new String[]{"Total incoming calls"
                , "Total outgoing calls"});

        //entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTextSize(12f);
        Toolbar toolbar = (Toolbar) findViewById(R.id.statsToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        // toolbar height
        layoutParams.height = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());

        toolbar.setLayoutParams(layoutParams);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setData(int[] colorCodes ) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

        float total = cse.getIncomingCallCount() + cse.getOutgoingCallCount()
                + cse.getRemindersAddedCount() + cse.getTypedNoteCount();
        if (total == 0) {
            AllStatisticsView.this.finish();
            Toast.makeText(getApplicationContext(), "no data yet", Toast.LENGTH_SHORT).show();
        }
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);

        float inc = cse.getIncomingCallCount()/total*100;
        float out = cse.getOutgoingCallCount()/total*100;
       // int typed = cse.getTypedNoteCount();///total*100;
       // float rem = cse.getRemindersAddedCount()/total*100;
        entries.add(new PieEntry(inc, inc));
        entries.add(new PieEntry(out, out));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);


        //colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colorCodes);
        dataSet.setSelectionShift(0f);
        PieData data = new PieData(dataSet);

        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = phoneNumber;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

}
