package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.Activities.MainListChildItem;
import com.example.juseris.aftercallnote.DataExportation;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Created by juseris on 3/17/2017.
 */
public class ChildItemAdapter extends BaseAdapter {
    private Context context;

    class ViewHolder {
        TextView textName;
        TextView textDate;
        TextView reminder;
        ImageView overFlowInContact;
    }

    private LayoutInflater inflater;
    private ArrayList<ClassNote> objects;

    public ChildItemAdapter(Context context, ArrayList<ClassNote> objects) {
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
        for (ClassNote note : objects) {
            Log.d(note.getName(), note.getNotes(true));
        }
    }

    public int getCount() {
        return objects.size();
    }

    public ClassNote getItem(int position) {
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
            convertView = inflater.inflate(R.layout.list_item_in_contact, parent, false);
            holder.textName = (TextView) convertView.findViewById(R.id.textName);
            holder.textDate = (TextView) convertView.findViewById(R.id.textDate);
            holder.reminder  = (TextView)convertView.findViewById(R.id.textReminder);

            holder.overFlowInContact = (ImageView) convertView.findViewById(R.id.overFlowInContact);
            holder.textName.setText(objects.get(position).getNotes(false));

            Log.d("POSITION", String.valueOf(position));

            int seconds = 0;
            int minutes = 0;
            try {
                seconds = Integer.parseInt(getItem(position).getCallTime());
                while (seconds - 60 >= 0) {
                    minutes++;
                    seconds -= 60;
                }
            } catch (NumberFormatException e) {
                //Will Throw exception!
                //do something! anything to handle the exception.
            }
            if(objects.get(position).isSynced() == 0) {
                final SpannableStringBuilder sb = new SpannableStringBuilder();
                ForegroundColorSpan categoryColor = new ForegroundColorSpan(Color.rgb(53, 173, 63));
                int catLength = 0 ;
                if(objects.get(position).getCategory().equals("Personal ")){
                    categoryColor = new ForegroundColorSpan(Color.rgb(53, 173, 63));
                    catLength = 9;
                }else if(objects.get(position).getCategory().equals("Important ")){
                    categoryColor = new ForegroundColorSpan(Color.RED);
                    catLength = 10;
                }else if(objects.get(position).getCategory().equals("Vip contact ")){
                    categoryColor = new ForegroundColorSpan(Color.MAGENTA);
                    catLength = 12;
                }

                if (minutes != 0) {
                    sb.append(objects.get(position).getCategory()+objects.get(position).getCallDate()
                            + " ; " + minutes + " min " + seconds + " s");
                    if(!objects.get(position).getCategory().equals("")) {
                        sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    holder.textDate.setText(sb);

                } else {
                    if (seconds == 0) {
                        sb.append(objects.get(position).getCategory()+objects.get(position).getCallDate());
                        if(!objects.get(position).getCategory().equals("")) {
                            sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        holder.textDate.setText(sb);
                    } else {
                        sb.append(objects.get(position).getCategory()+objects.get(position).getCallDate()
                                + " ; " + seconds + " s");
                        if(!objects.get(position).getCategory().equals("")) {
                            sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        holder.textDate.setText(sb);
                    }
                }
            }else{
                final SpannableStringBuilder sb = new SpannableStringBuilder();
                final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#58a7c8"));
                ForegroundColorSpan categoryColor = new ForegroundColorSpan(Color.parseColor("#ff4081"));
                int catLength = 0;
                if(objects.get(position).getCategory().equals("Personal ")){
                    categoryColor = new ForegroundColorSpan(Color.rgb(53, 173, 63));
                    catLength = 9;
                }else if(objects.get(position).getCategory().equals("Important ")){
                    categoryColor = new ForegroundColorSpan(Color.RED);
                    catLength = 10;
                }else if(objects.get(position).getCategory().equals("Vip contact ")){
                    categoryColor = new ForegroundColorSpan(Color.MAGENTA);
                    catLength = 12;
                }
                int length = objects.get(position).getCallDate().length() + 1;
                int emailLength = objects.get(position).getFriendEmail().length() + 1;
                String email = objects.get(position).getFriendEmail().replace(",", ".");

                sb.append(objects.get(position).getCategory()+objects.get(position).getCallDate() + ", " + email);
                if(catLength > 0) {
                    sb.setSpan(categoryColor, 0, catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                sb.setSpan(fcs, length + catLength, length + emailLength + catLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.textDate.setText(sb);

                holder.textDate.setText(sb);
            }
            if (!getItem(position).getReminder().equals("")) {
                int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                        , context.getResources().getDisplayMetrics());
                Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_reminder_icon, null);
                img.setBounds(0, 0, imgSize, imgSize);
                holder.reminder.setCompoundDrawables(img, null, null, null);
                holder.reminder.setText(objects.get(position).getReminder()
                        .substring(5, objects.get(position).getReminder().length()));
                if (objects.get(position).isSynced() == 1) {
                   // holder.reminder.setVisibility(View.GONE);
                }
            } else {
               // int botPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5
               //         , context.getResources().getDisplayMetrics());
               // holder.textDate.setPadding(0, 0, 0, botPadding);
                holder.reminder.setVisibility(View.GONE);
            }


            holder.overFlowInContact.setTag(position);
            holder.overFlowInContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Integer itemIndex = (Integer) v.getTag();
                    Context wrapper = new ContextThemeWrapper(context, R.style.MyPopupMenu);
                    PopupMenu popup = new PopupMenu(wrapper, v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.edit_delete_note, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_note:
                                    Intent i = new Intent(context,ActivityPopupAfter.class);
                                    i.putExtra("PhoneNumber",objects.get(itemIndex).getPhoneNumber());
                                    i.putExtra("Note",objects.get(itemIndex).getNotes(false));
                                    i.putExtra("ID",objects.get(itemIndex).getID());
                                    context.startActivity(i);
                                    //editNote();
                                    return true;
                                case R.id.delete_note:
                                    Database db = new Database(context);
                                    db.Delete_Note(objects.get(itemIndex).getID());
                                    db.deleteFromSyncedTable(objects.get(itemIndex).getID());
                                    FirebaseConnection con = new FirebaseConnection(context);
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String email = "";
                                    if (user != null) {
                                        email = user.getEmail();
                                        String fixedEmail = email.replace(".", ",");
                                        con.addDataToFirebase(fixedEmail);
                                    }
                                    objects.remove(getItem(itemIndex));
                                    notifyDataSetChanged();
                                    return true;
                                case R.id.share_note:
                                    DataExportation de = new DataExportation(context);
                                    de.exportNote(objects.get(itemIndex));
                                    return true;
                            }
                            return true;
                        }
                    });
                }
            });
            convertView.setTag(holder);
        }
        return convertView;
    }
}