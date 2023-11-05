package com.doxart.ivpn.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.doxart.ivpn.Activities.MainActivity;
import com.doxart.ivpn.DB.ServerDB;
import com.doxart.ivpn.DB.UsageDB;
import com.doxart.ivpn.Interfaces.ChangeServer;
import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.Util.Utils;
import com.doxart.ivpn.Util.VPNCountdownTimer;
import com.doxart.ivpn.databinding.FragmentVPNBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

public class VPNFragment extends Fragment implements ChangeServer {

    private static VPNFragment instance;

    FragmentVPNBinding b;
    Context context;

    private ChangeServer changeServer;
    private ServerModel server;
    private List<ServerModel> serverList;
    boolean vpnRunning = false;

    private RewardedAd rewardedAd;

    private final String TAG = "VPNApp";

    private InterstitialAd mInterstitialAd;

    private int showTry = 0;
    private int adMode = 0;

    private boolean premium = false;

    private SharePrefs sharePrefs;

    private UsageDB usageDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (instance == null) instance = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentVPNBinding.inflate(inflater, container, false);
        context = getContext();

        sharePrefs = new SharePrefs(context);
        usageDB = new UsageDB(context);
        usageDB.open();

        adMode = sharePrefs.getInt("vpnButtonAdMode");
        premium = sharePrefs.getBoolean("premium");

        switch (adMode) {
            case 0:
                buildRewarded();
                break;
            case 1:
                buildInterstitial();
                break;
        }

        init();

        return b.getRoot();
    }

    private void buildRewarded() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, getString(R.string.rewarded_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                        Log.d(TAG, "onAdFailedToLoad: " + loadAdError);
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }
                });
    }

    private void buildInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, getString(R.string.interstitial_id), adRequest,
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
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    showTry++;
                    if (showTry < 3) showInterstitial();
                    else {
                        if (vpnRunning) {
                            stopVpn();
                        } else {
                            prepareVpn();
                        }
                    }
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    long m = (long) sharePrefs.getInt("delayTimeBetweenAds") * 1000 * 60;
                    sharePrefs.putLong("lastAd", new Date().getTime() + m);
                    if (vpnRunning) {
                        stopVpn();
                    } else {
                        prepareVpn();
                    }
                }
            });

            mInterstitialAd.show(requireActivity());
        } else {
            showTry++;
            if (showTry < 3) showInterstitial();
            else {
                if (vpnRunning) {
                    stopVpn();
                } else {
                    prepareVpn();
                }
            }
        }
    }

    private void showAd() {
        long lastAd = sharePrefs.getLastAd();
        Log.d(TAG, "showAd: " + lastAd);

        if (new Date().getTime() >= lastAd) {
            if (rewardedAd != null) {
                rewardedAd.show(requireActivity(), rewardItem -> {
                    if (vpnRunning) {
                        stopVpn();
                    } else {
                        prepareVpn();
                    }
                    long m = (long) sharePrefs.getInt("delayTimeBetweenAds") * 1000 * 60;
                    sharePrefs.putLong("lastAd", new Date().getTime() + m);
                    buildRewarded();
                });
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
                if (vpnRunning) {
                    stopVpn();
                } else {
                    prepareVpn();
                }
                buildRewarded();
            }
        } else {
            if (vpnRunning) {
                stopVpn();
            } else {
                prepareVpn();
            }
        }
    }

    private void init() {
        changeServer = this;

        serverList = new ArrayList<>();
        List<ServerModel> nativeList = ServerDB.getInstance().getServerList();

        for (ServerModel sv : nativeList) {
            if (sv.getLatency() > 0) serverList.add(sv);
        }

        serverList.sort(Comparator.comparing(ServerModel::getLatency));

        if (serverList != null) {
            if (serverList.size() > 0) {
                if (!sharePrefs.getBoolean("premium")) {
                    for (ServerModel sv : serverList) {
                        if (!sv.isPremium()) {
                            server = sv;
                            break;
                        }
                    }
                } else server = serverList.get(0);
            }
        } else {
            return;
        }

        updateCurrentServerLay(server, true);

        if (!premium) {
            if (sharePrefs.getBoolean("showBannerAds")) b.adView.loadAd(new AdRequest.Builder().build());
        }

        b.vpnBtn.getRoot().setOnClickListener(v -> {
            if (premium) {
                if (vpnRunning) {
                    stopVpn();
                } else {
                    prepareVpn();
                }
            } else {
                switch (adMode){
                    case 0:
                        showAd();
                        break;
                    case 1:
                        showInterstitial();
                        break;
                }
            }
        });

        b.countryLay.getRoot().setOnClickListener(v -> {
            if (vpnRunning) Toast.makeText(context, getString(R.string.vpn_is_running), Toast.LENGTH_SHORT).show();
            else MainActivity.getInstance().openServerList();

        });

        isServiceRunning();
        VpnStatus.initLogCache(context.getCacheDir());
    }

    public void changeServer(int index) {
        changeServer.newServer(serverList.get(index));
    }

    private void prepareVpn() {
        if (!vpnRunning) {
            if (getInternetStatus()) {
                Intent intent = VpnService.prepare(context);

                if (intent != null) resultLauncher.launch(intent);
                else startVpn();

                status("connecting");
            } else showToast("You have no internet connection!!");
        } else if (stopVpn()) showToast("Successfully disconnected.");
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) startVpn();
        else showToast("Permission Deny!!");
    });

    public boolean stopVpn() {
        try {
            OpenVPNThread.stop();
            context.stopService(new Intent(context, VPNCountdownTimer.class));

            status("connect");

            vpnRunning = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean getInternetStatus() {
        return Utils.checkConnection(context);
    }

    public void isServiceRunning() {
        setStatus(OpenVPNService.getStatus());
    }

    private void startVpn() {
        File file = new File(context.getFilesDir().toString() + "/" + server.getOvpn());

        try {
            //InputStream conf = context.getAssets().open(server.getOvpn());
            InputStream conf;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                conf = Files.newInputStream(file.toPath());
            } else conf = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder config = new StringBuilder();
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config.append(line).append("\n");
            }

            br.readLine();

            String today = Utils.getToday();
            Log.d(TAG, "startVpn: " + today);

            if (!usageDB.isExist(today))
                usageDB.insertUsage(today, System.currentTimeMillis(), 0);

            OpenVpnApi.startVpn(context, config.toString(), server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());


            b.vpnStatus.setText(getString(R.string.starting));
            vpnRunning = true;
        } catch (IOException | RemoteException e) {
            Log.d(TAG, "startVpn: " + e);
            status("connect");
        }
    }

    public void setStatus(String connectionState) {
        if (connectionState!= null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("connect");
                    vpnRunning = false;
                    OpenVPNService.setDefaultStatus();

                    break;
                case "CONNECTED":
                    vpnRunning = true;
                    status("connected");
                    context.startService(new Intent(context, VPNCountdownTimer.class));
                    break;
                case "WAIT":
                    status("connecting");
                    break;
                case "AUTH":
                    status("auth");
                    break;
                case "RECONNECTING":
                    status("RECONNECTING");

                    break;
                case "NONETWORK":
                    status("nonetwork");
                    vpnRunning = false;
                    break;
            }

    }

    public void status(String status) {
        switch (status) {
            case "connect":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
                b.vpnStatus.setText(getString(R.string.disconnected));
                b.durationTxt.setText(getString(R.string.tap_to_connect));
                break;
            case "connecting":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.TOP);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorWhite)));
                b.vpnStatus.setText(getString(R.string.connecting));
                break;
            case "connected":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.TOP);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorWhite)));
                b.vpnStatus.setText(getString(R.string.connected));
                break;
            case "tryDifferentServer":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
                b.vpnStatus.setText(getString(R.string.try_different_server));
                b.durationTxt.setText(getString(R.string.tap_to_connect));
                break;
            case "loading":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.TOP);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorWhite)));
                b.vpnStatus.setText(getString(R.string.loading));
                break;
            case "invalidDevice":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
                b.vpnStatus.setText(getString(R.string.invalid_device));
                b.durationTxt.setText(getString(R.string.tap_to_connect));
                break;
            case "authenticationCheck":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.TOP);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorWhite)));
                b.vpnStatus.setText(getString(R.string.authenticating));
                b.durationTxt.setText(getString(R.string.tap_to_connect));
                break;
            case "nonetwork":
                b.vpnBtn.switchLay.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                b.vpnBtn.switchBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                b.vpnBtn.switchBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
                b.vpnStatus.setText(getString(R.string.disconnected));
                b.durationTxt.setText(getString(R.string.tap_to_connect));
                break;
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                double byteIn = intent.getDoubleExtra("byteIn", 0);
                double byteOut = intent.getDoubleExtra("byteOut", 0);

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (intent.getAction().equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    public void updateConnectionStatus(String duration, String lastPacketReceive, double byteIn, double byteOut) {
        //if (vpnRunning) b.durationTxt.setText(duration);
    }

    public void updateConnectionStatus(int m, int s) {
        int totalSeconds = m * 60 + s;
        String formattedTime = String.format("%02d.%02d", totalSeconds / 60, totalSeconds % 60);
        if (vpnRunning) b.durationTxt.setText(formattedTime);
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void updateCurrentServerLay(ServerModel m, boolean auto) {
        Glide.with(context).load("https://flagcdn.com/h80/" + m.getFlagUrl() + ".png").centerCrop().into(b.countryLay.cFlagImg);
        if (auto) b.countryLay.cMsTxt.setText(String.format(getString(R.string.auto_ms_format), m.getLatency() + "ms"));
        else b.countryLay.cMsTxt.setText(String.format("%sms", m.getLatency()));
        b.countryLay.cCountryTxt.setText(m.getCountry());

        Utils.setSignalView(context, b.countryLay.s1, b.countryLay.s2, b.countryLay.s3, m.getLatency());
    }

    @Override
    public void newServer(ServerModel server) {
        this.server = server;
        updateCurrentServerLay(server, false);

        if (vpnRunning) {
            stopVpn();
        }

        prepareVpn();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter("usage_data_updated"));
        if (sharePrefs != null) premium = sharePrefs.getBoolean("premium");
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static VPNFragment getInstance() {
        return instance;
    }
}