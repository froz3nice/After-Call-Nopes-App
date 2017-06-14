package com.example.juseris.aftercallnote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CachedFileProvider extends ContentProvider {

    private static final String CLASS_NAME = "CachedFileProvider";

    // The authority is the symbolic name for the provider class
    public static final String AUTHORITY = "com.stephendnicholas.gmailattach.provider";

    // UriMatcher used to match against incoming requests
    private UriMatcher uriMatcher;
    private static boolean hasCSV = false;
    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        hasCSV = false;
        // Add a URI to the matcher which will match against the form
        // 'content://com.stephendnicholas.gmailattach.provider/*'
        // and return 1 in the case that the incoming Uri matches this pattern
        uriMatcher.addURI(AUTHORITY, "*", 1);

        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        String LOG_TAG = CLASS_NAME + " - openFile";

        // Check incoming Uri against the matcher
        switch (uriMatcher.match(uri)) {

            // If it returns 1 - then it matches the Uri defined in onCreate
            case 1:

                // The desired file name is specified by the last segment of the
                // path
                // E.g.
                // 'content://com.stephendnicholas.gmailattach.provider/Test.txt'
                // Take this and build the path to the file
                String fileLocation = getContext().getCacheDir() + File.separator
                        + uri.getLastPathSegment();

                // Create & return a ParcelFileDescriptor pointing to the file
                // Note: I don't care what mode they ask for - they're only getting
                // read only
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(
                        fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
                return pfd;

            // Otherwise unrecognised Uri
            default:
                Log.v(LOG_TAG, "Unsupported uri: '" + uri + "'.");
                throw new FileNotFoundException("Unsupported uri: "
                        + uri.toString());
        }
    }

    // //////////////////////////////////////////////////////////////
    // Not supported / used / required for this example
    // //////////////////////////////////////////////////////////////

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s,
                      String[] as) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String s, String[] as1,
                        String s1) {
        return null;
    }

    public static void createCachedFile(Context context,String fileName1, String fileName2,
                                        String content,String content2) throws IOException {
        if(!fileName2.equals("")) {
            File cacheFile = new File(context.getCacheDir() + File.separator
                    + fileName1);
            cacheFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(cacheFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
            PrintWriter pw = new PrintWriter(osw);

            pw.println(content);

            //pw.flush();
            pw.close();

            File cacheFile2 = new File(context.getCacheDir() + File.separator
                    + fileName2);
            cacheFile2.createNewFile();

            FileOutputStream fos2 = new FileOutputStream(cacheFile2);
            OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "UTF8");
            PrintWriter pw2 = new PrintWriter(osw2);

            pw2.println(content2);

            //pw.flush();
            pw2.close();
        }else{
            File cacheFile = new File(context.getCacheDir() + File.separator
                    + fileName1);
            cacheFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(cacheFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
            PrintWriter pw = new PrintWriter(osw);

            pw.println(content);

            //pw.flush();
            pw.close();
        }
        hasCSV = true;
    }

    public Intent getMultipleAttachmentIntent(String name,String name2){
        //File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), name);
        //Uri path = Uri.fromFile(filelocation);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if(user != null) {
            email = user.getEmail();
        }
        String[] TO = {email};
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        //Explicitly only use Gmail to send
        //emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

        //emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Notes And incoming calls");
        ArrayList<Uri> uris = new ArrayList<Uri>();
        String[] filePaths = new String[]{"content://" + CachedFileProvider.AUTHORITY + "/" + name
                                         ,"content://" + CachedFileProvider.AUTHORITY + "/" + name2};
        for (String file : filePaths)
        {
            uris.add(Uri.parse(file));
        }

        if(hasCSV) {
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        return emailIntent;

    }


    public Intent getSendEmailIntent(String allLines,String name) {
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), name);
        Uri path = Uri.fromFile(filelocation);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if(user != null) {
            email = user.getEmail();
        }
        String[] TO = {email};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        //Explicitly only use Gmail to send
        //emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

        //emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        //emailIntent.putExtra(Intent.EXTRA_CC, CC);
        if(name.equals("call_data.csv")) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Notes");
        }else{
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "All incoming calls");
        }
        emailIntent.putExtra(Intent.EXTRA_TEXT, allLines);
        if(hasCSV) {
            emailIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    Uri.parse("content://" + CachedFileProvider.AUTHORITY + "/"
                            + name));

        }
        return emailIntent;
    }
}