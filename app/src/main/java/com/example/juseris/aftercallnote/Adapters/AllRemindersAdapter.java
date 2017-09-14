package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Activities.MainListChildItem;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.CategoriesAndColors;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by juseris on 3/17/2017.
 */

public class AllRemindersAdapter extends RecyclerView.Adapter<AllRemindersAdapter.ItemViewHolder> {

    private Database database;

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView note;
        TextView time;
        TextView reminder;
        TextView category;
        TextView friendEmail;
        ImageView text;
        ImageView call;
        int realPosition;
        TextView title;

        int collapsedHeight, expandedHeight;

        //View divider;
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = (TextView) itemView.findViewById(R.id.name);
            note = (TextView) itemView.findViewById(R.id.note);
            friendEmail = (TextView) itemView.findViewById(R.id.friendEmail);
            time = (TextView) itemView.findViewById(R.id.time);
            reminder = (TextView) itemView.findViewById(R.id.reminder);
            category = (TextView) itemView.findViewById(R.id.category);
            text = (ImageView) itemView.findViewById(R.id.textToContact);
            call = (ImageView) itemView.findViewById(R.id.callToContact);
            title = (TextView) itemView.findViewById(R.id.text);
            //overFlow = (ImageView) itemView.findViewById(R.id.overFlow);
            // divider = itemView.findViewById(R.id.divider);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, MainListChildItem.class);
            intent.putExtra("classNoteobj", ((ClassNote) objects.get(getAdapterPosition())));
            context.startActivity(intent);//;startActivityForResult(intent, 0xe420);
        }
    }

    private LayoutInflater inflater;
    private ArrayList<IGenericItem> objects;
    private Context context;

    public AllRemindersAdapter(Context context, ArrayList<IGenericItem> objects) {
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
        database = new Database(context);
    }

    public int getCount() {
        return objects.size();
    }

    public IGenericItem getItem(int position) {
        return objects.get(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public int getItemViewType(int position) {
        if (((ClassNote) objects.get(position)).isSection) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View convertView = inflater.inflate(R.layout.list_item_main, parent, false);

            return new ItemViewHolder(convertView);
        } else {
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
        final ClassNote item = ((ClassNote) objects.get(holder.getAdapterPosition()));
        if (!item.isSection) {

            if (objects == null || objects.size() == 0) {
                //holder.name.setText("No Results");
            } else {
                if (!item.getName().equals("")) {
                    holder.name.setText(item.getName());
                } else {
                    holder.name.setText(item.getPhoneNumber());
                }

                int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                        , context.getResources().getDisplayMetrics());
                Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_clock, null);
                img.setBounds(0, 0, imgSize, imgSize);
                holder.time.setCompoundDrawables(img, null, null, null);
                holder.note.setText(item.getNotes(true));
                holder.note.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, MainListChildItem.class);
                        intent.putExtra("classNoteobj", item);
                        context.startActivity(intent);
                    }
                });
                String cat = item.getCategory();
                boolean hasCategory = !cat.equals("");
                if (hasCategory) {
                    Drawable background = holder.category.getBackground();
                    holder.category.setText(item.getCategoryTitle(context, background));
                } else {
                    holder.category.setVisibility(View.GONE);
                }
                holder.time.setText(item.getTimeTitle());

                if (item.isSynced() != 0) {
                    String email = item.getFriendEmail().replace(",", ".");
                    Drawable img2 = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_person, null);
                    Drawable drawable1 = new ScaleDrawable(img2, 0, imgSize, imgSize).getDrawable();
                    drawable1.setBounds(0, 0, imgSize, imgSize);
                    holder.friendEmail.setCompoundDrawables(drawable1, null, null, null);
                    holder.friendEmail.setText(email);
                }
                if (!item.getReminder().equals("")) {
                    //setting reminder text
                    Drawable img2 = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_reminder_icon, null);
                    Drawable drawable1 = new ScaleDrawable(img2, 0, imgSize, imgSize).getDrawable();
                    drawable1.setBounds(0, 0, imgSize, imgSize);
                    holder.reminder.setCompoundDrawables(drawable1, null, null, null);
                    holder.reminder.setText(item.getReminder()
                            .substring(5, item.getReminder().length()));
                } else {
                    int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
                            , context.getResources().getDisplayMetrics());
                    holder.time.setPadding(0, 0, 0, botPadding);
                    holder.reminder.setCompoundDrawables(null, null, null, null);
                    holder.reminder.setVisibility(View.GONE);
                }
                holder.realPosition = holder.getAdapterPosition();
            }
            holder.realPosition = position;
            if (holder.getAdapterPosition() + 1 < objects.size()) {
                if (((ClassNote) objects.get(holder.getAdapterPosition() + 1)).isSection) {
                    //holder.divider.setVisibility(View.GONE);
                }
            }

        } else {
            holder.title.setText(item.getName());
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
