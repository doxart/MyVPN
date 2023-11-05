package com.doxart.ivpn.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.doxart.ivpn.Activities.MainActivity;
import com.doxart.ivpn.Adapter.UsageAdapter;
import com.doxart.ivpn.DB.UsageDB;
import com.doxart.ivpn.Model.UsageModel;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.databinding.FragmentSettingsBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SETTINGS";
    FragmentSettingsBinding b;
    Context context;

    SharePrefs sharePrefs;

    UsageDB usageDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSettingsBinding.inflate(inflater, container, false);
        context = getContext();

        sharePrefs = new SharePrefs(context);
        usageDB = new UsageDB(context);
        usageDB.open();

        adjustMargin();
        getUsage();
        init();

        return b.getRoot();
    }

    private void adjustMargin() {
        WindowInsetsCompat insets = MainActivity.getInstance().getInsetsCompat();
        if (insets != null) {
            final int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            final int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            b.appbar.setPaddingRelative(0, statusBarHeight + pxToDp, 0, 0);

            b.scrollView.setPaddingRelative(0, 0, 0, navigationBarHeight + pxToDp);
        }
    }

    private void getUsage() {
        List<UsageModel> usageList = usageDB.getAllUsageData();

        UsageAdapter adapter = new UsageAdapter(context, usageList);

        b.usageRecycler.setHasFixedSize(true);
        b.usageRecycler.setAdapter(adapter);
    }

    private void init() {
        b.closeBT.setOnClickListener(v -> MainActivity.getInstance().closeSettings());

        b.dynamicBgSwitch.setChecked(sharePrefs.isDynamicBackground());
        b.dynamicBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> sharePrefs.setDynamicBackground(isChecked));

        b.autoConnectSwitch.setChecked(sharePrefs.isAutoConnect());
        b.autoConnectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> sharePrefs.setAutoConnect(isChecked));
    }
}