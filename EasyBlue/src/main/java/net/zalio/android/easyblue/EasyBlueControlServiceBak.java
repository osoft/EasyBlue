package net.zalio.android.easyblue;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.yeelight.blue.SDK.ConnectionManager;
import com.yeelight.blue.SDK.YeelightCallBack;
import com.yeelight.blue.SDK.YeelightDevice;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Henry on 12/14/13.
 */
public class EasyBlueControlServiceBak extends Service {
    private static final String TAG = "EasyBlueControlService";
    public static final String ACTION_FIRE = "net.zalio.android.easyblue.PLUGIN_FIRE";
    private static final int MSG_STOP_SERVICE = 10001;
    private static final int MSG_SWITCH = 10002;
    private static final int MSG_CONNECT = 10003;
    private ConnectionManager mConnMgr;
    private DeviceReceiver mReceiver;
    private boolean mSwitch;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_CONNECT: {
                    Log.i(TAG, "Try to connect...");
                    mConnMgr.stopLEScan();
                    mConnMgr.connect();
                    //sendEmptyMessageDelayed(MSG_SWITCH, 5000);
                    break;
                }
                case MSG_STOP_SERVICE: {
                    if (hasMessages(MSG_SWITCH)) {
                        Log.i(TAG, "Try to stop self but delayed");
                        removeMessages(MSG_STOP_SERVICE);
                        sendEmptyMessageDelayed(MSG_STOP_SERVICE, 2000);
                    } else {
                        Log.i(TAG, "Try to stop self");
                        EasyBlueControlServiceBak.this.stopSelf();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                    break;
                }
                case MSG_SWITCH: {
                    Log.i(TAG, "Setting brightness to " + mConnMgr.getConnected().size() + " devices");
                    mConnMgr.writeBrightAndColor(mSwitch ? 100:0, 0xFFFFFF);
                    sendEmptyMessageDelayed(MSG_STOP_SERVICE, 2000);
                    break;
                }
            }
        }
    };

    private class DeviceReceiver extends BroadcastReceiver {

        WeakReference<EasyBlueControlServiceBak> parentRef;

        DeviceReceiver(EasyBlueControlServiceBak parentService) {
            parentRef = new WeakReference<EasyBlueControlServiceBak>(parentService);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            EasyBlueControlServiceBak parent = parentRef.get();
            if (parent == null) {
                return;
            }

            String action = intent.getAction();
            if (action.equals(ConnectionManager.DEVICE_FOUND)) {
                parent.onDeviceFound(null);

            } else if (action.equals(YeelightCallBack.DEVICE_CONNECTED)) {
                List<YeelightDevice> connectedDevices = parent.mConnMgr.getConnected();
                Log.i(TAG, "DEVICE_CONNECTED: " + connectedDevices.size());
                synchronized (ConnectionManager.lock_connecte_devices_list) {
                    for (YeelightDevice d:connectedDevices) {
                        parent.mConnMgr.writeBrightAndColor(parent.mSwitch ? 100:0, 0xFFFFFF, d);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //Object obj = intent.getParcelableExtra(YeelightDevice.EXTRA_DEVICE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_NOT_STICKY;
    }

    //@Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(this, "IntentService Got! ", Toast.LENGTH_LONG).show();
        Bundle b = intent.getBundleExtra(EditActivity.EXTRA_BUNDLE);
        if (b != null) {
            mSwitch = b.getBoolean(EditActivity.KEY_SWITCH);
            Toast.makeText(this, "Turn On: " + Boolean.toString(mSwitch), Toast.LENGTH_LONG).show();
        }
        mConnMgr = ConnectionManager.getInstance(this);
        IntentFilter iF = new IntentFilter();
        iF.addAction(ConnectionManager.DEVICE_FOUND);
        iF.addAction(YeelightCallBack.DEVICE_CONNECTED);
        mReceiver = new DeviceReceiver(this);
        registerReceiver(mReceiver, iF);
        if (mConnMgr.isScanning()) {
            Log.i(TAG, "Stopping scan");
            mConnMgr.stopLEScan();
        }
        Log.i(TAG, "Starting scan");
        mConnMgr.startLEScan();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnMgr != null) {
            if(mConnMgr.isScanning()) {
                mConnMgr.stopLEScan();
            }
            mConnMgr.disconnect();
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    private void onDeviceFound(YeelightDevice device) {
        if (mConnMgr == null) {
            return;
        }
        List<YeelightDevice> allDevices = mConnMgr.getDevices();
        Log.d(TAG, "=======");
        for (YeelightDevice d:allDevices) {
            Log.d(TAG, "Device addr: " + d.getAddress());
            //mConnMgr.connect(d.getAddress());
        }
        Log.d(TAG, "=======\n");

        mHandler.removeMessages(MSG_CONNECT);
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT, 1000);
    }
}
