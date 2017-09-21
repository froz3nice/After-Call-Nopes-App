package com.example.juseris.aftercallnote.Models;

import java.util.Date;

/**
 * Created by juseris on 9/7/2017.
 */

public interface IGenericItem  {
    void setDateObject(Date date);
    Date getDateObject();
    String getDateString();
}
