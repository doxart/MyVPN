package com.doxart.ivpn.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.Utils;
import com.doxart.ivpn.databinding.ActivityConnectionReportBinding;
import com.google.android.gms.ads.AdRequest;

public class ConnectionReportActivity extends AppCompatActivity {

    ActivityConnectionReportBinding b;
    public static ServerModel server;
    private boolean isConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivityConnectionReportBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        adjustMargins();
        setupView();
        init();
    }

    private void adjustMargins() {
        int statusBarHeight = MainActivity.getInstance().getInsetsCompat().getInsets(WindowInsetsCompat.Type.statusBars()).top;
        int navigationBarHeight = MainActivity.getInstance().getInsetsCompat().getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        b.appbar.setPadding(0, statusBarHeight, 0, 0);
    }

    private void setupView() {
        if (server != null) {
            isConnection = getIntent().getBooleanExtra("isConnection", true);

            if (isConnection) b.connectionTypeTxt.setText(getString(R.string.connection_successful));
            else {
                b.connectionTypeTxt.setText(getString(R.string.connection_report));
                updateConnectionStatus(getIntent().getIntExtra("sessionM", 0), getIntent().getIntExtra("sessionS", 0));
            }

            Glide.with(this).load("https://flagcdn.com/h80/" + server.getFlagUrl() + ".png").centerCrop().into(b.serverFlag);
            b.serverIp.setText(server.getIpv4());
            b.serverName.setText(server.getCountry());
        } else finish();
    }

    private void init() {
        b.adView.loadAd(new AdRequest.Builder().build());
        b.closeBT.setOnClickListener(v -> finish());

        b.shareBT.setOnClickListener(v -> Utils.shareApp(this));
        b.ratingView.setOnClickListener(v -> Utils.openAppInPlayStore(this));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    public void updateConnectionStatus(int m, int s) {
        int totalSeconds = m * 60 + s;
        String formattedTime = String.format("%02d s", totalSeconds % 60);
        b.serverTime.setText(formattedTime);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("usage_data_updated"));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}