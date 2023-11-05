package com.doxart.ivpn.Util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.doxart.ivpn.BuildConfig;
import com.doxart.ivpn.DB.UsageDB;
import com.doxart.ivpn.Model.UsageModel;

public class VPNCountdownTimer extends Service {
    private Handler handler;
    private Runnable runnable;
    private boolean isServiceRunning = false;
    private int usageMinutes, usageSeconds, inUsageSeconds = 0;
    private int oneMinute;
    private UsageDB usageDB;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) oneMinute = 10;
        else oneMinute = 60;

        usageDB = new UsageDB(this);
        usageDB.open();
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if (isServiceRunning) {
                    usageSeconds++;
                    inUsageSeconds++;

                    if (inUsageSeconds >= oneMinute) {
                        usageMinutes++;
                        inUsageSeconds = 0;

                        String today = Utils.getToday();
                        UsageModel m = usageDB.getUsageDataByDate(today);

                        if (m != null) {
                            if (BuildConfig.DEBUG) usageMinutes += 59;
                            usageDB.updateUsage(today, m.getUsage() + usageMinutes);
                        }
                    }

                    Intent intent = new Intent("usage_data_updated");
                    intent.putExtra("usageMinutes", usageMinutes);
                    intent.putExtra("usageSeconds", usageSeconds);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        usageDB.close();
        handler.removeCallbacks(runnable);
    }
}
