package com.doxart.myvpn.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.doxart.myvpn.Model.ServerModel;

import java.util.UUID;

public class SharePrefs {
    public static String PREFERENCE = "DoxyVPNUser";


    private final Context ctx;
    public SharedPreferences sharedPreferences;
    private static SharePrefs instance;


    public SharePrefs(Context context) {
        this.ctx = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCE, 0);
    }

    public static SharePrefs getInstance(Context ctx) {
        if (instance == null) {
            instance = new SharePrefs(ctx);
        }
        return instance;
    }

    public void putString(String key, String val) {
        sharedPreferences.edit().putString(key, val).apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public String getCollReference(String key) {
        return sharedPreferences.getString(key, "all-coll");
    }

    public void putInt(String key, Integer val) {
        sharedPreferences.edit().putInt(key, val).apply();
    }

    public void increaseInt(String key, Integer val){
        sharedPreferences.edit().putInt(key, sharedPreferences.getInt(key, 0) + val).apply();
    }

    public long getLong(String key){
        return sharedPreferences.getLong(key, 0);
    }

    public String getUid() {
        if (sharedPreferences.getString("uuid", "").isEmpty()) {
            sharedPreferences.edit().putString("uuid", UUID.randomUUID().toString()).apply();
        }

        return sharedPreferences.getString("uuid", "");
    }

    public long getLastAd() {
        return sharedPreferences.getLong("lastAd", 0);
    }

    public void putLong(String key, long val){
        sharedPreferences.edit().putLong(key, val).apply();
    }

    public void putBoolean(String key, Boolean val) {
        sharedPreferences.edit().putBoolean(key, val).apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public Boolean isDynamicBackground() {
        return sharedPreferences.getBoolean("dynamicBackground", true);
    }

    public void setDynamicBackground(Boolean val) {
        sharedPreferences.edit().putBoolean("dynamicBackground", val).apply();
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void clearSharePrefs() {
        sharedPreferences.edit().clear().apply();
    }

    public Boolean isAutoConnect() {
        return sharedPreferences.getBoolean("autoConnect", false);
    }

    public void setAutoConnect(Boolean val) {
        sharedPreferences.edit().putBoolean("autoConnect", val).apply();
    }

    public void putServer(ServerModel server) {
        if (server.getOvpn() == null & server.getCountry() != null) server.setOvpn(server.getCountry() + ".ovpn");
        if (server.getOvpn() != null & server.getCountry() != null & server.getOvpnUserName() != null & server.getOvpnUserPassword() != null) {
            sharedPreferences.edit().putString("serverOVPN", server.getOvpn()).apply();
            sharedPreferences.edit().putString("serverCountry", server.getCountry()).apply();
            sharedPreferences.edit().putString("serverUsername", server.getOvpnUserName()).apply();
            sharedPreferences.edit().putString("serverPassword", server.getOvpnUserPassword()).apply();
            sharedPreferences.edit().putBoolean("serverIsPremium", server.isPremium()).apply();
        }
    }

    public ServerModel getServer() {
        ServerModel s = new ServerModel();
        s.setOvpn(sharedPreferences.getString("serverOVPN", ""));
        s.setCountry(sharedPreferences.getString("serverCountry", ""));
        s.setOvpnUserName(sharedPreferences.getString("serverUsername", ""));
        s.setOvpnUserPassword(sharedPreferences.getString("serverPassword", ""));
        s.setPremium(sharedPreferences.getBoolean("serverIsPremium", false));

        return s;
    }
}
