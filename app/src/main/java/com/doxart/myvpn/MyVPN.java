package com.doxart.myvpn;

import android.app.Application;

import com.adapty.Adapty;
import com.google.android.gms.ads.MobileAds;

public class MyVPN extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        Adapty.activate(this, "CHANGE WITH YOUR ADAPTY APP ID");
    }
}
