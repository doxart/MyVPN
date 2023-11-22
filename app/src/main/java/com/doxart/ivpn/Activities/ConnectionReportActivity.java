package com.doxart.ivpn.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.Util.Utils;
import com.doxart.ivpn.databinding.ActivityConnectionReportBinding;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import java.util.Locale;

public class ConnectionReportActivity extends AppCompatActivity {

    ActivityConnectionReportBinding b;
    public static ServerModel server;
    private Dialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivityConnectionReportBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        loadAds();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        setupView();
        init();
    }

    private void setupView() {
        if (server != null) {
            boolean isConnection = getIntent().getBooleanExtra("isConnection", true);

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

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        SharePrefs sharePrefs = new SharePrefs(this);

        boolean premium = sharePrefs.getBoolean("premium");

        if (!premium) {
            if (sharePrefs.getBoolean("showBannerAds")) loadAds();
            else b.myTemplate.setVisibility(View.GONE);
        } else b.myTemplate.setVisibility(View.GONE);

        b.closeBT.setOnClickListener(v -> finish());

        b.shareBT.setOnClickListener(v -> Utils.shareApp(this));
        b.ratingView.setOnTouchListener((v, event) -> {
            Utils.openAppInPlayStore(ConnectionReportActivity.this);
            return false;
        });
    }


    private void loadAds() {
        b.myTemplate.setVisibility(View.GONE);
        AdLoader adLoader = new AdLoader.Builder(this, getString(R.string.native_id))
                .forNativeAd(nativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();
                    TemplateView template = findViewById(R.id.my_template);
                    template.setStyles(styles);
                    template.setNativeAd(nativeAd);
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
        b.myTemplate.setVisibility(View.VISIBLE);
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
        String formattedTime = String.format(Locale.getDefault(), "%02d s", totalSeconds % 60);
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