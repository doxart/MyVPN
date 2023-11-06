package com.doxart.ivpn.Util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.doxart.ivpn.BuildConfig;
import com.doxart.ivpn.DB.Usage;

public class VPNCountdownTimer extends Service {
    private Handler handler;
    private Runnable runnable;
    private boolean isServiceRunning = false;
    private int usageMinutes, usageSeconds, inUsageSeconds = 0;
    private int oneMinute;

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

                        new Thread(() -> insertToDB(today, usageMinutes)).start();
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

    private void insertToDB(String today, int usageMinutes) {
        if (BuildConfig.DEBUG) usageMinutes += 59;
        Usage usage = ViewModelHolder.getInstance().getUsageViewModel().getUsageRepository().getUsage(today);

        if (usage == null) {
            usage = new Usage();
            usage.setDate(today);
            usage.setDateTime(System.currentTimeMillis());
            usage.setUsageInMinutes(usageMinutes);
        } else usage.setUsageInMinutes(usage.getUsageInMinutes() + usageMinutes);

        Log.d("ADGADGASFASDASD", "run: " + usage.getUsageInMinutes());

        ViewModelHolder.getInstance().getUsageViewModel().insertUsage(usage);
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
        handler.removeCallbacks(runnable);
    }
}
