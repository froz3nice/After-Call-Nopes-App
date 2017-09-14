package com.example.juseris.aftercallnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.MainListChildItem;
import com.example.juseris.aftercallnote.Database;
import com.example.juseris.aftercallnote.Models.CategoriesAndColors;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.Models.Order;
import com.example.juseris.aftercallnote.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by juseris on 9/6/2017.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> implements Filterable {

    private ArrayList<IGenericItem> notesCopy;
    private Context context;
    private Database db;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView note;
        TextView time;
        TextView reminder;
        TextView category;
        TextView friendEmail;
        ImageView text;
        ImageView call;

        int realPosition;
        int collapsedHeight, expandedHeight;
        public TextView order_nr;

        public ViewHolder(View itemView) {
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
            order_nr = (TextView) itemView.findViewById(R.id.order_nr);

            // float textViewWidthPx = note.getWidth();

            // get collapsed height after TextView is drawn
          /*  note.post(new Runnable() {
                @Override
                public void run() {
                    collapsedHeight = note.getMeasuredHeight();
                }
            });*/
            final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 17, context.getResources().getDisplayMetrics());
            if (note != null) {
                note.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, MainListChildItem.class);
                        intent.putExtra("classNoteobj", ((ClassNote) objects.get(getAdapterPosition())));
                        context.startActivity(intent);
                    }
                });
            }


            int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                    , context.getResources().getDisplayMetrics());
            Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_clock, null);
            img.setBounds(0, 0, imgSize, imgSize);
            time.setCompoundDrawables(img, null, null, null);
        }

        @Override
        public void onClick(View view) {
            if (objects.get(getAdapterPosition()) instanceof ClassNote) {
                Intent intent = new Intent(context, MainListChildItem.class);
                intent.putExtra("classNoteobj", ((ClassNote) objects.get(getAdapterPosition())));
                context.startActivity(intent);
            } else {
                //Toast.makeText(context, ((Order) objects.get(getAdapterPosition())).getPhone_nr(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private LayoutInflater inflater;
    private ArrayList<IGenericItem> objects;

    public MainAdapter(Context context, ArrayList<IGenericItem> objects) {
        this.context = context;
        this.objects = objects;
        inflater = LayoutInflater.from(context);
        db = new Database(context);
        notesCopy = objects;
    }

    MainAdapter.SearchFilter mContactsFilter;

    @Override
    public Filter getFilter() {
        if (mContactsFilter == null)
            mContactsFilter = new MainAdapter.SearchFilter();

        return mContactsFilter;
    }

    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // Create a FilterResults object
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = notesCopy;
                results.count = notesCopy.size();
            } else {
                ArrayList<IGenericItem> filteredContacts = new ArrayList<>();

                for (IGenericItem gen : notesCopy) {
                    if (gen instanceof ClassNote) {
                        ClassNote c = ((ClassNote) gen);
                        if (c.getNotes(false).toUpperCase().contains(constraint.toString().toUpperCase())
                                || c.getName().toUpperCase().contains(constraint.toString().toUpperCase())
                                || c.getCallDate().toUpperCase().contains(constraint.toString().toUpperCase())
                                || c.getPhoneNumber().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            filteredContacts.add(c);
                        }
                    } else {
                        Order c = ((Order) gen);
                        String name_surname = String.format("%s %s", c.getName().toUpperCase(), c.getSurname().toUpperCase());
                        if (name_surname.contains(constraint.toString().toUpperCase())
                                || c.getOrder_nr().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            filteredContacts.add(c);
                        }
                    }
                }

                // Finally set the filtered values and size/count
                results.values = filteredContacts;
                results.count = filteredContacts.size();
            }
            // Return our FilterResults object
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            objects = (ArrayList<IGenericItem>) results.values;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (objects.get(position) instanceof ClassNote) {
            return 0;
        } else {
            return 1;
        }
    }

    public IGenericItem getItem(int position) {
        return objects.get(position);
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView;
        if (viewType == 0) {
            convertView = inflater.inflate(R.layout.list_item_main, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.list_item_pretashop_order, parent, false);
            convertView.setOnClickListener(null);
        }
        return new ViewHolder(convertView);
    }

    private void setCategoryColor(MainAdapter.ViewHolder holder, int color, String title) {
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
    public void onBindViewHolder(MainAdapter.ViewHolder holder, int position) {
        if (objects.get(position) instanceof ClassNote) {

            if (objects == null || objects.size() == 0) {
                //holder.name.setText("No Results");
            } else {
                final ClassNote item = ((ClassNote) objects.get(holder.getAdapterPosition()));
                if (!item.getName().equals("")) {
                    holder.name.setText(item.getName());
                } else {
                    holder.name.setText(item.getPhoneNumber());
                }
                holder.note.setText(item.getNotes(true));
                String cat = item.getCategory();//categories stored in db with space in the end
                boolean hasCategory = !cat.equals("");
                if (hasCategory) {
                    Drawable background = holder.category.getBackground();
                    holder.category.setText(item.getCategoryTitle(context, background));
                } else {
                    holder.category.setVisibility(View.GONE);
                }
                holder.time.setText(item.getTimeTitle());

                if (item.isSynced() != 0) {
                    setSyncedNoteText(item, holder);
                }
                if (!item.getReminder().equals("")) {
                    //setting reminder text
                    int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12
                            , context.getResources().getDisplayMetrics());
                    Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_reminder_icon, null);
                    img.setBounds(0, 0, imgSize, imgSize);
                    holder.reminder.setCompoundDrawables(img, null, null, null);
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
                holder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:" + item.getPhoneNumber()));
                        context.startActivity(sendIntent);
                    }
                });

                holder.call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (item.getPhoneNumber().equalsIgnoreCase("None")) {
                            Toast.makeText(context, "Sorry, you cant make a call when number is \"None\""
                                    , Toast.LENGTH_LONG).show();
                        } else {
                            Uri number = Uri.parse("tel:" + item.getPhoneNumber());
                            Intent i = new Intent(Intent.ACTION_DIAL, number);
                            context.startActivity(i);
                        }
                    }
                });
            }
        } else {
            if (objects == null || objects.size() == 0) {
            } else {
                Order order = ((Order) objects.get(holder.getAdapterPosition()));
                setCategoryColor(holder, Color.GREEN, order.getOrder_state());
                int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15
                        , context.getResources().getDisplayMetrics());
                Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_category_icon, null);
                img.setBounds(0, 0, imgSize, imgSize);
                SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                Date result;
                try {
                    result = df1.parse(order.getDate());
                } catch (ParseException e) {
                    result = new Date();
                }
                holder.time.setText(String.format("%s", df.format(result)));
                holder.order_nr.setCompoundDrawables(img, null, null, null);
                holder.order_nr.setText(order.getOrder_nr());
                holder.name.setText(String.format("%s %s", order.getName(), order.getSurname()));
            }
        }
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objects == null ? 0 : objects.size();
    }

    private void setSyncedNoteText(ClassNote item, MainAdapter.ViewHolder holder) {
        String email = item.getFriendEmail().replace(",", ".");
        int imgSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13
                , context.getResources().getDisplayMetrics());

        Drawable img = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_person, null);
        Drawable drawable1 = new ScaleDrawable(img, 0, imgSize, imgSize).getDrawable();
        drawable1.setBounds(0, 0, imgSize, imgSize);

        holder.friendEmail.setCompoundDrawables(drawable1, null, null, null);
        holder.friendEmail.setText(email);
    }
}


// number of max lines when collapsed
                        /*if (note.getLayout().getLineCount() == 1) {
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
                                public void onAnimationCancel(Animator animator) {}
                                @Override
                                public void onAnimationRepeat(Animator animator) {}
                            });
                            animation.setDuration(250).start();
                        }*/
