package com.doxart.ivpn.Util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.doxart.ivpn.Interfaces.OnAnswerListener;
import com.doxart.ivpn.R;
import com.doxart.ivpn.databinding.AskViewBinding;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static Dialog setProgress(Context context){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.progress_view);
        if (dialog.getWindow() != null) dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(false);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);

        return dialog;
    }

    public static void shareApp(Context context) {
        final String appPackageName = context.getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static void openAppInPlayStore(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static double getNetSpeed(String urlStr) {
        double downloadSpeed = 0.0;
        try {
            URL url = new URL("https://www.google.com");
            URLConnection connection = url.openConnection();

            long startTime = System.currentTimeMillis();
            connection.connect();
            long endTime = System.currentTimeMillis();

            long fileSize = connection.getContentLength();

            if (fileSize > 0) {
                double downloadTime = (endTime - startTime) / 1000.0;
                downloadSpeed = (fileSize / downloadTime) * 8 / 1000000;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadSpeed;
    }

    public static String getImgURL(int resourceId) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resourceId).toString();
    }

    public static void setSignalView(Context context, CardView s1, CardView s2, CardView s3, int latency) {
        int resGreen = ContextCompat.getColor(context, R.color.green);
        int resDark = ContextCompat.getColor(context, R.color.blat1);
        int resOrange = ContextCompat.getColor(context, R.color.orange);
        int resRed = ContextCompat.getColor(context, R.color.red);

        if (latency < 100) {
            s1.setCardBackgroundColor(resGreen);
            s2.setCardBackgroundColor(resGreen);
            s3.setCardBackgroundColor(resGreen);
        } else if (latency > 100 & latency < 300) {
            s1.setCardBackgroundColor(resOrange);
            s2.setCardBackgroundColor(resOrange);
            s3.setCardBackgroundColor(resDark);
        } else if (latency > 300) {
            s1.setCardBackgroundColor(resRed);
            s2.setCardBackgroundColor(resDark);
            s3.setCardBackgroundColor(resDark);
        } else {
            s1.setCardBackgroundColor(resDark);
            s2.setCardBackgroundColor(resDark);
            s3.setCardBackgroundColor(resDark);
        }
    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yy ", Locale.US);

    public static String getToday() {
        Date today = new Date();
        today.setTime(today.getTime()+100000);

        return dateFormat.format(today);
    }

    public static Dialog askQuestion(Context context, String title, String contain,
                                     String positive, String negative, String other,
                                     SpannableStringBuilder isSpanned, boolean showCheckImg,
                                     OnAnswerListener answerListener){
        View view = LayoutInflater.from(context).inflate(R.layout.ask_view, null);
        AskViewBinding b = AskViewBinding.bind(view);

        Dialog ask = new Dialog(context);
        ask.setContentView(b.getRoot());
        if (ask.getWindow() != null) ask.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ask.setCancelable(false);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (ask.getWindow() != null) {
            layoutParams.copyFrom(ask.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            ask.getWindow().setAttributes(layoutParams);
        }

        ask.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);

        b.askTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT));

        if (showCheckImg) {
            b.checkImg.setVisibility(View.VISIBLE);
            b.askNegative.setVisibility(View.GONE);
        }

        if(!contain.isEmpty()) b.askContain.setText(Html.fromHtml(contain));
        else b.askContain.setText(isSpanned);

        if (!positive.isEmpty()) b.askPositive.setText(positive);
        if (!negative.isEmpty()) b.askNegative.setText(negative);
        if (!other.isEmpty()) {
            b.askOther.setVisibility(View.VISIBLE);
            b.askOther.setText(other);
        }
        else b.askOther.setVisibility(View.GONE);

        b.askPositive.setOnClickListener(v -> {
            ask.dismiss();
            if (answerListener != null) {
                answerListener.onPositive();
            }
        });

        b.askNegative.setOnClickListener(v -> {
            ask.dismiss();
            if (answerListener != null) {
                answerListener.onNegative();
            }
        });

        b.askOther.setOnClickListener(v -> {
            ask.dismiss();
            if (answerListener != null) {
                answerListener.onOther();
            }
        });
        return ask;
    }

    public static boolean checkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo != null && nInfo.isConnectedOrConnecting();
    }
}
