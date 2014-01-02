package net.zalio.android.easyblue.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Henry on 12/31/13.
 */
public class EasyBlueBulb {

    private static final String TAG = "EasyBlueBulb";
    BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private ConnectionListener mConnectionListener;
    volatile private boolean isConnected;
    private ArrayBlockingQueue<PendingCommand> mPendingCommands;

    private BluetoothGattCallback mCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                EzLog.d(TAG, "Connected: " + mBluetoothDevice.getAddress());
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                EzLog.d(TAG, "DisConnected" + mBluetoothDevice.getAddress());
                isConnected = false;
                if (mConnectionListener != null) {
                    mConnectionListener.onConnectionChanged(EasyBlueBulb.this, isConnected);
                }
            } else {
                EzLog.w(TAG, "EXCEPTION: status:" + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            EzLog.d(TAG, "onServicesDiscovered: " + mBluetoothDevice.getAddress());
            isConnected = true;
            if (!mWorkThread.isAlive()) {
                mWorkThread.start();
            }
            if (mConnectionListener != null) {
                mConnectionListener.onConnectionChanged(EasyBlueBulb.this, isConnected);
            }
        }
    };

    private boolean shouldBeWorking = true;
    private Thread mWorkThread = new Thread() {
        @Override
        public void run() {
            while (shouldBeWorking) {
                if (isConnected) {
                    try {
                        PendingCommand cmd = mPendingCommands.take();
                        EzLog.d(TAG, "Got a pending cmd, executing: " + cmd.data + "on: " + mBluetoothDevice.getAddress());
                        write(cmd.uuid, cmd.data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public EasyBlueBulb(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
        mPendingCommands = new ArrayBlockingQueue<PendingCommand>(Constants.MAX_PENDING_COMMANDS);
    }

    public BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

    public void connect(Context context, ConnectionListener listener) {
        mConnectionListener = listener;
        EzLog.d(TAG, "Connecting: " + mBluetoothDevice.getAddress());
        mBluetoothGatt = mBluetoothDevice.connectGatt(context, true, mCallback);
    }

    public void disconnect(){
        shouldBeWorking = false;
        mWorkThread.interrupt();
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public static interface ConnectionListener {
        void onConnectionChanged(EasyBlueBulb bulb, boolean isConnected);
    }

    public void setColor(int color, int brightness) {
        String data = "";
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        if (brightness > 100) {
            brightness = 100;
        }
        if (brightness < 0) {
            brightness = 0;
        }
        data = r + "," + g + "," + b + "," + brightness + ",";

        mPendingCommands.add(new PendingCommand(Constants.UUID_CONTROL, data));
    }

    private void write(String uuid, String data){
        while (data.length() < 18) {
            data += ",";
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(Constants.YEELIGHT_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic c = service.getCharacteristic(UUID.fromString(uuid));


            if (c != null) {
                c.setValue(data.getBytes());
                boolean res = mBluetoothGatt.writeCharacteristic(c);
                if (!res) {
                    EzLog.w(TAG, "write: write fail");
                } else {
                    EzLog.d(TAG, "Writing to: " + mBluetoothDevice.getAddress());
                }
            }
        }
    }

    public static class PendingCommand {
        protected String uuid;
        protected String data;
        protected PendingCommand(String uuid, String data) {
            this.uuid = uuid;
            this.data = data;
        }
    }
}
