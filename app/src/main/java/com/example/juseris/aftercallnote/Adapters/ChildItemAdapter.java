package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.juseris.aftercallnote.Activities.ActivityPopupAfter;
import com.example.juseris.aftercallnote.DataExportation;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.FirebaseConnection;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.Models.Order;
import com.example.juseris.aftercallnote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by juseris on 3/17/2017.
 */


public class ChildItemAdapter extends RecyclerView.Adapter<ChildItemAdapter.ViewHolder> {
    private final Context context;
    private SharedPreferences prefs;
    private int selectedPos = 0;
    private Database db;

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton overFlow;
        TextView note;
        TextView time;
        TextView reminder;
        TextView category;
        TextView friendEmail;
        TextView name;

        TextView order_nr;

        int realPosition;
        int collapsedHeight, expandedHeight;
        private PopupMenu popup;

        public ViewHolder(View itemView) {
            super(itemView);
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            note = (TextView) itemView.findViewById(R.id.note);
            friendEmail = (TextView) itemView.findViewById(R.id.friendEmail);
            time = (TextView) itemView.findViewById(R.id.time);
            reminder = (TextView) itemView.findViewById(R.id.reminder);
            category = (TextView) itemView.findViewById(R.id.category);
            overFlow = (ImageButton) itemView.findViewById(R.id.overFlow);
            name = (TextView) itemView.findViewById(R.id.name);

            order_nr = (TextView) itemView.findViewById(R.id.order_nr);
            setOverFlowListener();
        }


        private void setOverFlowListener() {
            int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                    , context.getResources().getDisplayMetrics());
            Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_clock, null);
            img.setBounds(0, 0, imgSize, imgSize);
            time.setCompoundDrawables(img, null, null, null);
            if (overFlow != null) {
                overFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ClassNote note = ((ClassNote) objects.get(getAdapterPosition()));
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
                                        Intent i = new Intent(context, ActivityPopupAfter.class);
                                        i.putExtra("PhoneNumber", note.getPhoneNumber());
                                        i.putExtra("Note", note.getNotes(false));
                                        i.putExtra("ID", note.getId());
                                        i.putExtra("category", note.getCategory());
                                        i.putExtra("isSynced", note.isSynced());
                                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("haveToChooseContact", false).apply();
                                        context.startActivity(i);
                                        //editNote();
                                        return true;
                                    case R.id.delete_note:
                                        Database db = new Database(context);
                                        db.Delete_Note(note.getId());
                                        db.deleteFromSyncedTable(note.getId());
                                        FirebaseConnection con = new FirebaseConnection(context);
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        String email = "";
                                        if (user != null) {
                                            email = user.getEmail();
                                            String fixedEmail = email.replace(".", ",");
                                            con.addDataToFirebase(fixedEmail);
                                        }
                                        objects.remove(objects.get(getAdapterPosition()));
                                        notifyDataSetChanged();
                                        return true;
                                    case R.id.share_note:
                                        DataExportation de = new DataExportation(context);
                                        de.exportNote(note);
                                        return true;
                                }
                                return true;
                            }
                        });
                    }
                });
            }
        }
    }


    private LayoutInflater inflater;
    private List<IGenericItem> objects;

    public ChildItemAdapter(Context context, List<IGenericItem> objects) {
        this.objects = objects;
        this.context = context;
        inflater = LayoutInflater.from(context);
        db = new Database(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView;
        if (viewType == 0) {
            convertView = inflater.inflate(R.layout.list_item_in_contact, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.list_item_pretashop_order, parent, false);
        }
        return new ViewHolder(convertView);
    }

    @Override
    public int getItemViewType(int position) {
        if (objects.get(position) instanceof ClassNote) {
            return 0;
        } else {
            return 1;
        }
    }

    private void setCategoryColor(ViewHolder holder, int color, String title) {
        Drawable background = holder.category.getBackground();
        try {
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(context, color));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(ContextCompat.getColor(context, color));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(ContextCompat.getColor(context, color));
            }
        } catch (Exception e) {
            String hexColor = String.format("#%06X", (0xFFFFFF & color));
            if (background instanceof ShapeDrawable) {
                ((ShapeDrawable) background).getPaint().setColor(Color.parseColor(String.valueOf(hexColor)));
            } else if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(Color.parseColor(String.valueOf(hexColor)));
            } else if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(Color.parseColor(String.valueOf(hexColor)));
            }
        }
        holder.category.setText(title);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (objects.get(position) instanceof ClassNote) {
            if (objects == null || objects.size() == 0) {
            } else {
                ClassNote note = ((ClassNote) objects.get(holder.getAdapterPosition()));
                holder.note.setText(note.getNotes(true));
                String cat = note.getCategory();
                boolean hasCategory = !cat.equals("");
                if (hasCategory) {
                    Drawable background = holder.category.getBackground();
                    holder.category.setText(note.getCategoryTitle(context, background));
                } else {
                    holder.category.setVisibility(View.GONE);
                }

                holder.time.setText(note.getTimeTitle());

                if (note.isSynced() == 0) {
                } else {
                    String email = note.getFriendEmail().replace(",", ".");
                    int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13
                            , context.getResources().getDisplayMetrics());

                    Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_person, null);
                    Drawable drawable1 = new ScaleDrawable(img, 0, imgSize, imgSize).getDrawable();
                    drawable1.setBounds(0, 0, imgSize, imgSize);
                    holder.friendEmail.setCompoundDrawables(drawable1, null, null, null);
                    holder.friendEmail.setText(email);
                }
                if (!note.getReminder().equals("")) {
                    //setting reminder text
                    int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                            , context.getResources().getDisplayMetrics());

                    Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_reminder_icon, null);
                    Drawable drawable1 = new ScaleDrawable(img, 0, imgSize, imgSize).getDrawable();
                    drawable1.setBounds(0, 0, imgSize, imgSize);
                    holder.reminder.setCompoundDrawables(drawable1, null, null, null);
                    holder.reminder.setText(note.getReminder().substring(5, note.getReminder().length()));
                } else {
                    holder.reminder.setCompoundDrawables(null, null, null, null);
                    holder.reminder.setVisibility(View.GONE);
                }
                holder.realPosition = holder.getAdapterPosition();
            }
        } else {
            if (objects == null || objects.size() == 0) {
            } else {
                Order order = ((Order) objects.get(holder.getAdapterPosition()));
                setCategoryColor(holder, Color.GREEN, order.getOrder_state());
                int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15
                        , context.getResources().getDisplayMetrics());
                Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_category_icon, null);
                SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                Date result;
                try {
                    result = df1.parse(order.getDate());
                } catch (ParseException e) {
                    result = new Date();
                }
                holder.time.setText(String.format("%s", df.format(result)));
                img.setBounds(0, 0, imgSize, imgSize);
                holder.order_nr.setCompoundDrawables(img, null, null, null);
                holder.order_nr.setText(order.getOrder_nr());
                holder.name.setVisibility(View.GONE);
            }
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


// view that will expand/collapse your TextView
         /*   note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // number of max lines when collapsed
                    if (note.getLayout().getLineCount() == 1) {
                        // expand
                        note.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                        note.setMaxLines(Integer.MAX_VALUE);
                        note.measure(0,0);
                        expandedHeight = note.getMeasuredHeight()+px;
                        ObjectAnimator animation = ObjectAnimator.ofInt(note, "height", collapsedHeight, expandedHeight);
                        animation.setDuration(250).start();
                    } else {
                        // collapse
                        ObjectAnimator animation = ObjectAnimator.ofInt(note, "height", expandedHeight+px, collapsedHeight);
                        animation.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {
                                note.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                // number of max lines when collapsed
                                note.setMaxLines(1);
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        animation.setDuration(250).start();
                    }
                }
            });*/