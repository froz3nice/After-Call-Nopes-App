package com.example.juseris.aftercallnote.Adapters;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Activities.MainActivity;
import com.example.juseris.aftercallnote.Activities.MainListChildItem;
import com.example.juseris.aftercallnote.Activities.RemindersList;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.R;

import java.util.ArrayList;

/**
 * Created by juseris on 3/17/2017.
 */

public class AllRemindersAdapter extends RecyclerView.Adapter<AllRemindersAdapter.ItemViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text1;
        TextView text2;
        TextView text3;
        TextView text4;
        TextView title;
        ImageView overFlow;
        int realPosition;
        View divider;
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            text1 = (TextView) itemView.findViewById(R.id.text1);
            text2 = (TextView) itemView.findViewById(R.id.text2);
            text3 = (TextView) itemView.findViewById(R.id.text3);
            text4 = (TextView) itemView.findViewById(R.id.text4);
            title = (TextView) itemView.findViewById(R.id.text);
            overFlow = (ImageView) itemView.findViewById(R.id.overFlow);
            divider = itemView.findViewById(R.id.divider);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, MainListChildItem.class);
            intent.putExtra("classNoteobj", objects.get(getAdapterPosition()));
            context.startActivity(intent);//;startActivityForResult(intent, 0xe420);
        }
    }
    private LayoutInflater inflater;
    private ArrayList<ClassNote> objects;
    private Context context;
    public AllRemindersAdapter(Context context, ArrayList<ClassNote> objects) {
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return objects.size();
    }

    public ClassNote getItem(int position) {
        return objects.get(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public int getItemViewType(int position) {
        if(objects.get(position).isSection) {
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View convertView = inflater.inflate(R.layout.main_list_item, parent, false);

            return new ItemViewHolder(convertView);
        }else{
            View convertView = inflater.inflate(R.layout.section_header, parent, false);
            convertView.setEnabled(false);
            convertView.setOnClickListener(null);
            //TextView tvSectionTitle = (TextView) convertView.findViewById(R.id.tvSectionTitle);
            // tvSectionTitle.setText(((SectionItem) item.get(position)).getTitle());
            return new ItemViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if(!objects.get(position).isSection) {
                if (objects == null || objects.size() == 0) {
                    //holder.text1.setText("No Results");
                } else {
                    if (!objects.get(position).getName().equals("")) {
                        holder.text1.setText(objects.get(position).getName());
                    } else {
                        holder.text1.setText(objects.get(position).getPhoneNumber());
                    }
                    //Log.d("POSITION", String.valueOf(position));

                    int seconds = 0;
                    int minutes = 0;
                    try {
                        seconds = Integer.parseInt(objects.get(position).getCallTime());
                        while (seconds - 60 >= 0) {
                            minutes++;
                            seconds -= 60;
                        }
                    } catch (NumberFormatException e) {
                        //Will Throw exception!
                        //do something! anything to handle the exception.
                    }
                    holder.text2.setText(objects.get(position).getNotes(true));
                    if (minutes != 0) {
                        holder.text3.setText(objects.get(position).getCallDate()
                                + " ; " + minutes + " min " + seconds + " s");
                    } else {
                        if (seconds == 0) {
                            holder.text3.setText(objects.get(position).getCallDate());
                        } else {
                            holder.text3.setText(objects.get(position).getCallDate()
                                    + " ; " + seconds + " s");
                        }
                    }
                    if (!objects.get(position).getReminder().equals("")) {
                        int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13
                                , context.getResources().getDisplayMetrics());
                        Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_reminder_icon, null);
                        img.setBounds(0, 0, imgSize, imgSize);
                        holder.text4.setCompoundDrawables(img, null, null, null);
                        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        // holder.text4.removeOnLayoutChangeListener(this);
                        // }
                        holder.text4.setText(objects.get(position).getReminder()
                                .substring(5,objects.get(position).getReminder().length()));
                    } else {
                        int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
                                , context.getResources().getDisplayMetrics());
                        holder.text3.setPadding(0, 0, 0, botPadding);
                        holder.text4.setVisibility(View.GONE);
                    }
                    /*int leftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13
                            , context.getResources().getDisplayMetrics());
                    holder.text1.setPadding(leftPadding,0,0,0);
                    holder.text2.setPadding(leftPadding,0,0,0);
                    holder.text3.setPadding(leftPadding,0,0,0);
                    holder.text4.setPadding(leftPadding,0,0,0);*/

                    holder.realPosition = position;
                    holder.overFlow.setVisibility(View.GONE);
                    if(holder.getAdapterPosition() + 1 < objects.size()){
                        if(objects.get(holder.getAdapterPosition()+1).isSection){
                             holder.divider.setVisibility(View.GONE);
                        }
                    }
                   /* if(position == objects.size()-1){
                         //holder.divider.setVisibility(View.GONE);
                    }*/
                }
        }else{
            holder.title.setText(getItem(position).getName());
        }
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }
}
