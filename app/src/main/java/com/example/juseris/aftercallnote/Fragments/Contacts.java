package com.example.juseris.aftercallnote.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.juseris.aftercallnote.Activities.MainListChildItem;
import com.example.juseris.aftercallnote.Adapters.ContactsAdapter;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.R;
import com.example.juseris.aftercallnote.UtilsPackage.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by juseris on 3/24/2017.
 */


public class Contacts extends Fragment {

    public static final String ARG_OBJECT = "object";
    private ListView incomingLog;
    private ArrayList<ContactEntity> contacts = new ArrayList<>();
    private RelativeLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.outgoing_calls_fragment, container, false);
        layout = (RelativeLayout) rootView.findViewById(R.id.loadingPanel);
        incomingLog = (ListView) rootView.findViewById(R.id.incomingCalls);
        LoadContact loadContact = new LoadContact();
        loadContact.execute();
        return rootView;

    }

    // Load data on background
    class LoadContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            contacts = new ArrayList<>();
            if (getActivity() != null) {
                Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                if (phones != null) {
                    while (phones.moveToNext()) {
                        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.add(new ContactEntity(name, phoneNumber, "ExceptionTrigger", "0"));

                    }
                    phones.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (getActivity() != null) {
                Collections.sort(contacts, new Comparator<ContactEntity>() {
                    @Override
                    public int compare(ContactEntity a, ContactEntity b) {
                        return a.getName().compareTo(b.getName());
                    }
                });
                final ContactsAdapter listAdapter = new ContactsAdapter(getActivity(), contacts);
                listAdapter.notifyDataSetChanged();
                incomingLog.setAdapter(listAdapter);
                incomingLog.setItemsCanFocus(true);
                boolean chooseContact = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getBoolean("haveToChooseContact", false);
                if (chooseContact) {
                    incomingLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) view.getTag();
                            //Intent intent = new Intent(getActivity(), ActivityPopupAfter.class);
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().putString("ChosenNumber", listAdapter.getItem(position).getNumber()).apply();                       // startActivity(intent);
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().putBoolean("haveToChooseContact", false).apply();
                            getActivity().finish();
                        }
                    });
                } else {
                    incomingLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) view.getTag();
                            Intent intent = new Intent(getActivity(), MainListChildItem.class);
                            ClassNote note = new ClassNote();
                            note.setPhoneNumber(Utils.fixNumber(listAdapter.getItem(position).getNumber()));
                            note.setName(listAdapter.getItem(position).getName());
                            intent.putExtra("classNoteobj", note);
                            startActivity(intent);
                        }
                    });
                }
                incomingLog.setFastScrollEnabled(true);
                layout.setVisibility(View.GONE);
            }
        }
    }
}
