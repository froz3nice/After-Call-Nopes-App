package com.example.juseris.aftercallnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.example.juseris.aftercallnote.UtilsPackage.Utils;

public class PhoneCallReceiver extends BroadcastReceiver  {

    private Listener mListener = null;


    // interface declaration
    public interface Listener {
        void onCallStateChanged(Context context, int state, String nr);
    }

    // registration of listener
    public void registerListener (Listener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            String nr = Utils.fixNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("LastActiveNr", nr).apply();
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int state = 0;
            String nr = "";
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
                nr = Utils.fixNumber(intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER));
            }
           /* Intent i = new Intent(context, ReceiverHelperService.class);
            if(!isMyServiceRunning(ReceiverHelperService.class,context)) {
                context.startService(i);
            }*/
            registerListener(new ReceiverHelperService());
            mListener.onCallStateChanged(context,state,nr);
        }
    }


}

