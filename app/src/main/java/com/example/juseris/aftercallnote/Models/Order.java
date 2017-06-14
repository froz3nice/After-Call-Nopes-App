package com.example.juseris.aftercallnote.Models;


/**
 * Created by juseris on 6/10/2017.
 */
public class Order {
    String id;
    String name;
    String surname;
    String phone_nr;
    String order_nr;
    String order_state;
    public Order(){}

    public Order(String id, String name, String surname, String phone_nr, String order_nr, String order_state) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phone_nr = phone_nr;
        this.order_nr = order_nr;
        this.order_state = order_state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
