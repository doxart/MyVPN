package com.doxart.ivpn.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.doxart.ivpn.Activities.MainActivity;
import com.doxart.ivpn.Activities.PaywallActivity;
import com.doxart.ivpn.Adapter.UsageAdapter;
import com.doxart.ivpn.BootCompleteReceiver;
import com.doxart.ivpn.DB.Usage;
import com.doxart.ivpn.DB.UsageViewModel;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.databinding.FragmentSettingsBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SETTINGS";
    FragmentSettingsBinding b;
    Context context;

    SharePrefs sharePrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSettingsBinding.inflate(inflater, container, false);
        context = getContext();

        sharePrefs = new SharePrefs(context);

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
        UsageAdapter adapter = new UsageAdapter(context, () -> b.usageRecycler.smoothScrollToPosition(Gravity.END));

        b.usageRecycler.setHasFixedSize(true);
        b.usageRecycler.setAdapter(adapter);

        b.usageRecycler.smoothScrollToPosition(Gravity.END);

        UsageViewModel usageViewModel = new ViewModelProvider(requireActivity()).get(UsageViewModel.class);
        LiveData<List<Usage>> allUsages = usageViewModel.getAllUsages();

        allUsages.observe(requireActivity(), usages -> {
            adapter.setUsageList(usages);

            if (usages.isEmpty()) {
                b.noStatistics.setVisibility(View.VISIBLE);
                b.usageRecycler.setVisibility(View.GONE);
            }
            else {
                b.noStatistics.setVisibility(View.GONE);
                b.usageRecycler.setVisibility(View.VISIBLE);
            }

            b.usageRecycler.smoothScrollToPosition(Gravity.END);
        });
    }

    private void init() {
        b.closeBT.setOnClickListener(v -> MainActivity.getInstance().closeSettings());

        b.dynamicBgSwitch.setChecked(sharePrefs.isDynamicBackground());
        b.dynamicBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> sharePrefs.setDynamicBackground(isChecked));

        b.autoConnectSwitch.setChecked(sharePrefs.isAutoConnect());
        b.autoConnectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (sharePrefs.getBoolean("premium")) {
                sharePrefs.setAutoConnect(isChecked);
                setAutoConnect(isChecked);
            } else {
                buttonView.setChecked(sharePrefs.isAutoConnect());
                startActivity(new Intent(context, PaywallActivity.class).putExtra("timer", 0));
            }
        });
    }

    private void setAutoConnect(boolean on) {
        int flag = (on ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

        ComponentName comp = new ComponentName(context, BootCompleteReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(comp, flag, PackageManager.DONT_KILL_APP);
    }
}