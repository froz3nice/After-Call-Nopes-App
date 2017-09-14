package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

/**
 * Created by juseris on 3/17/2017.
 */


public class ContactsAdapter extends BaseAdapter {
    class ViewHolder {
        TextView text1;
        TextView text2;
    }

    private LayoutInflater inflater;
    private ArrayList<ContactEntity> objects;
    private Context context;

    public ContactsAdapter(Context context, ArrayList<ContactEntity> objects) {
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return objects.size();
    }

    public ContactEntity getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder = new ViewHolder();
        convertView = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.outgoing_contacts_item, parent, false);
            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            if (objects == null || objects.size() == 0) {
            } else {

                if (!objects.get(position).getName().equals("")) {
                    holder.text1.setText(objects.get(position).getName());
                } else {
                    holder.text1.setText(objects.get(position).getNumber());
                }
                //Log.d("POSITION", String.valueOf(position));
                String _date = "";
                _date = objects.get(position).getCallTime();
                if (_date.equals("ExceptionTrigger")) {
                    // int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
                    //        , context.getResources().getDisplayMetrics());
                    //holder.text1.setPadding(0, 0, 0, botPadding);
                    // holder.text2.setVisibility(View.GONE);
                    holder.text2.setText(objects.get(position).getNumber());
                    Log.e("Tag", "Exception");
                } else {
                    holder.text2.setText(_date + ", " + objects.get(position).getCallDuration());
                }
            }
            convertView.setTag(holder);
        }
        return convertView;
    }
}
