package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by juseris on 3/17/2017.
 */

public class CustomListAdapterDialog extends BaseAdapter {
    private Context context;

    class ViewHolder {
        TextView title;
    }

    private LayoutInflater inflater;
    private ArrayList<String> objects;

    public CustomListAdapterDialog(Context context, ArrayList<String> objects) {
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return objects.size();
    }

    public String getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        convertView = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.dialog_textview, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.textView12);
            final SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append(objects.get(position));
            if (position == 0) {
                holder.title.setTextColor(Color.parseColor("#808080"));
            } else if (position == 1) {
                ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(53, 173, 63));
                sb.setSpan(fcs, 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (position == 2) {
                holder.title.setTextColor(Color.RED);
            } else if (position == objects.size() - 1) {
                holder.title.setTextColor(Color.parseColor("#808080"));
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> colors = prefs.getStringSet("colorsOfCats", new HashSet<String>());
                holder.title.setTextColor(Color.BLUE);
            }
           /* int a = 3;
            for(String color : colors){
                if(a == position) {
                    holder.title.setTextColor(Integer.parseInt(color));
                    break;
                }
                a++;
            }*/
            holder.title.setText(sb);
            convertView.setTag(holder);
        }
        return convertView;
    }
}
