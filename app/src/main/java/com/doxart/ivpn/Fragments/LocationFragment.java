package com.doxart.ivpn.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.doxart.ivpn.R;
import com.doxart.ivpn.RetroFit.GetIPDataService;
import com.doxart.ivpn.RetroFit.MyIP;
import com.doxart.ivpn.RetroFit.RetrofitClient;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.databinding.FragmentLocationBinding;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationFragment extends Fragment {
    private static LocationFragment instance;
    private MyIP myIP;

    public static LocationFragment getInstance() {
        return instance;
    }

    FragmentLocationBinding b;
    Context context;

    IMapController mapController;
    private final String TAG = "LOCATION_PROCESS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentLocationBinding.inflate(inflater, container, false);
        context = getContext();

        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        init();

        return b.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        b.mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapController = b.mapView.getController();

        b.mapView.setOnTouchListener((v, event) -> {
            b.getRoot().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        if (!SharePrefs.getInstance(context).getBoolean("premium")) {
            if (SharePrefs.getInstance(context).getBoolean("showBannerAds")) loadAds();
            else b.myTemplate.setVisibility(View.GONE);
        } else b.myTemplate.setVisibility(View.GONE);

        b.refreshBT.setOnClickListener(v -> {
            first = false;
            getIPLocation();
        });

        getIPLocation();
    }

    private void loadAds() {
        AdLoader adLoader = new AdLoader.Builder(context, getString(R.string.native_id))
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

    boolean first = false;

    public void getIPLocation() {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call = service.getMyIP();

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                myIP = response.body();
                if (!first) {
                    setIPLocation();
                    first = true;
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    public void setIPLocation() {
        if (myIP != null) {
            mapController.animateTo(new GeoPoint(myIP.getLat(), myIP.getLon()));
            mapController.setZoom(15f);

            b.myIpTxt.setText(myIP.getQuery());

            DecimalFormat decimalFormat = new DecimalFormat("##.#######");

            b.latTxt.setText(String.format(getString(R.string.lat_d), decimalFormat.format(myIP.getLat())));
            b.lngTxt.setText(String.format(getString(R.string.lng_d), decimalFormat.format(myIP.getLon())));

            b.regionTxt.setText(myIP.getRegion());
            b.cityTxt.setText(myIP.getCity());
            b.countryTxt.setText(myIP.getCountry());
            b.ispTxt.setText(myIP.getIsp());
        } else getIPLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        b.mapView.onResume();
        setIPLocation();
    }

    @Override
    public void onPause() {
        b.mapView.onPause();
        super.onPause();
    }
}