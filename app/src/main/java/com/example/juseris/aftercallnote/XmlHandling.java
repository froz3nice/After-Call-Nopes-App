package com.example.juseris.aftercallnote;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.example.juseris.aftercallnote.Models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by juseris on 6/14/2017.
 */

public class XmlHandling {
    private final FirebaseDatabase database;
    private final DatabaseReference myRef;
    Context context;
    public XmlHandling(Context ctx) {
        context = ctx;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        new Test().execute();
    }

    private Document getXmlFileDocument(URL url) throws IOException, ParserConfigurationException, SAXException {
        HttpURLConnection urlConnection = null;
        Document doc = null;
        try {

            String username = context.getString(R.string.prestashop_key);
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
        }catch (Exception e){
            Log.e("Connection error","connection aborted");
            e.printStackTrace();
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return doc;
    }


    private ArrayList<Order> getOrdersData(URL url)  {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            Document doc = getXmlFileDocument(url);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("order");
            System.out.println("----------------------------");
            System.out.println("attrib " + nList.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue());
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Order order = new Order();
                Node nNode = nList.item(temp);
                String id = nNode.getAttributes().getNamedItem("id").getNodeValue();
                String link = nNode.getAttributes().getNamedItem("xlink:href").getNodeValue();
                try {
                    Document document = getXmlFileDocument(new URL(link));
                    document.getDocumentElement().normalize();
                    NodeList customer = document.getElementsByTagName("id_customer");
                    String customerUrl = customer.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue();
                    String firstName;
                    String lastName;
                    String orderState;
                    String phoneNr;
                    try {
                        Document customerDoc = getXmlFileDocument(new URL(customerUrl));
                        customerDoc.getDocumentElement().normalize();

                        NodeList firstNameNode = customerDoc.getElementsByTagName("firstname");
                        NodeList lastNameNode = customerDoc.getElementsByTagName("lastname");

                        Element firstNameElement = (Element) firstNameNode.item(0);
                        Element lastNameElement = (Element) lastNameNode.item(0);

                        firstName = getCharacterDataFromElement(firstNameElement);
                        lastName = getCharacterDataFromElement(lastNameElement);
                    }catch(Exception e){
                        e.printStackTrace();
                        firstName = "";
                        lastName = "";
                    }
                    NodeList orderStateNode = document.getElementsByTagName("current_state");
                    String orderStateUrl = orderStateNode.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue();
                    try{
                        Document orderStateDoc = getXmlFileDocument(new URL(orderStateUrl));
                        orderStateDoc.getDocumentElement().normalize();

                        NodeList orderNameTag = orderStateDoc.getElementsByTagName("name");
                        NodeList orderLanguageTag =  orderNameTag.item(0).getChildNodes();
                        Element orderStateElement = (Element) orderLanguageTag.item(0);
                        orderState = getCharacterDataFromElement(orderStateElement);
                    }catch (Exception e){
                        e.printStackTrace();
                        orderState = "";
                    }
                    NodeList nodeForPhoneNr = document.getElementsByTagName("id_address_delivery");
                    String phoneNrUrl = nodeForPhoneNr.item(0).getAttributes().getNamedItem("xlink:href").getNodeValue();
                    try{
                        Document phoneNrDoc = getXmlFileDocument(new URL(phoneNrUrl));
                        phoneNrDoc.getDocumentElement().normalize();

                        NodeList phoneNrNode = phoneNrDoc.getElementsByTagName("phone_mobile");
                        Element phoneNrElement = (Element) phoneNrNode.item(0);
                        phoneNr = getCharacterDataFromElement(phoneNrElement);
                    }catch (Exception e){
                        e.printStackTrace();
                        phoneNr = "";
                    }
                    order.setId(id);
                    order.setName(firstName);
                    order.setSurname(lastName);
                    order.setOrder_state(orderState);
                    order.setPhone_nr(phoneNr);
                    orders.add(order);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return orders;
    }

    private String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    class Test extends AsyncTask<Void, ArrayList<Order> , ArrayList<Order>> {

        private Exception exception;

        protected ArrayList<Order> doInBackground(Void... urls) {
            URL url;
            ArrayList<Order> orders = new ArrayList<>();
            try {
                long a = System.nanoTime();
                url = new URL("http://@swims.lt/api/orders");
                Document doc = getXmlFileDocument(url);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("order");
                System.out.println("----------------------------");
                orders = getOrdersData(url);
                long b = System.nanoTime();
                System.out.println(String.valueOf(b - a));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return orders;
        }

        protected void onPostExecute(ArrayList<Order> feed) {
            for(Order order : feed) {
                String str = String.format(Locale.CANADA,"%s  %s  %s  %s",order.getId(),order.getName(),order.getOrder_state(),order.getSurname());
                System.out.println(str);
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                DatabaseReference userRef = myRef.child("Prestashop").child("swims");
                userRef.setValue(feed);
            }
        }
    }

}
