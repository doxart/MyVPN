package com.doxart.myvpn.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.R;
import com.doxart.myvpn.databinding.FragmentSpeedTestBinding;
import com.ekn.gruzer.gaugelibrary.Range;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import java.text.DecimalFormat;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedTestFragment extends Fragment implements ISpeedTestListener {

    FragmentSpeedTestBinding b;
    Context context;

    private long startTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSpeedTestBinding.inflate(inflater, container, false);
        context = getContext();

        init();

        return b.getRoot();
    }

    private void loadAds() {
        AdLoader adLoader = new AdLoader.Builder(context, "CHANGE WITH YOUR ADMOB NATIVE ID")
                .forNativeAd(nativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();
                    TemplateView template = b.myTemplate;
                    template.setStyles(styles);
                    template.setNativeAd(nativeAd);
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void init() {
        setupGauge();

        if (!SharePrefs.getInstance(context).getBoolean("premium")) {
            if (SharePrefs.getInstance(context).getBoolean("showBannerAds")) loadAds();
            else b.myTemplate.setVisibility(View.GONE);
        } else b.myTemplate.setVisibility(View.GONE);

        b.startTestBT.setOnClickListener(v -> {
            b.startTestBT.setVisibility(View.GONE);
            new Thread(this::getNetSpeed).start();
        });
    }

    private void setupGauge() {
        Range range = new Range();
        range.setColor(ContextCompat.getColor(context, R.color.red));
        range.setFrom(0d);
        range.setTo(50d);

        Range range1 = new Range();
        range1.setColor(ContextCompat.getColor(context, R.color.orange));
        range1.setFrom(50d);
        range1.setTo(100d);

        Range range2 = new Range();
        range2.setColor(ContextCompat.getColor(context, R.color.green));
        range2.setFrom(100d);
        range2.setTo(150d);

        b.speedGauge.addRange(range);
        b.speedGauge.addRange(range1);
        b.speedGauge.addRange(range2);

        b.speedGauge.setMinValue(0d);
        b.speedGauge.setMaxValue(150d);
        b.speedGauge.setValue(0d);

        b.speedGauge.setValueColor(ContextCompat.getColor(context, android.R.color.transparent));
    }

    int test = 0;
    private void getNetSpeed() {
        test = 99;
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        b.speedGauge.setValueColor(ContextCompat.getColor(context, R.color.colorWhite));

        startTime = System.currentTimeMillis();
        speedTestSocket.addSpeedTestListener(this);
        speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/50M.iso", 100);
    }

    @Override
    public void onCompletion(SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;
        getActivity().runOnUiThread(() -> {
            b.speedGauge.setValue(Math.floor(r));

            b.startTestBT.setVisibility(View.VISIBLE);
            b.speedGauge.setValueColor(ContextCompat.getColor(context, android.R.color.transparent));

            b.speedTxt.setText(String.format("%s MB/s", new DecimalFormat("##").format(r)));
            b.latencyTxt.setText(String.format("%s ms", (System.currentTimeMillis() - startTime) / 600));
            b.startTestBT.setText(getString(R.string.start));
        });
        Log.d("AGDDGSGSGSDFSDG", "onCompletion: " + r);
    }

    @Override
    public void onProgress(float percent, SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;
        getActivity().runOnUiThread(() -> b.speedGauge.setValue(Math.floor(r)));
        Log.d("AGDDGSGSGSDFSDG", "onProgress: " + r / 1000000);
    }

    @Override
    public void onError(SpeedTestError speedTestError, String errorMessage) {
        Log.d("2AGDDGSGSGSDFSDG", "onError: " + errorMessage);
    }
}