package com.doxart.ivpn.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doxart.ivpn.Activities.MainActivity;
import com.doxart.ivpn.Adapter.ServerListRVAdapter;
import com.doxart.ivpn.DB.ServerDB;
import com.doxart.ivpn.Interfaces.NavItemClickListener;
import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.Util.Utils;
import com.doxart.ivpn.databinding.FragmentServersBinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServersFragment extends Fragment {

    FragmentServersBinding b;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentServersBinding.inflate(inflater, container, false);
        context = getContext();

        adjustMargin();
        init();

        return b.getRoot();
    }

    private void adjustMargin() {
        final int statusBarHeight = Utils.getStatusBarHeight(context);
        final int navigationBarHeight = Utils.getNavigationBarHeight(context);

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        b.appbar.setPaddingRelative(0, statusBarHeight, 0, 0);
    }

    private void init() {
        NavItemClickListener navItemClickListener = (NavItemClickListener) context;

        if (navItemClickListener != null) {
            List<ServerModel> nativeList = ServerDB.getInstance().getServerList();
            List<ServerModel> adjustedList = new ArrayList<>();
            List<ServerModel> adjustedVipList = new ArrayList<>();

            for (ServerModel sv : nativeList) {
                if (sv.getLatency() > 0 & !sv.isPremium()) adjustedList.add(sv);
                if (sv.getLatency() > 0 & sv.isPremium()) adjustedVipList.add(sv);
            }

            adjustedList.sort(Comparator.comparing(ServerModel::getLatency));

            ServerListRVAdapter adapter = new ServerListRVAdapter(context, adjustedList, navItemClickListener, 0);
            ServerListRVAdapter adapter1 = new ServerListRVAdapter(context, adjustedVipList, navItemClickListener, 1);

            b.serverRecycler.setHasFixedSize(true);
            b.serverRecycler.setAdapter(adapter);

            b.vipServerRecycler.setHasFixedSize(true);
            b.vipServerRecycler.setAdapter(adapter1);

            b.closeBT.setOnClickListener(v -> MainActivity.getInstance().closeServerList());
        }
    }
}