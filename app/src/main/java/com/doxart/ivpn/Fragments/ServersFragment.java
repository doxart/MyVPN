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
        WindowInsetsCompat insets = MainActivity.getInstance().getInsetsCompat();
        final int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
        final int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        b.appbar.setPaddingRelative(0, statusBarHeight + pxToDp, 0, 0);

        b.serverRecycler.setPaddingRelative(0, 0, 0, pxToDp);
    }

    private void init() {
        NavItemClickListener navItemClickListener = (NavItemClickListener) context;

        if (navItemClickListener != null) {
            List<ServerModel> nativeList = ServerDB.getInstance().getServerList();
            List<ServerModel> adjustedList = new ArrayList<>();

            for (ServerModel sv : nativeList) {
                if (sv.getLatency() > 0) adjustedList.add(sv);
            }

            adjustedList.sort(Comparator.comparing(ServerModel::getLatency));

            ServerListRVAdapter adapter = new ServerListRVAdapter(context, adjustedList, navItemClickListener);

            b.serverRecycler.setHasFixedSize(true);
            b.serverRecycler.setAdapter(adapter);

            b.closeBT.setOnClickListener(v -> MainActivity.getInstance().closeServerList());
        }
    }
}