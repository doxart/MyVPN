package com.doxart.ivpn.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.adapty.Adapty;
import com.adapty.errors.AdaptyError;
import com.adapty.models.AdaptyProfile;
import com.adapty.utils.AdaptyResult;
import com.doxart.ivpn.DB.ServerDB;
import com.doxart.ivpn.Interfaces.OnAnswerListener;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.PaywallViewUtils;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.Util.Utils;
import com.doxart.ivpn.databinding.ActivityLauncherBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LauncherActivity extends AppCompatActivity {

    ActivityLauncherBinding b;

    SharePrefs sharePrefs;

    private final String TAG = "LAUNCHER_PROCESS";
    private ExecutorService executorService;
    private Dialog connectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (Utils.checkConnection(LauncherActivity.this)) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
                inflate();
            } else showConnectionDialog();
        }
    });

    private void showConnectionDialog() {
        connectionDialog = Utils.askQuestion(this, getString(R.string.no_connection), getString(R.string.no_connection_detail), getString(R.string.go_network_settings),
                getString(R.string.try_again), getString(R.string.exit), null, false, new OnAnswerListener() {
                    @Override
                    public void onPositive() {
                        resultLauncher.launch(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    @Override
                    public void onNegative() {
                        inflate();
                    }

                    @Override
                    public void onOther() {
                        System.exit(1);
                    }
                });

        connectionDialog.show();
    }

    private void inflate() {
        b = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (!Utils.checkConnection(this)) {
            showConnectionDialog();
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        sharePrefs = new SharePrefs(this);

        b.adjustingTxt.setText(getString(R.string.launch_phase_0));

        executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        b.adjustingTxt.setText(getString(R.string.launch_phase_1));

        executorService.execute(() -> {
            PaywallViewUtils.getInstance().getPaywallView("tpid");

            handler.post(() -> PaywallViewUtils.getInstance().setPaywallLoadFinishListener(new PaywallViewUtils.OnPaywallLoadFinishListener() {
                @Override
                public void onFinish() {
                    getConfig();
                }

                @Override
                public void onError() {
                    getConfig();
                }
            }));
        });
    }

    private void getConfig() {
        b.adjustingTxt.setText(getString(R.string.launch_phase_2));
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sharePrefs.putBoolean("showPaywallCloseAd", remoteConfig.getBoolean("showPaywallCloseAd"));
                sharePrefs.putBoolean("showBannerAds", remoteConfig.getBoolean("showBannerAds"));
                sharePrefs.putInt("vpnButtonAdMode", ((Long)remoteConfig.getLong("vpnButtonAdMode")).intValue());
            }

            b.adjustingTxt.setText(getString(R.string.launch_phase_3));
            ServerDB.getInstance().getServers(this, this::getStatus);
        });
    }

    private void getStatus() {
        b.adjustingTxt.setText(getString(R.string.launch_phase_4));
        Adapty.getProfile(result -> {
            if (result instanceof AdaptyResult.Success) {
                AdaptyProfile profile = ((AdaptyResult.Success<AdaptyProfile>) result).getValue();

                AdaptyProfile.AccessLevel premium = profile.getAccessLevels().get("premium");

                sharePrefs.putBoolean("premium", premium != null && premium.isActive());
            } else if (result instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result).getError();
                Log.d(TAG, "getStatus: " + error);
            }

            b.adjustingTxt.setText(getString(R.string.launch_phase_5));
            init();
        });
    }

    private void init() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        executorService.shutdown();

        finish();
    }
}