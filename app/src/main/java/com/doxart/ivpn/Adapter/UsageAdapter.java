package com.doxart.ivpn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doxart.ivpn.DB.Usage;
import com.doxart.ivpn.Model.UsageModel;
import com.doxart.ivpn.R;
import com.skydoves.progressview.ProgressView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UsageAdapter extends RecyclerView.Adapter<UsageAdapter.UHolder> {

    Context context;
    List<Usage> usageList = new ArrayList<>();

    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

    public UsageAdapter(Context context, OnViewCreated onViewCreated) {
        this.context = context;
        this.onViewCreated = onViewCreated;
    }

    public UsageAdapter(Context context) {
        this.context = context;
    }

    public interface OnViewCreated {
        void onCreated();
    }

    OnViewCreated onViewCreated;

    public void setUsageList(List<Usage> usageList) {
        this.usageList = usageList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.usage_item, parent, false);
        if (onViewCreated != null) onViewCreated.onCreated();
        return new UHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UHolder h, int p) {
        Usage m = usageList.get(p);

        Date date = new Date();
        date.setTime(m.getDateTime());

        float usage = m.getUsageInMinutes()/60f;

        h.usage.setProgress(usage);

        if (!checkDateIsNowWeek(date)) {
            h.date.setText(dayFormat.format(date));
        } else h.date.setText(DateFormat.getDateInstance().format(date));

        h.hour.setText(String.format("%sh", new DecimalFormat("#.#").format(usage)));
    }

    private boolean checkDateIsNowWeek(Date usageDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date startOfWeek = cal.getTime();

        cal.setTime(usageDate);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date startOfWeekSpecified = cal.getTime();

        return startOfWeek.equals(startOfWeekSpecified);
    }

    @Override
    public int getItemCount() {
        return usageList.size();
    }

    public static class UHolder extends RecyclerView.ViewHolder {
        ProgressView usage;
        TextView date, hour;
        public UHolder(@NonNull View v) {
            super(v);
            usage = v.findViewById(R.id.usageProgress);
            date = v.findViewById(R.id.usageDate);
            hour = v.findViewById(R.id.usageHour);
        }
    }
}
