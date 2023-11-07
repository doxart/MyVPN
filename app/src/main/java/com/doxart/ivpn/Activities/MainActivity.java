package com.doxart.ivpn.Activities;

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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.doxart.ivpn.Adapter.VPAdapter;
import com.doxart.ivpn.Fragments.ServersFragment;
import com.doxart.ivpn.Fragments.SettingsFragment;
import com.doxart.ivpn.Fragments.VPNFragment;
import com.doxart.ivpn.Interfaces.NavItemClickListener;
import com.doxart.ivpn.Interfaces.OnAnswerListener;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.Util.Utils;
import com.doxart.ivpn.Util.ViewModelHolder;
import com.doxart.ivpn.databinding.ActivityMainBinding;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavItemClickListener {
    private static MainActivity instance;
    ActivityMainBinding b;

    WindowInsetsCompat insetsCompat;

    Dialog connectionDialog;

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

        if (!SharePrefs.getInstance(this).getBoolean("premium"))
            startActivity(new Intent(this, PaywallActivity.class));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) resultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

        adjustMargin();
        if (SharePrefs.getInstance(this).isDynamicBackground())
            setBackground();
        createFragments();
    }

    private void setBackground() {
        int[] bg = new int[] {R.drawable.main_background1, R.drawable.main_background2, R.drawable.main_background3, R.drawable.main_background4};

        b.getRoot().setBackgroundResource(bg[new Random().nextInt(bg.length-1)]);
    }

    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), o -> {});

    private void adjustMargin() {
        ViewCompat.setOnApplyWindowInsetsListener(b.getRoot(), (v, insets) -> {
            final int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            final int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            b.appbar.getRoot().setPadding(0, statusBarHeight, 0, 0);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) b.mainPager.getLayoutParams();
            params.setMargins(0,  0, 0, navigationBarHeight + pxToDp);

            b.mainNav.setLayoutParams(params);
            b.mainNav.requestLayout();

            setInsetsCompat(insets);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void createFragments() {
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), getLifecycle());

        vpAdapter.addFragment(new ServersFragment());
        vpAdapter.addFragment(new VPNFragment());
        vpAdapter.addFragment(new SettingsFragment());

        b.mainPager.setUserInputEnabled(false);
        b.mainPager.setAdapter(vpAdapter);

        b.mainPager.setCurrentItem(1, false);

        b.appbar.settingsBT.setOnClickListener(v -> openSettings());

        b.mainNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navVpn) {
                b.mainNav.getMenu().findItem(R.id.navVpn).setChecked(true);
                b.mainPager.setCurrentItem(1);
            } else if (item.getItemId() == R.id.navBrowser) {
                b.mainNav.getMenu().findItem(R.id.navBrowser).setChecked(true);
                b.mainPager.setCurrentItem(2);
            } else {
                b.mainNav.getMenu().findItem(R.id.navSettings).setChecked(true);
                b.mainPager.setCurrentItem(3);
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

    public void openSettings() {
        b.mainPager.setCurrentItem(2);
        //b.mainNav.setVisibility(View.GONE);
        b.appbar.getRoot().setVisibility(View.GONE);
    }

    public void closeSettings() {
        b.mainPager.setCurrentItem(1);
        //b.mainNav.setVisibility(View.GONE);
        b.appbar.getRoot().setVisibility(View.VISIBLE);
    }

    @Override
    public void clickedItem(int index) {
        closeServerList();
        VPNFragment.getInstance().changeServer(index);
    }

    public WindowInsetsCompat getInsetsCompat() {
        return insetsCompat;
    }

    public void setInsetsCompat(WindowInsetsCompat insetsCompat) {
        this.insetsCompat = insetsCompat;
    }

    public static MainActivity getInstance() {
        return instance;
    }
}