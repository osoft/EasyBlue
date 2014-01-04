package net.zalio.android.easyblue;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import net.zalio.android.easyblue.sdk.EasyBlueBulb;
import net.zalio.android.easyblue.sdk.EasyBlueManager;

import java.util.ArrayList;

/**
 * Created by Henry on 12/14/13.
 */
public class EasyBlueControlService extends Service {
    private static final String TAG = "EasyBlue";
    public static final String ACTION_FIRE = "net.zalio.android.easyblue.PLUGIN_FIRE";
    private boolean mSwitch;
    private int mBrightness;

    private EasyBlueManager mEasyBlueManager;
    private static ArrayList<EasyBlueBulb> mConnectedBulbs;

    private EasyBlueBulb.ConnectionListener mConnectionListener = new EasyBlueBulb.ConnectionListener() {
        @Override
        public void onConnectionChanged(EasyBlueBulb bulb, boolean isConnected) {
            if (isConnected) {
                MyLog.i(TAG, "Bulb connected: " + bulb.getDevice().getAddress());
                bulb.setColor(0xffffff, mBrightness);
                for (EasyBlueBulb b:mConnectedBulbs) {
                    if (b.getDevice().getAddress().equals(bulb.getDevice().getAddress())) {
                        mConnectedBulbs.remove(b);
                    }
                }
                mConnectedBulbs.add(bulb);

            } else {
                MyLog.i(TAG, "Bulb disconnected: " + bulb.getDevice().getAddress());
                bulb.connect(EasyBlueControlService.this, this);
                for (int i = mConnectedBulbs.size() - 1; i >= 0; i--) {
                    EasyBlueBulb b = mConnectedBulbs.get(i);
                    if (b.getDevice().getAddress().equals(bulb.getDevice().getAddress())) {
                        mConnectedBulbs.remove(i);
                    }
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //@Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(this, "IntentService Got! ", Toast.LENGTH_LONG).show();
        Bundle b = intent.getBundleExtra(EditActivity.EXTRA_BUNDLE);
        if (b != null) {
            mSwitch = b.getBoolean(EditActivity.KEY_SWITCH);
            mBrightness = b.getInt(EditActivity.KEY_BRIGHTNESS);
            Toast.makeText(this, "Turn On: " + Boolean.toString(mSwitch), Toast.LENGTH_LONG).show();
        }
        MyLog.d(TAG, "Received intent: " + mSwitch);


        if (mConnectedBulbs == null) {
            mConnectedBulbs = new ArrayList<EasyBlueBulb>();

            BluetoothManager manager = (BluetoothManager) this
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = manager.getAdapter();

            mEasyBlueManager = EasyBlueManager.init(adapter);
            mEasyBlueManager.scan(true, new EasyBlueManager.OnBulbFoundListener() {
                @Override
                public void onBulbFound(EasyBlueBulb bulb) {
                    MyLog.d(TAG, "Found: " + bulb.getDevice().getAddress());
                    bulb.connect(EasyBlueControlService.this, mConnectionListener);
                }
            });
        } else {
            for (EasyBlueBulb bulb:mConnectedBulbs) {
                bulb.setColor(0xffffff, mBrightness);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
