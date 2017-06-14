package com.example.juseris.aftercallnote;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by juseris on 6/11/2017.
 */

public class MyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new CountDownTimer(100000,4000)
        {
            @Override
            public void onTick(long millisUntilFinished) {
                sendBroadcast(new Intent("fromservice"));
            }

            @Override
            public void onFinish() {

            }
        }.start();
        return START_STICKY;
    }
}
