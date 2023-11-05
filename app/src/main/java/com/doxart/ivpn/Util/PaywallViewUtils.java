package com.doxart.ivpn.Util;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.adapty.Adapty;
import com.adapty.errors.AdaptyError;
import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.models.AdaptyViewConfiguration;
import com.adapty.ui.AdaptyPaywallInsets;
import com.adapty.ui.AdaptyPaywallView;
import com.adapty.ui.AdaptyUI;
import com.adapty.ui.listeners.AdaptyUiEventListener;
import com.adapty.utils.AdaptyResult;
import com.doxart.ivpn.Model.PaywallModel;

import java.util.List;

public class PaywallViewUtils {

    private static final PaywallViewUtils instance = new PaywallViewUtils();

    public String TAG = "PAYWALL_GETTER";

    public PaywallModel paywallHolder = new PaywallModel();

    public interface OnPaywallLoadFinishListener {
        void onFinish();
        void onError();
    }

    public OnPaywallLoadFinishListener paywallLoadFinishListener;

    public void setPaywallLoadFinishListener(OnPaywallLoadFinishListener listener) {
        paywallLoadFinishListener = listener;
    }

    public void getPaywallView(String pid) {
        Adapty.getPaywall(pid, "en", result -> {
            if (result instanceof AdaptyResult.Success) {
                AdaptyPaywall paywall = ((AdaptyResult.Success<AdaptyPaywall>) result).getValue();


                new Handler().postDelayed(() -> {
                    paywallHolder.setAdaptyPaywall(paywall);

                    getProducts(paywall);
                }, 0);
            } else if (result instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result).getError();

                Log.d(TAG, "getPaywallView: " + error);
                if (paywallLoadFinishListener != null) paywallLoadFinishListener.onError();
            }
        });
    }

    private void getProducts(AdaptyPaywall paywall) {
        Adapty.getPaywallProducts(paywall, result -> {
            if (result instanceof AdaptyResult.Success) {
                List<AdaptyPaywallProduct> products = ((AdaptyResult.Success<List<AdaptyPaywallProduct>>) result).getValue();

                new Handler().postDelayed(() -> {
                    paywallHolder.setProducts(products);

                    getViewConf(paywall);
                }, 0);

            } else if (result instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result).getError();
                // handle the error
                Log.d(TAG, "getProducts: " + error);
                if (paywallLoadFinishListener != null) paywallLoadFinishListener.onError();
            }
        });
    }

    public void getViewConf(AdaptyPaywall paywall) {
        Adapty.getViewConfiguration(paywall, "en", result1 -> {
            if (result1 instanceof AdaptyResult.Success) {
                AdaptyViewConfiguration viewConfiguration = ((AdaptyResult.Success<AdaptyViewConfiguration>) result1).getValue();
                Log.d(TAG, "createView: " + viewConfiguration.getTemplateId());

                new Handler().postDelayed(() -> {
                    paywallHolder.setViewConfiguration(viewConfiguration);

                    if (paywallLoadFinishListener != null) paywallLoadFinishListener.onFinish();
                }, 0);
            } else if (result1 instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result1).getError();
                Log.d(TAG, "makePRC: " + error);
                if (paywallLoadFinishListener != null) paywallLoadFinishListener.onError();
            }
        });
    }

    public AdaptyPaywallView createView(Activity activity, AdaptyUiEventListener adaptyUiEventListener) {
        Log.d(TAG, "createView: products " + paywallHolder.getProducts());

        Log.d(TAG, "createView: paywall " + paywallHolder.getAdaptyPaywall());
        Log.d(TAG, "createView: viewConfig " + paywallHolder.getViewConfiguration());

        if (paywallHolder != null & paywallHolder.getAdaptyPaywall() != null & paywallHolder.getViewConfiguration() != null & paywallHolder.getProducts() != null) {
            AdaptyPaywallView adaptyPaywallView = AdaptyUI.getPaywallView(
                    activity,
                    paywallHolder.getAdaptyPaywall(),
                    paywallHolder.getProducts(),
                    paywallHolder.getViewConfiguration(),
                    AdaptyPaywallInsets.of(10, 10),
                    adaptyUiEventListener,
                    adaptyPaywallProduct -> false
            );

            paywallHolder.setAdaptyPaywallView(adaptyPaywallView);

            Log.d(TAG, "createView: paywallView " + paywallHolder.getAdaptyPaywallView());

            return adaptyPaywallView;
        }

        return null;
    }

    public PaywallModel getPaywallHolder() {
        return paywallHolder;
    }

    public static PaywallViewUtils getInstance() {
        return instance;
    }
}
