package com.example.juseris.aftercallnote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.juseris.aftercallnote.Models.IGenericItem;
import com.example.juseris.aftercallnote.Models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by juseris on 6/14/2017.
 */

public class XmlHandling {
    private final DatabaseReference myRef;
    private final SharedPreferences prefs;
    private String key;
    private String urlName;
    Context context;
    FirebaseUser user;
    private Database db;
    private long syncedItemsCount = 0;

    public XmlHandling(Context ctx,String urlName,String key) {
        context = ctx;
        this.key = key;//context.getString(R.string.prestashop_key);
        this.urlName = urlName;//"https://www.medikos.lt";
        myRef = Utils.getDatabase().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db = new Database(context);
        if(isNetworkAvailable()) {
            new Test().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Document getXmlFileDocument(URL url) throws IOException, ParserConfigurationException, SAXException {
        HttpURLConnection urlConnection = null;
        Document doc = null;
        try {
            String username = key;
            String password = "";// leave it empty
            String authToBytes = username + ":" + password;
            //....
            String authBytesString = Base64.encodeToString(authToBytes.getBytes(), Base64.DEFAULT);// I keep it generic
            //then your code
            urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setDoOutput(false);
            urlConnection.setRequestProperty("Authorization", "Basic " + authBytesString);

            InputStream xml;// = urlConnection.getInputStream();
            int statusCode = urlConnection.getResponseCode();
            xml = urlConnection.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(xml);
        } catch (Exception e) {
            Log.e("Connection error", "connection aborted");
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return doc;
    }

    int index = 0;

    private ArrayList<Order> getOrdersData(URL url) {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            Document doc = getXmlFileDocument(url);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("order");
            int size = nList.getLength();
            boolean firstTimeDownload = prefs.getBoolean("firstTimeDownloading", true);
            int index = prefs.getInt("lastDownloadedItem", size - 1);
            if (firstTimeDownload) {
                prefs.edit().putInt("lastLatestItemToDownload", size).apply();
            }
            int lastLatestItem = prefs.getInt("lastLatestItemToDownload", size - 1);
            if (size - 1 > lastLatestItem) {
                int lastLatestItemToDwnld = 0;
                for (int temp = size - 1; temp > prefs.getInt("lastLatestItemToDownload", size - 1); temp--) {
                    Order order = getOrder(temp, nList);
                    if (order.getOrder_nr() != null) {
                        orders.add(order);
                        Log.d("Authentication success", order.getDateString());
                        db.insertNewPrestaOrder(order);
                        if (user != null) {
                            insertOrdersToFirebase(nList, order);
                        }
                    }
                }
                prefs.edit().putInt("lastLatestItemToDownload", size).apply();
            }

            for (int temp = index; temp > 0; temp--) {
                Order order = getOrder(temp, nList);
                if (order.getOrder_nr() != null) {
                    orders.add(order);
                    db.insertPrestashopOrder(order);
                    if (user != null) {
                        insertOrdersToFirebase(nList, order);
                        //lastDownloadedRef.setValue(temp+1);
                        prefs.edit().putInt("lastDownloadedItem", temp).apply();
                        if (temp > size - 2) {
                            prefs.edit().putBoolean("areOrdersDownloaded", true).apply();
                        }
                    }
                }
                prefs.edit().putBoolean("firstTimeDownloading", false).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    private Order getOrder(int temp, NodeList nList) {
        long a = System.currentTimeMillis();
        try {
            Node nNode = nList.item(temp);
            String id = nNode.getAttributes().getNamedItem("id").getNodeValue();
            String link = nNode.getAttributes().getNamedItem("xlink:href").getNodeValue();
            Document document = getXmlFileDocument(new URL(link));
            document.getDocumentElement().normalize();
            NodeList customer = document.getElementsByTagName("id_customer");
            String customerUrl = customer.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue();
            String firstName;
            String lastName;
            Log.d("customer url",customerUrl);
            try {
                Document customerDoc = getXmlFileDocument(new URL(customerUrl));
                customerDoc.getDocumentElement().normalize();

                NodeList firstNameNode = customerDoc.getElementsByTagName("firstname");
                NodeList lastNameNode = customerDoc.getElementsByTagName("lastname");

                Element firstNameElement = (Element) firstNameNode.item(0);
                Element lastNameElement = (Element) lastNameNode.item(0);

                firstName = getCharacterDataFromElement(firstNameElement);
                lastName = getCharacterDataFromElement(lastNameElement);
                Log.d("first name",firstName);
                Log.d("lastName",lastName);
            } catch (Exception e) {
                e.printStackTrace();
                firstName = "";
                lastName = "";
            }
            String orderState = getOrderState(document);
            String phoneNr = getPhoneNr(document);
            String date = getDate(document);
            return new Order(firstName, lastName, phoneNr, id, orderState, date);
        } catch (Exception e) {
            e.printStackTrace();
            return new Order();
        }
    }

    private void insertOrdersToFirebase(NodeList nList, Order order) {
        String fixedUrl = urlName.replace(".", ",").substring(8, urlName.length());
        DatabaseReference userRef = myRef.child("Prestashop").child(fixedUrl).push();
        if (nList.getLength() - 1 > syncedItemsCount) {
            userRef.setValue(order);
        }
    }

    private String getOrderState(Document document) {
        NodeList orderStateNode = document.getElementsByTagName("current_state");
        String orderStateUrl = orderStateNode.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue();
        try {
            Document orderStateDoc = getXmlFileDocument(new URL(orderStateUrl));
            orderStateDoc.getDocumentElement().normalize();

            NodeList orderNameTag = orderStateDoc.getElementsByTagName("name");
            NodeList orderLanguageTag = orderNameTag.item(0).getChildNodes();
            Element orderStateElement = (Element) orderLanguageTag.item(1);
            return getCharacterDataFromElement(orderStateElement);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getPhoneNr(Document document) {
        NodeList nodeForPhoneNr = document.getElementsByTagName("id_address_delivery");
        String phoneNrUrl = nodeForPhoneNr.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue();
        try {
            Document phoneNrDoc = getXmlFileDocument(new URL(phoneNrUrl));
            phoneNrDoc.getDocumentElement().normalize();

            NodeList phoneNrNode = phoneNrDoc.getElementsByTagName("phone_mobile");
            Element phoneNrElement = (Element) phoneNrNode.item(0);
            return Utils.fixNumber(getCharacterDataFromElement(phoneNrElement));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDate(Document document) {
        try {
            NodeList nodeForDate = document.getElementsByTagName("date_add");
            Element dateElement = (Element) nodeForDate.item(0);
            return getCharacterDataFromElement(dateElement);
        } catch (Exception e) {
            return "";
        }
    }

    private String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    class Test extends AsyncTask<Void, ArrayList<Order>, ArrayList<Order>> {

        private Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String fixedUrl = urlName.replace(".", ",").substring(8, urlName.length());
            DatabaseReference ref = myRef.child("Prestashop").child(fixedUrl);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        syncedItemsCount = dataSnapshot.getChildrenCount();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        protected ArrayList<Order> doInBackground(Void... urls) {
            URL url;
            ArrayList<Order> orders = new ArrayList<>();
            try {
                long a = System.nanoTime();
                url = new URL(urlName + "/api/orders");
                Document doc = getXmlFileDocument(url);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("order");
                System.out.println("----------------------------");
                //boolean isDownloaded = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("areOrdersDownloaded",false);
                orders = getOrdersData(url);
                long b = System.nanoTime();
                System.out.println(String.valueOf(b - a));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return orders;
        }

        protected void onPostExecute(ArrayList<Order> feed) {
            for (Order order : feed) {
                String str = String.format(Locale.CANADA, "%s  %s  %s  %s", order.getOrder_nr(), order.getName(), order.getOrder_state(), order.getSurname());
                System.out.println(str);
            }
            if (user != null) {
                String fixedUrl = urlName.replace(".", ",");
                //DatabaseReference userRef = myRef.child("Prestashop").child(fixedUrl);
                // userRef.setValue(feed);
            }
        }
    }

}
