package com.doxart.ivpn.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.doxart.ivpn.R;
import com.doxart.ivpn.RetroFit.GetIPDataService;
import com.doxart.ivpn.RetroFit.MyIP;
import com.doxart.ivpn.RetroFit.RetrofitClient;
import com.doxart.ivpn.databinding.ActivityMyLocationBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyLocationActivity extends AppCompatActivity {

    ActivityMyLocationBinding b;

    IMapController mapController;
    private final String TAG = "LOCATION_PROCESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivityMyLocationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        adjustMargins();
        init();
    }

    private void adjustMargins() {
        int statusBarHeight = MainActivity.getInstance().getInsetsCompat().getInsets(WindowInsetsCompat.Type.statusBars()).top;
        int navigationBarHeight = MainActivity.getInstance().getInsetsCompat().getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        b.appbar.setPadding(0, statusBarHeight, 0, 0);
        b.scrollView.setPaddingRelative(0, 0, 0, navigationBarHeight + pxToDp);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        b.mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapController = b.mapView.getController();

        b.mapView.setOnTouchListener((v, event) -> {
            b.scrollView.requestDisallowInterceptTouchEvent(true);
            return false;
        });

        b.closeBT.setOnClickListener(v -> finish());
        b.refreshBT.setOnClickListener(v -> getIPLocation());

        getIPLocation();
    }

    private void getIPLocation() {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call = service.getMyIP();

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                MyIP myIP = response.body();

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

                } else {
                    Toast.makeText(MyLocationActivity.this, "Something wrong..", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                Toast.makeText(MyLocationActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        b.mapView.onResume();
    }

    @Override
    protected void onPause() {
        b.mapView.onPause();
        super.onPause();
    }
}