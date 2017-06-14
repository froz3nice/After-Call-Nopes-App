package com.example.juseris.aftercallnote.Models;

import android.content.Context;
import android.content.SharedPreferences;

public class ClassSettings {
    private Context context;

    private SharedPreferences Settings;
    private SharedPreferences.Editor SettingsEditor;

    private final static String SettingsData = "NOTES";

    private final String CallNumber = "CALL_NUMBER";
    private final String CallTime = "CALL_TIME";
    private final String CallDate = "CALL_DATE";
    private final String Name = "call_name";
    private final String RecentNumber = "RecentNR";
    private final String Numbers = "NUMBERS";
    private final String ActiveCatchCall = "ACTIVE_CATCH_CALL";

    public boolean getIsNumberChecked(String nr) {
        return Settings.getBoolean(nr,true);
    }
    public void setIsNumberChecked(String nr,boolean isChecked){
        SettingsEditor.putBoolean(nr, isChecked);
        SettingsEditor.apply();
    }
    private final String isNumberChecked = "isNumberChecked";

    public ClassSettings(Context context) {
        this.context = context;

        Settings = context.getApplicationContext().getSharedPreferences(SettingsData, 0);
        SettingsEditor = Settings.edit();

    }

    public String getRecentNumber() {
        return Settings.getString(RecentNumber, "");
    }

    public void setRecentNumber(String number) {
        SettingsEditor.putString(RecentNumber, number);
        SettingsEditor.apply();
    }

    public String getNumbers() {
        return Settings.getString(Numbers, "");
    }

    public void setNumbers(String numbers) {
        SettingsEditor.putString(Numbers, numbers);
        SettingsEditor.apply();
    }

    public void setCatchCall(Boolean _Catch) {
        SettingsEditor.putBoolean(ActiveCatchCall, _Catch);
        SettingsEditor.apply();
    }

    public Boolean getCatchCall() {
        return Settings.getBoolean(ActiveCatchCall, false);
    }

    public void setIncomingNumber(String _Number) {
        SettingsEditor.putString(CallNumber, _Number);
        SettingsEditor.apply();
    }

    public String getIncomingNumber() {
        return Settings.getString(CallNumber, "");
    }

    public void setDate(String _Date) {
        SettingsEditor.putString(CallDate, _Date);
        SettingsEditor.apply();
    }

    public void setName(String name) {
        SettingsEditor.putString(Name, name);
        SettingsEditor.apply();
    }

    public String getName() {
        return Settings.getString(Name, "");
    }

    public String GetIncomingDate() {
        return Settings.getString(CallDate, "");
    }

    public void setCallTime(String _Time) {
        SettingsEditor.putString(CallTime, _Time);
        SettingsEditor.apply();
    }

    public String getCallTime() {
        return Settings.getString(CallTime, "");
    }

    public void setNulls() {
        SettingsEditor.putString(CallNumber, "");
        SettingsEditor.putString(CallTime, "");
        SettingsEditor.putString(CallDate, "");
        SettingsEditor.apply();
    }
}
