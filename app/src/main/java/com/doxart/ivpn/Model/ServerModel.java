package com.doxart.ivpn.Model;

public class ServerModel {
    private String country, region;
    private String flagUrl;
    private String ovpn;
    private String ovpnUserName;
    private String ovpnUserPassword;
    private boolean premium;
    private String urlToOVPN;
    private String ipv4;

    private int latency;
    private int port;


    public ServerModel() {
    }

    public ServerModel(String country, String region, String flagUrl, String ovpn, String ovpnUserName, String ovpnUserPassword, boolean premium, String urlToOVPN, String ipv4, int latency, int port) {
        this.country = country;
        this.region = region;
        this.flagUrl = flagUrl;
        this.ovpn = ovpn;
        this.ovpnUserName = ovpnUserName;
        this.ovpnUserPassword = ovpnUserPassword;
        this.premium = premium;
        this.urlToOVPN = urlToOVPN;
        this.ipv4 = ipv4;
        this.latency = latency;
        this.port = port;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public void setUrlToOVPN(String urlToOVPN) {
        this.urlToOVPN = urlToOVPN;
    }

    public String getUrlToOVPN() {
        return urlToOVPN;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public String getOvpn() {
        return ovpn;
    }

    public void setOvpn(String ovpn) {
        this.ovpn = ovpn;
    }

    public String getOvpnUserName() {
        return ovpnUserName;
    }

    public void setOvpnUserName(String ovpnUserName) {
        this.ovpnUserName = ovpnUserName;
    }

    public String getOvpnUserPassword() {
        return ovpnUserPassword;
    }

    public void setOvpnUserPassword(String ovpnUserPassword) {
        this.ovpnUserPassword = ovpnUserPassword;
    }
}
