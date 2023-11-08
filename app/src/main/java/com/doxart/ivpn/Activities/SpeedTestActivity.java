package com.doxart.ivpn.Activities;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;

import com.doxart.ivpn.R;
import com.doxart.ivpn.databinding.ActivitySpeedTestBinding;
import com.ekn.gruzer.gaugelibrary.Range;

import java.text.DecimalFormat;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedTestActivity extends AppCompatActivity implements ISpeedTestListener {

    ActivitySpeedTestBinding b;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivitySpeedTestBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        adjustMargins();
        init();
    }

    private void adjustMargins() {
        int statusBarHeight = MainActivity.getInstance().getInsetsCompat().getInsets(WindowInsetsCompat.Type.statusBars()).top;
        int navigationBarHeight = MainActivity.getInstance().getInsetsCompat().getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        b.appbar.setPadding(0, statusBarHeight, 0, 0);
        b.relativeView.setPaddingRelative(pxToDp, pxToDp, pxToDp, navigationBarHeight + pxToDp);
    }

    private void init() {
        b.closeBT.setOnClickListener(v -> finish());

        setupGauge();

        b.startTestBT.setOnClickListener(v -> {
            b.startTestBT.setText(getString(R.string.stop));
            new Thread(this::getNetSpeed).start();
        });
    }

    private void setupGauge() {
        Range range = new Range();
        range.setColor(ContextCompat.getColor(this, R.color.red));
        range.setFrom(0d);
        range.setTo(50d);

        Range range1 = new Range();
        range1.setColor(ContextCompat.getColor(this, R.color.orange));
        range1.setFrom(50d);
        range1.setTo(100d);

        Range range2 = new Range();
        range2.setColor(ContextCompat.getColor(this, R.color.green));
        range2.setFrom(100d);
        range2.setTo(150d);

        b.speedGauge.addRange(range);
        b.speedGauge.addRange(range1);
        b.speedGauge.addRange(range2);

        b.speedGauge.setMinValue(0d);
        b.speedGauge.setMaxValue(150d);
        b.speedGauge.setValue(0d);

        b.speedGauge.setValueColor(ContextCompat.getColor(this, R.color.colorWhite));
    }

    private void getNetSpeed() {
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        startTime = System.currentTimeMillis();
        speedTestSocket.addSpeedTestListener(this);
        speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/50M.iso", 100);
    }

    @Override
    public void onCompletion(SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;
        runOnUiThread(() -> {
            b.speedGauge.setValue(Math.floor(r));

            b.speedTxt.setText(String.format("%s MB/s", new DecimalFormat("##").format(r)));
            b.latencyTxt.setText(String.format("%s ms", (System.currentTimeMillis() - startTime) / 600));
            b.startTestBT.setText(getString(R.string.start));
        });
        Log.d("AGDDGSGSGSDFSDG", "onCompletion: " + r);
    }

    @Override
    public void onProgress(float percent, SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;
        runOnUiThread(() -> b.speedGauge.setValue(Math.floor(r)));
        Log.d("AGDDGSGSGSDFSDG", "onProgress: " + r / 1000000);
    }

    @Override
    public void onError(SpeedTestError speedTestError, String errorMessage) {
        Log.d("2AGDDGSGSGSDFSDG", "onError: " + errorMessage);
    }
}