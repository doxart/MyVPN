package com.doxart.myvpn.Model;

import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.models.AdaptyViewConfiguration;
import com.adapty.ui.AdaptyPaywallView;

import java.util.List;

public class PaywallModel {
    AdaptyPaywall adaptyPaywall;
    AdaptyViewConfiguration viewConfiguration;
    AdaptyPaywallView adaptyPaywallView;
    List<AdaptyPaywallProduct> products;

    public PaywallModel() {}

    public PaywallModel(AdaptyPaywall adaptyPaywall, AdaptyViewConfiguration viewConfiguration, AdaptyPaywallView adaptyPaywallView, List<AdaptyPaywallProduct> products) {
        this.adaptyPaywall = adaptyPaywall;
        this.viewConfiguration = viewConfiguration;
        this.adaptyPaywallView = adaptyPaywallView;
        this.products = products;
    }

    public AdaptyPaywallView getAdaptyPaywallView() {
        return adaptyPaywallView;
    }

    public void setAdaptyPaywallView(AdaptyPaywallView adaptyPaywallView) {
        this.adaptyPaywallView = adaptyPaywallView;
    }

    public AdaptyPaywall getAdaptyPaywall() {
        return adaptyPaywall;
    }

    public void setAdaptyPaywall(AdaptyPaywall adaptyPaywall) {
        this.adaptyPaywall = adaptyPaywall;
    }

    public AdaptyViewConfiguration getViewConfiguration() {
        return viewConfiguration;
    }

    public void setViewConfiguration(AdaptyViewConfiguration viewConfiguration) {
        this.viewConfiguration = viewConfiguration;
    }

    public List<AdaptyPaywallProduct> getProducts() {
        return products;
    }

    public void setProducts(List<AdaptyPaywallProduct> products) {
        this.products = products;
    }
}
