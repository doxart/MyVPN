package com.doxart.ivpn.Adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.R;
import com.doxart.ivpn.Interfaces.NavItemClickListener;
import com.doxart.ivpn.Util.Utils;

import java.util.List;


public class ServerListRVAdapter extends RecyclerView.Adapter<ServerListRVAdapter.MyViewHolder> {

    private final List<ServerModel> serverLists;
    private final Context context;
    private final NavItemClickListener listener;

    public ServerListRVAdapter(Context context, List<ServerModel> serverLists, NavItemClickListener listener) {
        this.serverLists = serverLists;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.country_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder h, int p) {
        ServerModel m = serverLists.get(p);

        h.serverCountry.setText(m.getCountry());
        h.serverRegion.setText(m.getRegion());
        h.serverMs.setText(String.format("%sms", m.getLatency()));

        Glide.with(context).load("https://flagcdn.com/h80/" + m.getFlagUrl() + ".png").centerCrop().into(h.serverFlag);

        if (m.isPremium()) {
            h.status.setText(context.getString(R.string.premium));
            h.status.setBackgroundResource(R.drawable.card_premium_bg);
        } else {
            h.status.setText(context.getString(R.string.basic));
            h.status.setBackgroundResource(R.drawable.card_basic_bg);
        }

        Utils.setSignalView(context, h.s1, h.s2, h.s3, m.getLatency());

        h.itemView.setOnClickListener(v -> {
            Log.d("SAGASGASGASGASGASG", "onBindViewHolder: " + m.getOvpn());
            listener.clickedItem(p);
        });
    }

    @Override
    public int getItemCount() {
        return serverLists.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView serverFlag;
        TextView serverCountry, serverRegion, serverMs, status;
        CardView s1, s2, s3;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            serverFlag = itemView.findViewById(R.id.cFlagImg);
            serverCountry = itemView.findViewById(R.id.cCountryTxt);
            serverRegion = itemView.findViewById(R.id.cRegionTxt);
            serverMs = itemView.findViewById(R.id.cMsTxt);
            status = itemView.findViewById(R.id.cStatusTxt);
            s1 = itemView.findViewById(R.id.s1);
            s2 = itemView.findViewById(R.id.s2);
            s3 = itemView.findViewById(R.id.s3);
        }
    }
}
