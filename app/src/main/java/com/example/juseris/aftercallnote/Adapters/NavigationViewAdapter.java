package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

/**
 * Created by juseris on 3/17/2017.
 */

public class NavigationViewAdapter extends BaseAdapter {
    class ViewHolder {
        TextView text1;
    }
    private LayoutInflater inflater;
    private ArrayList<String> objects;

    public NavigationViewAdapter(Context context, ArrayList<String> objects) {
        this.objects = objects;
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
        final ViewHolder holder = new ViewHolder();
        convertView = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.nav_item, parent, false);
            holder.text1 = (TextView) convertView.findViewById(R.id.textView10);
            /*if(position == objects.size()-1){
                holder.text1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                holder.text1.setCompoundDrawables(null, null, null, null);
                holder.text1.setTextColor(Color.BLACK);
            }*/
            holder.text1.setText(getItem(position));
            convertView.setTag(holder);
        }
        return convertView;
    }
}
