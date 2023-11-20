package com.doxart.ivpn.DB;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import com.doxart.ivpn.Adapter.ServerListRVAdapter;
import com.doxart.ivpn.Interfaces.NavItemClickListener;
import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.R;
import com.doxart.ivpn.databinding.VpnListSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ServerDB {

    private static final ServerDB instance = new ServerDB();

    public interface OnServerReadyListener {
        void onReady();
    }

    private List<ServerModel> serverList;

    public void getServers(Context context, OnServerReadyListener onServerReadyListener) {
        List<ServerModel> list = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("servers").whereEqualTo("active", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    ServerModel serverModel = new ServerModel();

                    serverModel.setOvpn(snapshot.getString("ovpn"));
                    serverModel.setRegion(snapshot.getString("region"));
                    serverModel.setFlagUrl(snapshot.getString("flagUrl"));
                    serverModel.setOvpnUserName(snapshot.getString("ovpnUserName"));
                    serverModel.setOvpnUserPassword(snapshot.getString("ovpnUserPassword"));
                    serverModel.setCountry(snapshot.getString("country"));
                    serverModel.setPremium(Boolean.TRUE.equals(snapshot.getBoolean("premium")));
                    serverModel.setUrlToOVPN(snapshot.getString("urlToOVPN"));
                    serverModel.setIpv4(snapshot.getString("ipv4"));
                    serverModel.setPort(Objects.requireNonNull(snapshot.getLong("port")).intValue());

                    new Thread(() -> serverModel.setLatency(getPing(serverModel.getIpv4(),  serverModel.getPort()))).start();

                    File file = new File(context.getFilesDir().toString() + "/" + serverModel.getOvpn());
                    if (!file.exists()) new Thread(() -> downloadOVPN(context, serverModel.getUrlToOVPN(), serverModel.getOvpn())).start();

                    list.add(serverModel);
                }

                list.sort(Comparator.comparing(ServerModel::getLatency));

                setServerList(list);
                if (onServerReadyListener != null) onServerReadyListener.onReady();
            }
        });
    }

    private void downloadOVPN(Context context, String url, String name) {
        Log.d("SYNC getUpdate", "downloadOVPN: getting: " + url);
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream(context.getFilesDir().toString() + "/" + name);
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
    }

    public int getPing(String URL, int port) {
        long ms = 0;
        try {
            long startTime = System.currentTimeMillis();
            Socket socket = new Socket(URL, port);
            long endTime = System.currentTimeMillis();

            ms = endTime - startTime;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (int) ms;
    }

    public void setServerList(List<ServerModel> serverList) {
        this.serverList = serverList;
    }

    public List<ServerModel> getServerList() {
        return serverList;
    }

    public static BottomSheetDialog getListView(Context context, NavItemClickListener navItemClickListener) {
        VpnListSheetBinding b = VpnListSheetBinding.bind(LayoutInflater.from(context).inflate(R.layout.vpn_list_sheet, null));

        BottomSheetDialog d = new BottomSheetDialog(context);
        d.setContentView(b.getRoot());
        d.show();

        ServerListRVAdapter adapter = new ServerListRVAdapter(context, ServerDB.getInstance().getServerList(), navItemClickListener, 0);

        b.serverRecycler.setHasFixedSize(true);
        b.serverRecycler.setAdapter(adapter);

        return d;
    }

    public static ServerDB getInstance() {
        return instance;
    }
}
