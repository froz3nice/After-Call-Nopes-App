package com.example.juseris.aftercallnote.Models;


import java.util.Date;

/**
 * Created by juseris on 6/10/2017.
 */
public class Order implements IGenericItem {
    private String dateString;
    private String name;
    private String surname;
    private String phone_nr;
    private String order_nr;
    private String order_state;

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    private Date dateObject;
    public Order(){}

    public Order(String name, String surname, String phone_nr, String order_nr, String order_state,String date) {
        this.name = name;
        this.surname = surname;
        this.phone_nr = phone_nr;
        this.order_nr = order_nr;
        this.order_state = order_state;
        this.dateString = date;
    }

    @Override
    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone_nr() {
        return phone_nr;
    }

    public void setPhone_nr(String phone_nr) {
        this.phone_nr = phone_nr;
    }

    public String getOrder_nr() {
        return order_nr;
    }

    public void setOrder_nr(String order_nr) {
        this.order_nr = order_nr;
    }

    public String getOrder_state() {
        return order_state;
    }

    public void setOrder_state(String order_state) {
        this.order_state = order_state;
    }
}
