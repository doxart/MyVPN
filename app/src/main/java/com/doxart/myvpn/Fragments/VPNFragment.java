package com.doxart.myvpn.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
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
import com.doxart.myvpn.Activities.ConnectionReportActivity;
import com.doxart.myvpn.Activities.MainActivity;
import com.doxart.myvpn.DB.ServerDB;
import com.doxart.myvpn.Interfaces.ChangeServer;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.RetroFit.GetIPDataService;
import com.doxart.myvpn.RetroFit.MyIP;
import com.doxart.myvpn.RetroFit.RetrofitClient;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.Util.VPNCountdownTimer;
import com.doxart.myvpn.databinding.FragmentVPNBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

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
import java.util.Locale;
import java.util.Objects;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VPNFragment extends Fragment implements ChangeServer {

    private static VPNFragment instance;

    FragmentVPNBinding b;
    Context context;

    private ChangeServer changeServer;
    private ServerModel server;
    private List<ServerModel> serverList;
    boolean vpnRunning = false;

    private final String TAG = "VPNApp";

    private InterstitialAd mInterstitialAd;

    private boolean premium = false;

    private SharePrefs sharePrefs;
    private MyIP myIP;
    int adDelay = 0;

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

        premium = sharePrefs.getBoolean("premium");
        adDelay = sharePrefs.getInt("delayTimeBetweenAds");

        if (!premium) buildInterstitial();

        getIPLocation();
        init();

        return b.getRoot();
    }

    private void getIPLocation() {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call = service.getMyIP();

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                myIP = response.body();

                if (myIP != null) b.myIpTxt.setText(myIP.getQuery());
            }
            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, "CHANGE WITH YOUR ADMOB INTERSTITIAL ID", adRequest,
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

    private void showInterstitial(boolean forConnect) {
        long lastAd = sharePrefs.getLastAd();

        Log.d(TAG, "showInterstitial: now: " + new Date().getTime() + " then: " + lastAd);

        if (new Date().getTime() < lastAd) {
            goConnectionReport(forConnect);
            Log.d(TAG, "showInterstitial: getTime");
            return;
        }

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d(TAG, "showInterstitial: fail");
                    goConnectionReport(forConnect);
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    sharePrefs.putLong("lastAd", new Date().getTime() + (long) adDelay * 60 * 1000);
                    goConnectionReport(forConnect);
                    buildInterstitial();
                    Log.d(TAG, "showInterstitial: dismiss");

                }
            });

            mInterstitialAd.show(requireActivity());
        } else {
            goConnectionReport(forConnect);
            Log.d(TAG, "showInterstitial: null");
            buildInterstitial();
        }

    }

    private void goConnectionReport(boolean isConnection) {
        if (server != null) {
            Intent i = new Intent(context, ConnectionReportActivity.class);
            i.putExtra("isConnection", isConnection);
            if (isConnection) {
                i.putExtra("sessionM", m);
                i.putExtra("sessionS", s);
            }
            startActivity(i);
            ConnectionReportActivity.server = server;
        }
    }

    int initTry = 0;
    private void init() {
        changeServer = this;

        serverList = new ArrayList<>();
        List<ServerModel> nativeList = ServerDB.getInstance().getServerList();

        if (nativeList != null) {
            for (ServerModel sv : nativeList) {
                if (sv.getLatency() > 0) serverList.add(sv);
            }

            makeServerList();
        } else {
            initTry++;

            if (initTry < 3) init();
            else {
                ServerDB.getInstance().getServers(context, () -> {
                    initTry = 0;
                    init();
                });
            }
        }

        b.vpnBtn.getRoot().setOnClickListener(v -> {
            if (vpnRunning) {
                stopVpn();
            } else {
                prepareVpn();
            }
        });

        b.countryLay.getRoot().setOnClickListener(v -> {
            if (vpnRunning) Toast.makeText(context, getString(R.string.vpn_is_running), Toast.LENGTH_SHORT).show();
            else MainActivity.getInstance().openServerList();

        });

        isServiceRunning();
        VpnStatus.initLogCache(context.getCacheDir());
    }

    private void makeServerList() {
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

        if (server != null) {
            sharePrefs.putServer(server);
            updateCurrentServerLay(server);
        }
    }

    public void changeServer(ServerModel index) {
        changeServer.newServer(index);
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
            b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.orange), PorterDuff.Mode.SRC_ATOP);
            b.vpnStatus.setText(getString(R.string.disconnecting));
            b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
            OpenVPNThread.stop();

            if (!premium) {
                showInterstitial(false);
            } else goConnectionReport(false);

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
        if (server != null) {
            File file = new File(context.getFilesDir().toString() + "/" + server.getOvpn());

            Log.d(TAG, "startVpn: " + server.getCountry() + " ovpn: " + server.getOvpn() + " passid: " + server.getOvpnUserPassword() + " " + server.getOvpnUserName());

            try {
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

                OpenVpnApi.startVpn(context, config.toString(), server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

                if (!premium) {
                    showInterstitial(true);
                } else goConnectionReport(true);

                b.vpnStatus.setText(getString(R.string.starting));
                vpnRunning = true;
            } catch (IOException | RemoteException e) {
                Log.d(TAG, "startVpn: " + e);
                status("connect");
            }
        }
    }

    public void setStatus(String connectionState) {
        if (connectionState!= null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("disconnected");
                    vpnRunning = false;
                    OpenVPNService.setDefaultStatus();

                    context.stopService(new Intent(context, VPNCountdownTimer.class));

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
            case "disconnected":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.disconnected));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                if (myIP != null) b.myIpTxt.setText(myIP.getQuery());
                b.myIpTxt.setTextColor(ContextCompat.getColor(context, R.color.blat));
                if (LocationFragment.getInstance() != null) LocationFragment.getInstance().getIPLocation();
                break;
            case "connecting":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.orange), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.connecting));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
                break;
            case "connected":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.green), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.connected));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
                if (myIP != null) b.myIpTxt.setText(server.getIpv4());
                b.myIpTxt.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                if (LocationFragment.getInstance() != null) LocationFragment.getInstance().getIPLocation();
                break;
            case "tryDifferentServer":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.try_different_server));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                break;
            case "loading":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.loading));
                break;
            case "invalidDevice":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.invalid_device));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                break;
            case "authenticationCheck":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.authenticating));
                b.durationTxt.setText("");
                break;
            case "nonetwork":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.disconnected));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
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

            if (Objects.requireNonNull(intent.getAction()).equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    int m = 0;
    int s = 0;
    public void updateConnectionStatus(int mm, int ss) {
        int totalSeconds = mm * 60 + ss;
        m = totalSeconds / 60;
        s = totalSeconds % 60;
        String formattedTime = String.format(Locale.getDefault(), "%02d.%02d", m, s);
        if (vpnRunning) {
            b.durationTxt.setText(formattedTime);
        }
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void updateCurrentServerLay(ServerModel m) {
        if (m != null) {
            if (m.getFlagUrl() != null) Glide.with(context).load("https://flagcdn.com/h80/" + m.getFlagUrl() + ".png").centerCrop().into(b.countryLay.cFlagImg);
            b.countryLay.cCountryTxt.setText(m.getCountry());

            Utils.setSignalView(context, b.countryLay.s1, b.countryLay.s2, b.countryLay.s3, m.getLatency());
        }
    }

    @Override
    public void newServer(ServerModel server) {
        this.server = server;
        updateCurrentServerLay(server);

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
    public void onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static VPNFragment getInstance() {
        return instance;
    }
}