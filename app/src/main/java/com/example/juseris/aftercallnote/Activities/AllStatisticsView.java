package com.example.juseris.aftercallnote.Activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by juseris on 12/25/2016.
 */

public class AllStatisticsView extends AppCompatActivity {
    private PieChart mChart;
    private LineChart lineChart;
    private Database db;
    private CallStatisticsEntity cse;
    private SharedPreferences prefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_statistics);
        db = new Database(getApplicationContext());
        if (getIntent().getStringExtra("PhoneNumber") == null) {
            cse = db.getStatistics();
            setTitle("Statistics");
        } else {
            cse = db.getStatistics(getIntent().getStringExtra("PhoneNumber"));
            setTitle("Statistics of " + getContactName(getApplicationContext(), cse.getNumber()));
        }
        setUpPieChart();
        float calls = cse.getIncomingCallCount() + cse.getOutgoingCallCount();
        float notes = cse.getTypedNoteCount();
        if (calls != 0) {
            float percentage = (notes / calls) * 100;
            //String.format("%.2f", floatValue);
            mChart.setCenterText(String.format(java.util.Locale.US, "%.0f", percentage) + " %");
            mChart.setCenterTextSize(22f);
            mChart.setCenterTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
        int[] colorCodes = {
                Color.rgb(204, 0, 204), Color.rgb(51, 0, 102)};
        setData(colorCodes);
        lineChart = (LineChart) findViewById(R.id.lineChart);
        if (getIntent().getBooleanExtra("showContactStats", true)) {
            setUpLineChart();
            lineChart.getAxisRight().setEnabled(false);
            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setGranularity(1f);
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawAxisLine(false);
            setLineChartData();
        }else{
            lineChart.setVisibility(View.GONE);
        }
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
                mChart.setCenterText(String.format(java.util.Locale.US, "%.0f", percent) + " %");
                String str = String.format(java.util.Locale.US, "%d (%.0f%%)", values[i], percent);
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

    private void setUpLineChart() {
        lineChart.setDrawGridBackground(false);

        // no description text
        lineChart.getDescription().setEnabled(false);

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);

    }


    private void setUpPieChart() {
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
        mChart.invalidate();
        mChart.setHoleRadius(40f);
        mChart.setTransparentCircleRadius(35f);
        mChart.setDrawCenterText(true);
        //mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        // mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);
        mChart.animateY(1400, Easing.EasingOption.EaseInSine);
        //mChart.getLegend().setWordWrapEnabled(true);
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);
        // add a selection listener
        // mChart.setOnChartValueSelectedListener(this);
    }

    private void setLineChartData() {


        ArrayList<Entry> incomingValues = new ArrayList<>();
        ArrayList<Entry> outgoingValues = new ArrayList<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       /* prefs.edit().putInt("outgoing."+1,0).apply();
        prefs.edit().putInt("outgoing."+2,0).apply();
        prefs.edit().putInt("outgoing."+3,0).apply();
        prefs.edit().putInt("outgoing."+4,0).apply();
        prefs.edit().putInt("outgoing."+5,0).apply();
        prefs.edit().putInt("outgoing."+6,0).apply();
        prefs.edit().putInt("outgoing."+7,0).apply();*/

        final HashMap<Integer, String> numMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 1; i < 8; i++) {
            SimpleDateFormat df = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
            numMap.put(i, String.format("%s", df.format(calendar.getTime())));
            calendar.add(Calendar.DAY_OF_YEAR, +1);
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(7);
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return numMap.get((int) value);
            }
        });
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        for (int i = 0; i < 7; i++) {
            day--;
            if (day < 1) {
                day = 7;
            }
        }

        for (int i = 1; i < 8; i++) {
            incomingValues.add(new Entry(i, prefs.getInt("incoming." + day, 0)));
            outgoingValues.add(new Entry(i, prefs.getInt("outgoing." + day, 0)));
            day++;
            if (day > 7) {
                day = 1;
            }
        }


        LineDataSet incoming;
        LineDataSet outgoing;

        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            incoming = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            incoming.setValues(incomingValues);
            outgoing = (LineDataSet) lineChart.getData().getDataSetByIndex(1);
            outgoing.setValues(outgoingValues);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            outgoing = new LineDataSet(outgoingValues, "Outgoing calls");
            // set the line to be drawn like this "- - - - - -"
            outgoing.setColor(Color.rgb(51, 0, 102));
            outgoing.setCircleColor(Color.rgb(51, 0, 102));
            outgoing.setLineWidth(1f);
            outgoing.setCircleRadius(3f);
            outgoing.setDrawCircleHole(false);
            outgoing.setValueTextSize(9f);
            outgoing.setDrawFilled(true);
            outgoing.setFormLineWidth(1f);
            //outgoing.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            //outgoing.setFormSize(15.f);

            // create a dataset and give it a type
            incoming = new LineDataSet(incomingValues, "Incoming calls");
            // set the line to be drawn like this "- - - - - -"
            incoming.setColor(Color.rgb(204, 0, 204));
            incoming.setCircleColor(Color.rgb(204, 0, 204));
            incoming.setLineWidth(1f);
            incoming.setCircleRadius(3f);
            incoming.setDrawCircleHole(false);
            incoming.setValueTextSize(9f);
            incoming.setDrawFilled(true);
            incoming.setFormLineWidth(1f);
            //incoming.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            //incoming.setFormSize(15.f);

            if (Build.VERSION.SDK_INT >= 18) {
                // fill drawable only supported on api level 18 and above
            } else {
                incoming.setFillColor(Color.BLACK);
                outgoing.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(incoming); // add the datasets
            dataSets.add(outgoing);
            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
            lineChart.notifyDataSetChanged();
        }
    }


    private void setData(int[] colorCodes) {

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

        float inc = cse.getIncomingCallCount() / total * 100;
        float out = cse.getOutgoingCallCount() / total * 100;
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
