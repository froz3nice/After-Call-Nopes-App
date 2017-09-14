package com.example.juseris.aftercallnote;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Activities.MainActivity;
import com.example.juseris.aftercallnote.Models.ClassNote;
import com.example.juseris.aftercallnote.Models.ContactEntity;
import com.example.juseris.aftercallnote.Models.ContactsEntity;
import com.example.juseris.aftercallnote.Models.DataForSyncingModel;
import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by juseris on 3/17/2017.
 */

public class FirebaseConnection {
    private MainActivity activity;
    private RelativeLayout progressLayout;
    Context context;
    DatabaseReference myRef;
    FirebaseDatabase database;
    Database db;

    public FirebaseConnection(Context ctx, MainActivity activity) {
        context = ctx;
        myRef = Utils.getDatabase().getReference();
        db = new Database(context);
        this.activity = activity;
        progressLayout = (RelativeLayout) activity.findViewById(R.id.loadingPanel);

    }

    public FirebaseConnection(Context ctx) {
        context = ctx;
        myRef = Utils.getDatabase().getReference();
        db = new Database(context);
    }

    public void addIncomingCalls(final String email, final ArrayList<ContactEntity> contacts) {
        final DatabaseReference userRef = myRef.child("IncomingCalls").child(email);
        boolean hasSyncedCall = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("IncomingSynced", false);
        if (!hasSyncedCall) {
            DatabaseReference rootRef = myRef.child("IncomingCalls");
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild(email)) {
                        // run some code
                        userRef.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GenericTypeIndicator<ArrayList<ContactsEntity>> t =
                                        new GenericTypeIndicator<ArrayList<ContactsEntity>>() {
                                        };
                                ArrayList<ContactEntity> list = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    list.add(child.getValue(ContactEntity.class));
                                }
                                db.insertIncomingCalls(list);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Collections.reverse(contacts);
                        userRef.setValue(contacts);
                        db.insertIncomingCalls(contacts);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean("IncomingSynced", true).apply();
        }
    }


    public void fetchDataFromFirebase(final String email) {
        DatabaseReference SharedList = myRef.child("SyncList").child(email);
        SharedList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // Load data on background
                class LoadContact extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        if (progressLayout != null) {
                            progressLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        ArrayList<String> list = new ArrayList<>();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String email = childSnapshot.getKey();
                            list.add(email);
                        }
                        for (final String item : list) {
                            DatabaseReference othersList = myRef.child("SyncList").child(item);
                            othersList.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(email)) {
                                        DatabaseReference ref = myRef.child("Notes").child(item);
                                        ref.keepSynced(true);
                                        //ref.limitToFirst(1);
                                        ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                addSyncedData(dataSnapshot, item);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.w("RABARBARAS", "Failed to read value.", databaseError.toException());
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if (progressLayout != null) {
                            progressLayout.setVisibility(View.GONE);
                        }
                    }
                }
                new LoadContact().execute();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("RABARBARAS", "Failed to read value.", error.toException());
            }
        });
    }

    public void addSyncedData(final DataSnapshot dataSnapshot, final String FriendEmail) {

        // Load data on background
        class LoadContact extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... voids) {
                GenericTypeIndicator<ArrayList<DataForSyncingModel>> t =
                        new GenericTypeIndicator<ArrayList<DataForSyncingModel>>() {
                        };
                ArrayList<DataForSyncingModel> value = dataSnapshot.getValue(t);
                if (value != null) {
                    ArrayList<ClassNote> list = new ArrayList<>();
                    ArrayList<IGenericItem> synced = db.getSyncedData();
                    for (DataForSyncingModel syncedNote : value) {
                        if (syncedNote != null) {
                            list.add(initializeClassNote(syncedNote, FriendEmail));
                        }
                    }
                    Iterator<ClassNote> iter = list.iterator();
                    while (iter.hasNext()) {
                        ClassNote c = iter.next();

                        for (IGenericItem n : synced) {
                            if (n instanceof ClassNote) {
                                if (isEqualNote(c, n) || c.getCategory().equals("Personal ")) {
                                    iter.remove();
                                    break;
                                }
                            }
                        }
                    }
                    db.insertToSyncedTable(list);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.refreshList();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (progressLayout != null) {
                    progressLayout.setVisibility(View.GONE);
                }
            }
        }
        new LoadContact().execute();
        // db.insertSyncedData(list,1);
    }

    public void addOutgoingCalls(final String email, final ArrayList<ContactEntity> contacts) {
        final DatabaseReference userRef = myRef.child("OutgoingCalls").child(email);
        boolean hasSyncedCall = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("OutgoingSynced", false);
        if (!hasSyncedCall) {
            DatabaseReference rootRef = myRef.child("OutgoingCalls");
            db.deleteIncomingCalls();
            db.deleteOutgoingCalls();
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild(email)) {
                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ArrayList<ContactEntity> list = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    list.add(child.getValue(ContactEntity.class));
                                }
                                db.insertOutgoingCalls(list);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Collections.reverse(contacts);
                        userRef.setValue(contacts);
                        db.insertOutgoingCalls(contacts);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean("OutgoingSynced", true).apply();
        }
    }

    public void addSyncEmail(final String email, final String input) {

        final DatabaseReference userRef = myRef.child("SyncList").child(email);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                if (!snapshot.hasChild(input)) {
                    userRef.child(input).setValue("");
                } else {
                    Toast.makeText(context, "Email already added", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteNote(String noteID) {
        myRef = Utils.getDatabase().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail().replace(".", ",");
            DatabaseReference db_node = myRef.child("Notes").child(email).child(noteID);
            db_node.setValue(null);
        }

    }

    public void deleteNode(String myEmail, String emailToDelete) {
        myRef = Utils.getDatabase().getReference();
        DatabaseReference db_node = myRef.child("SyncList").child(myEmail).child(emailToDelete);
        db_node.setValue(null);
    }

    public void addDataToFirebase(String email) {
        DatabaseReference userRef = myRef.child("Notes").child(email);
        ArrayList<IGenericItem> l = db.getData();
        ArrayList<DataForSyncingModel> list = new ArrayList<>();
        for (IGenericItem c : l) {
            if (c instanceof ClassNote) {
                if (((ClassNote) c).isSynced() == 0) {
                    list.add(new DataForSyncingModel(((ClassNote) c).getCallDate(), ((ClassNote) c).getNotes(true), ((ClassNote) c).getPhoneNumber(), ((ClassNote) c).getCategory()));
                }
            }
        }
        if (!list.isEmpty()) {
            userRef.setValue(list);
        }
    }

    public void addMyNotes(final String email) {
        DatabaseReference myNotesDb = myRef.child("Notes").child(email);
        myNotesDb.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<DataForSyncingModel>> t =
                        new GenericTypeIndicator<ArrayList<DataForSyncingModel>>() {
                        };
                ArrayList<DataForSyncingModel> value = dataSnapshot.getValue(t);
                if (value != null) {
                    ArrayList<ClassNote> list = new ArrayList<>();
                    ArrayList<IGenericItem> allNotes = db.getData();
                    for (DataForSyncingModel note : value) {
                        list.add(initializeClassNote(note, ""));
                    }
                    Iterator<ClassNote> iter = list.iterator();
                    while (iter.hasNext()) {
                        ClassNote c = iter.next();

                        for (IGenericItem n : allNotes) {
                            if (n instanceof ClassNote) {
                                if (isEqualNote(c, n)) {
                                    iter.remove();
                                    break;
                                }
                            }
                        }
                    }
                    db.insertSyncedData(list);
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .edit().putString("SyncOccured", "yes").apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ClassNote initializeClassNote(DataForSyncingModel syncedNote, String friendEmail) {
        ClassNote note = new ClassNote();
        note.setNotes(syncedNote.getNotes());
        note.setName(getContactName(context, syncedNote.getPhoneNumber()));
        note.setCallDate(syncedNote.getCallDate());
        note.setPhoneNumber(Utils.fixNumber(syncedNote.getPhoneNumber()));
        note.setSynced(0);
        note.setReminder("");
        if (syncedNote.getCategory() == null || syncedNote.getCategory().equals("null")) {
            note.setCategory("");
        } else {
            note.setCategory(syncedNote.getCategory());
        }
        int catchCall = 1;
        if (!db.getCatchCall(syncedNote.getPhoneNumber())) {
            catchCall = 0;
        }
        note.setCatchCall(catchCall);
        note.setFriendEmail(friendEmail);
        return note;
    }

    private boolean isEqualNote(IGenericItem c, IGenericItem n) {

        return ((ClassNote) c).getNotes(true).equals(((ClassNote) n).getNotes(true))
                && ((ClassNote) c).getPhoneNumber().equals(((ClassNote) n).getPhoneNumber())
                && ((ClassNote) c).getCallDate().equals(((ClassNote) n).getCallDate());
    }

    public void addMyEmail(final String userId, final String email) {
        DatabaseReference ref = myRef.child("Emails");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(userId)) {
                    DatabaseReference userNameRef = myRef.child("Emails");
                    String fixedEmail = email.replace(".", ",");
                    userNameRef.child(fixedEmail).setValue("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("eserys", databaseError.getDetails());
            }
        });
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

}
