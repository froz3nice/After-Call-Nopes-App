package com.example.juseris.aftercallnote.Models;

/**
 * Created by juseris on 9/5/2017.
 */

public class CategoriesAndColors {
    private String category;
    private String color;

    public CategoriesAndColors(String category, String color) {
        this.category = category;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
