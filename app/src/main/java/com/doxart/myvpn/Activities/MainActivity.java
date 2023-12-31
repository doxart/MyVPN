package com.doxart.myvpn.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.doxart.myvpn.Adapter.VPAdapter;
import com.doxart.myvpn.Fragments.LocationFragment;
import com.doxart.myvpn.Fragments.ServersFragment;
import com.doxart.myvpn.Fragments.SettingsFragment;
import com.doxart.myvpn.Fragments.SpeedTestFragment;
import com.doxart.myvpn.Fragments.VPNFragment;
import com.doxart.myvpn.Interfaces.NavItemClickListener;
import com.doxart.myvpn.Interfaces.OnAnswerListener;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.Util.ViewModelHolder;
import com.doxart.myvpn.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavItemClickListener {
    private final String TAG = "MAIN_ACTIVITY";
    private static MainActivity instance;
    ActivityMainBinding b;

    Dialog connectionDialog;

    InterstitialAd mInterstitialAd;
    SharePrefs sharePrefs;
    int adDelay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (instance == null) instance = this;
        ViewModelHolder.getInstance().createViewModels(getInstance(), this);
        setupNetworkListener();
        inflate();
    }

    private void setupNetworkListener() {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                new Handler().postDelayed(() -> {
                    if (!Utils.checkConnection(MainActivity.this)) {
                        showConnectionDialog();
                    }
                }, 1000);
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (Utils.checkConnection(MainActivity.this)) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
            } else showConnectionDialog();
        }
    });

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getData() != null) {
                if (!SharePrefs.getInstance(MainActivity.this).getBoolean("premium") &
                        o.getData().getBooleanExtra("showAD", false)) showInterstitial();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    });

    private void showConnectionDialog() {
        connectionDialog = Utils.askQuestion(this, getString(R.string.no_connection), getString(R.string.no_connection_detail), getString(R.string.go_network_settings),
                getString(R.string.try_again), getString(R.string.exit), null, false, new OnAnswerListener() {
                    @Override
                    public void onPositive() {
                        settingsLauncher.launch(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    @Override
                    public void onNegative() {
                        if (!Utils.checkConnection(MainActivity.this)) showConnectionDialog();
                    }

                    @Override
                    public void onOther() {
                        System.exit(1);
                    }
                });

        connectionDialog.show();
    }

    private void inflate() {
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        sharePrefs = new SharePrefs(this);
        adDelay = sharePrefs.getInt("delayTimeBetweenAds");

        if (!sharePrefs.getBoolean("premium")) buildInterstitial();

        if (!SharePrefs.getInstance(this).getBoolean("premium"))
            activityLauncher.launch(new Intent(this, PaywallActivity.class).putExtra("timer", 3000));
        else b.appbar.premiumBT.setVisibility(View.GONE);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        //if (SharePrefs.getInstance(this).isDynamicBackground()) setBackground();

        init();
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), o -> {});

    private void init() {
        createFragments();

        b.appbar.premiumBT.setOnClickListener(v -> activityLauncher.launch(new Intent(this, PaywallActivity.class).putExtra("timer", 0)));
        b.appbar.shareBT.setOnClickListener(v -> Utils.shareApp(this));
    }

    private void createFragments() {
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), getLifecycle());

        vpAdapter.addFragment(new ServersFragment());
        vpAdapter.addFragment(new VPNFragment());
        vpAdapter.addFragment(new LocationFragment());
        vpAdapter.addFragment(new SpeedTestFragment());
        vpAdapter.addFragment(new SettingsFragment());

        b.mainPager.setUserInputEnabled(false);
        b.mainPager.setAdapter(vpAdapter);

        b.mainPager.setCurrentItem(1, false);

        b.mainNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navVpn) {
                b.mainNav.getMenu().findItem(R.id.navVpn).setChecked(true);
                b.mainPager.setCurrentItem(1);
            } else if (item.getItemId() == R.id.navLocation) {
                b.mainNav.getMenu().findItem(R.id.navLocation).setChecked(true);
                b.mainPager.setCurrentItem(2);
                if (LocationFragment.getInstance() != null) LocationFragment.getInstance().setIPLocation();

            } else if (item.getItemId() == R.id.navSpeedTest){
                b.mainNav.getMenu().findItem(R.id.navSpeedTest).setChecked(true);
                b.mainPager.setCurrentItem(3);
            } else {
                b.mainNav.getMenu().findItem(R.id.navSettings).setChecked(true);
                b.mainPager.setCurrentItem(4);
            }
            return false;
        });
    }

    public void openServerList() {
        b.mainPager.setCurrentItem(0);
        //b.mainNav.setVisibility(View.GONE);
        b.appbar.getRoot().setVisibility(View.GONE);
    }

    public void closeServerList() {
        b.mainPager.setCurrentItem(1);
        //b.mainNav.setVisibility(View.VISIBLE);
        b.appbar.getRoot().setVisibility(View.VISIBLE);
    }

    @Override
    public void clickedItem(ServerModel index) {
        closeServerList();
        VPNFragment.getInstance().changeServer(index);
    }

    private void buildInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ADD YOUR ADMOB INTERSTITIAL ID", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitial() {
        long lastAd = sharePrefs.getLastAd();

        if (new Date().getTime() < lastAd) return;

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d(TAG, "showInterstitial: fail");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    sharePrefs.putLong("lastAd", new Date().getTime() + (long) adDelay * 60 * 1000);
                    buildInterstitial();
                    Log.d(TAG, "showInterstitial: dismiss");

                }
            });

            mInterstitialAd.show(this);
        } else {
            Log.d(TAG, "showInterstitial: null");
            buildInterstitial();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    public static MainActivity getInstance() {
        return instance;
    }
}