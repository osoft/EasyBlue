package net.zalio.android.easyblue.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Henry on 12/31/13.
 */
public class EasyBlueManager {
    private static final String TAG = "EasyBlueManager";
    private static EasyBlueManager INSTANCE = null;
    private boolean mScanning;
    private static BluetoothAdapter mBluetoothAdapter;
    private OnBulbFoundListener mBulbFoundListener;

    private EasyBlueManager(){ }

    private Handler mHandler = new Handler();

    public static EasyBlueManager init(BluetoothAdapter adapter) {
        if (adapter != null) {
            mBluetoothAdapter = adapter;
            INSTANCE = new EasyBlueManager();
            return INSTANCE;
        } else {
            throw new InvalidParameterException();
        }
    }

    private ArrayList<BluetoothDevice> mDevices;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            EzLog.d(TAG, "onLeScan");
            //for (BluetoothDevice d:mDevices) {
            //    if (d.getAddress().equals(device.getAddress())) {
            //        return;
            //    }
            //}
            //mDevices.add(device);
            EasyBlueBulb b = new EasyBlueBulb(device);
            if (mBulbFoundListener != null) {
                EzLog.d(TAG, "onBulbFound");
                mBulbFoundListener.onBulbFound(b);
            }
        }
    };

    public EasyBlueManager getInstance() {
        return INSTANCE;
    }

    public void scan(final boolean enable, OnBulbFoundListener listener) {
        if (enable) {
            UUID uuids[] = {UUID.fromString(Constants.YEELIGHT_SERVICE)};
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, Constants.SCAN_PERIOD_MS);

            mScanning = true;
            mBulbFoundListener = listener;
            //mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBulbFoundListener = null;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public static interface OnBulbFoundListener {
        void onBulbFound(EasyBlueBulb bulb);
    }
}
