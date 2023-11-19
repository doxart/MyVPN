package com.doxart.ivpn.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
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
import com.doxart.ivpn.Util.PaywallViewUtils;
import com.doxart.ivpn.Util.SharePrefs;
import com.doxart.ivpn.databinding.ActivityPaywallBinding;

import java.util.List;

public class PaywallActivity extends AppCompatActivity {

    private final String TAG = "APPTAG";
    ActivityPaywallBinding b;

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

    int iTimer = 3000;

    private void init() {
        b.closeBT.setOnClickListener(v -> onBackPressed());

        b.payFrame.removeAllViews();

        AdaptyPaywall paywall = PaywallViewUtils.getInstance().getPaywallHolder().getAdaptyPaywall();
        List<AdaptyPaywallProduct> products = PaywallViewUtils.getInstance().getPaywallHolder().getProducts();
        AdaptyViewConfiguration viewConfiguration = PaywallViewUtils.getInstance().getPaywallHolder().getViewConfiguration();

        iTimer = getIntent().getIntExtra("timer", 3000);

        if (paywall != null & products != null & viewConfiguration != null) {
            getInsets(paywall, products, viewConfiguration);
            new CountDownTimer(iTimer, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    b.closeBTCounter.setText(String.valueOf(millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    b.closeBT.setVisibility(View.VISIBLE);
                    b.closeBTCounter.setVisibility(View.GONE);
                }
            }.start();
        } else onBackPressed();
    }

    private void getInsets(AdaptyPaywall paywall, List<AdaptyPaywallProduct> products, AdaptyViewConfiguration viewConfiguration) {
        ViewCompat.setOnApplyWindowInsetsListener(b.getRoot(), (v, insets) -> {
            final int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            final int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            try {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) b.closeBT.getLayoutParams();
                ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) b.closeBTCounter.getLayoutParams();
                params.setMargins(statusBarHeight, pxToDp + statusBarHeight, 0, 0);
                params1.setMargins(statusBarHeight, pxToDp + statusBarHeight, 0, 0);

                b.closeBT.setLayoutParams(params);
                b.closeBTCounter.setLayoutParams(params);
                b.closeBT.requestLayout();
                b.closeBTCounter.requestLayout();
            } catch (Exception e) {
                Log.d(TAG, "getInsets: " + e);
            }

            b.payFrame.setEventListener(eventListener);
            b.payFrame.showPaywall(paywall, products,
                    viewConfiguration,
                    AdaptyPaywallInsets.of(statusBarHeight, navigationBarHeight + pxToDp), adaptyPaywallProduct -> false);

            Adapty.logShowPaywall(paywall);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private final AdaptyUiEventListener eventListener = new AdaptyUiEventListener() {
        @Override
        public void onActionPerformed(@NonNull AdaptyUI.Action action, @NonNull AdaptyPaywallView adaptyPaywallView) {
            if (action instanceof AdaptyUI.Action.Close) {
                onBackPressed();
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

        Intent data = new Intent();
        data.putExtra("showAD", sharePrefs.getBoolean("showPaywallCloseAd"));
        setResult(Activity.RESULT_OK, data);

        super.onBackPressed();
    }
}