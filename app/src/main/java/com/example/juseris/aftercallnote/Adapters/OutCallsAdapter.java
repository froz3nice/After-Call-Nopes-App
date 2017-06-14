package com.example.juseris.aftercallnote.Adapters;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

/**
 * Created by juseris on 3/17/2017.
 */


public class OutCallsAdapter extends BaseAdapter {
    class ViewHolder {
        TextView text1;
        TextView text2;
        ImageButton overflow;
    }

    private LayoutInflater inflater;
    private ArrayList<ContactEntity> objects;
    private Context context;

    public OutCallsAdapter(Context context, ArrayList<ContactEntity> objects) {
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
            convertView = inflater.inflate(R.layout.incoming_call_item, parent, false);
            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            holder.overflow = (ImageButton)convertView.findViewById(R.id.overFlow);
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
                if(_date.equals("ExceptionTrigger")) {
                    // int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
                    //        , context.getResources().getDisplayMetrics());
                    //holder.text1.setPadding(0, 0, 0, botPadding);
                    // holder.text2.setVisibility(View.GONE);
                    holder.text2.setText(objects.get(position).getNumber());
                    Log.e("Tag", "Exception");
                }else {
                    holder.text2.setText(_date + ", " + objects.get(position).getCallDuration());
                }
                holder.overflow.setTag(position);
                holder.overflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Integer itemIndex = (Integer) v.getTag();
                        Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                        PopupMenu popup = new PopupMenu(wrapper, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.delete_call, popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:
                                        Database db = new Database(context);
                                        db.deleteOutCall(objects.get(itemIndex));
                                        objects.remove(getItem(itemIndex));
                                        notifyDataSetChanged();
                                        return true;

                                }
                                return true;
                            }
                        });
                    }
                });
            }
            convertView.setTag(holder);
        }
        return convertView;
    }

}

