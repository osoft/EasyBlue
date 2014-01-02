package net.zalio.android.easyblue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.yeelight.blue.SDK.ConnectionManager;
import com.yeelight.blue.SDK.YeelightDevice;

/**
 * Created by Henry on 12/14/13.
 */
public class EasyBlueReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectionManager.DEVICE_FOUND)) {
            //ConnectionManager cm = ConnectionManager.getInstance(context);
            //cm.connect();
            int rssi = intent.getIntExtra(YeelightDevice.EXTRA_RSSI, 0);
            Toast.makeText(context, "found " + rssi, Toast.LENGTH_LONG).show();
        }
    }


}
