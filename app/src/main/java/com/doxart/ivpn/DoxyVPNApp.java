package com.doxart.ivpn;

import android.app.Application;

import com.adapty.Adapty;
import com.doxart.ivpn.Util.SharePrefs;
import com.google.android.gms.ads.MobileAds;

public class DoxyVPNApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        Adapty.activate(this, "public_live_EC37CHz8.ZKT3fqeesXZAlYiC6284", false, SharePrefs.getInstance(this).getUid());
    }
}
