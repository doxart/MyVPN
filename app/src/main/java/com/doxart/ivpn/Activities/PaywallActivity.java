package com.doxart.ivpn.Activities;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.adapty.Adapty;
import com.adapty.errors.AdaptyError;
import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.models.AdaptyProfile;
import com.adapty.models.AdaptyViewConfiguration;
import com.adapty.ui.AdaptyPaywallInsets;
import com.adapty.ui.AdaptyPaywallView;
import com.adapty.ui.AdaptyUI;
import com.adapty.ui.listeners.AdaptyUiEventListener;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Util.PaywallViewUtils;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.databinding.ActivityPaywallBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.List;

public class PaywallActivity extends AppCompatActivity {

    private final String TAG = "APPTAG";
    ActivityPaywallBinding b;

    InterstitialAd mInterstitialAd;
    int showTry = 0;

    boolean isPremium = false;

    SharePrefs sharePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        inflate();

    }

    private void inflate() {
        b = ActivityPaywallBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        sharePrefs = new SharePrefs(this);

        init();
    }

    private void init() {
        buildInterstitial();

        b.payFrame.removeAllViews();

        AdaptyPaywall paywall = PaywallViewUtils.getInstance().getPaywallHolder().getAdaptyPaywall();
        List<AdaptyPaywallProduct> products = PaywallViewUtils.getInstance().getPaywallHolder().getProducts();
        AdaptyViewConfiguration viewConfiguration = PaywallViewUtils.getInstance().getPaywallHolder().getViewConfiguration();

        if (paywall != null & products != null & viewConfiguration != null) {
            getInsets(paywall, products, viewConfiguration);
        } else finish();
    }

    private void getInsets(AdaptyPaywall paywall, List<AdaptyPaywallProduct> products, AdaptyViewConfiguration viewConfiguration) {
        ViewCompat.setOnApplyWindowInsetsListener(b.getRoot(), (v, insets) -> {
            final int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top; // in px
            final int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom; // in px

            int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            if (paywall != null) {
                b.payFrame.setEventListener(eventListener);
                b.payFrame.showPaywall(paywall, products,
                        viewConfiguration,
                        AdaptyPaywallInsets.of(statusBarHeight, navigationBarHeight + pxToDp), adaptyPaywallProduct -> false);

                Adapty.logShowPaywall(paywall);
            }

            //b.payFrame.setPadding(0, statusBarHeight, 0, navigationBarHeight + pxToDp);
            //b.payFrame.addView(paywallView);
            //Adapty.logShowPaywall(paywall);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private final AdaptyUiEventListener eventListener = new AdaptyUiEventListener() {
        @Override
        public void onActionPerformed(@NonNull AdaptyUI.Action action, @NonNull AdaptyPaywallView adaptyPaywallView) {
            if (action instanceof AdaptyUI.Action.Close) {
                if (sharePrefs.getBoolean("showPaywallCloseAd")) {
                    showInterstitial();
                } else onBackPressed();
            }
            Log.d(TAG, "onActionPerformed: " + action);
        }

        @Override
        public boolean onLoadingProductsFailure(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallView adaptyPaywallView) {
            onBackPressed();
            return false;
        }

        @Override
        public void onProductSelected(@NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {

        }

        @Override
        public void onPurchaseCanceled(@NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseCanceled: canceled");
            isPremium = false;
        }

        @Override
        public void onPurchaseFailure(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseFailure: " + adaptyError);
            isPremium = false;
        }

        @Override
        public void onPurchaseStarted(@NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseStarted: started");
        }

        @Override
        public void onPurchaseSuccess(@Nullable AdaptyProfile adaptyProfile, @NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseSuccess: success " + true);
            isPremium = true;
            onBackPressed();
        }

        @Override
        public void onRenderingError(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallView adaptyPaywallView) {
            onBackPressed();
        }

        @Override
        public void onRestoreFailure(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallView adaptyPaywallView) {

        }

        @Override
        public void onRestoreSuccess(@NonNull AdaptyProfile adaptyProfile, @NonNull AdaptyPaywallView adaptyPaywallView) {

        }
    };

    @Override
    public void onBackPressed() {
        sharePrefs.putBoolean("premium", isPremium);
        super.onBackPressed();
    }

    private void buildInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.interstitial_id), adRequest,
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
                    else onBackPressed();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    onBackPressed();
                }
            });

            mInterstitialAd.show(this);
        } else {
            showTry++;
            if (showTry < 3) showInterstitial();
            else onBackPressed();
        }
    }
}