package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

/**
 * Created by Juozas on 2017.10.16.
 */


public class WeekListAdapter extends ArrayAdapter<String> {

    private ArrayList<String> weekList;
    private Context context;

    public WeekListAdapter(Context context, int textViewResourceId,
                           ArrayList<String> countryList) {
        super(context, textViewResourceId, countryList);
        this.context = context;
        this.weekList = new ArrayList<>();
        this.weekList.addAll(countryList);
    }

    private class ViewHolder {
        CheckBox name;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            if (position < 7) {
                convertView = vi.inflate(R.layout.week_days, parent, false);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);
                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        if (cb.isChecked()) {
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .edit()
                                    .putBoolean(cb.getText().toString(), true)
                                    .apply();
                        } else {
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .edit()
                                    .putBoolean(cb.getText().toString(), false)
                                    .apply();
                        }
                    }
                });
                String day = weekList.get(position);
                holder.name.setText(day);
                holder.name.setChecked(PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(day, true));
            } else {
                convertView = vi.inflate(R.layout.settings_string, parent, false);
            }
        }
        return convertView;
    }
}
