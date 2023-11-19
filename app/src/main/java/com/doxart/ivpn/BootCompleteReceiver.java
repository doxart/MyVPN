package com.doxart.ivpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.doxart.ivpn.Model.ServerModel;
import com.doxart.ivpn.Util.SharePrefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import de.blinkt.openvpn.OpenVpnApi;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ServerModel server = SharePrefs.getInstance(context).getServer();

        File file = new File(context.getFilesDir().toString() + "/" + server.getOvpn());

        try {
            InputStream conf;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                conf = Files.newInputStream(file.toPath());
            } else conf = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder config = new StringBuilder();
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config.append(line).append("\n");
            }

            br.readLine();

            OpenVpnApi.startVpn(context, config.toString(), server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

        } catch (IOException | RemoteException e) {
            Log.d("AutoConnectException", "startVpn: " + e);
        }
    }
}
