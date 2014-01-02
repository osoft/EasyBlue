package com.yeelight.blue.SDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import net.zalio.android.easyblue.R;

@TargetApi(18)
public class ConnectionManager {

	public static final String DEVICE_FOUND = "net.zalio.android.easyblue.DEVICE_FOUND";
    private static final String TAG = "EasyBlueControlService";

    private static ConnectionManager connectionManager;
	private BluetoothAdapter adapter;
	private boolean isScanning = false;
	private BluetoothManager manager;
	private Context context;
	public static List<YeelightDevice> devices = new ArrayList<YeelightDevice>();
	public static List<YeelightDevice> connected_devices = new ArrayList<YeelightDevice>();
	private String str1_cn = "设备不支持蓝牙";
	private String str2_cn = "设备不支持BLE";
	private String str1_en = "Bluetooth not supported.";
	private String str2_en ="BLE is not supported";

    public static Object lock_connecte_devices_list = new Object();
	/**
	 * 
	 * Return a list of {@link YeelightDevice} that has found.
	 * 
	 * Connected and unconncted returns.
	 * 
	 * @return 
	 *  list of com.yeelight.blue.SDK.YeelightDevice.
	 * */
	
	public List<YeelightDevice> getDevices(){
		return devices;
	}
	/**
	 * 
	 * Return a list of {@link YeelightDevice} that has connected.
	 * 
	 * @return 
	 *  list of com.yeelight.blue.SDK.YeelightDevice.
	 * */
	public List<YeelightDevice> getConnected(){
		return connected_devices;
	}
	public static ConnectionManager getInstance(Context context) {
		if(connectionManager==null){
			connectionManager = new ConnectionManager(context);
		}
		return connectionManager;
	} 
	
	private ConnectionManager(Context context) {

		this.context = context;
		manager = (BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE);
		adapter = manager.getAdapter();
	}

	private LeScanCallback callback = new LeScanCallback() {
		private boolean contains = false;

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

			contains = false;
			for (YeelightDevice device2 : devices) {
				if (device2.getAddress().equals(device.getAddress())) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				YeelightDevice yeelightDevice = new YeelightDevice(context,
						device.getAddress(), device, adapter);
				devices.add(yeelightDevice);
			}
			Intent intent = new Intent(DEVICE_FOUND);
			intent.putExtra(YeelightDevice.EXTRA_RSSI, rssi);
			intent.putExtra(YeelightDevice.EXTRA_DEVICE, device);
			context.sendBroadcast(intent);

		}
	};

	/**
	 * 
	 * Starts a scan for Bluetooth LE devices.
	 * */
	//开始搜索BLE设备
	public void startLEScan() {
		Resources res =context.getResources();
		String str1 = "";
		String str2 = "";
		Configuration config =res.getConfiguration();
		if(!config.locale.equals(Locale.CHINA)){
			str1 = str1_en;
			str2 = str2_en;
		} else{
			str1 = str1_cn;
			str2 = str2_cn;
		}
		
		if (adapter == null) {
			Toast.makeText(context, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			return;
		}

		if (!context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(context, R.string.bluetooth_le_not_supported, Toast.LENGTH_SHORT).show();
			return;
		}
		adapter.startLeScan(callback);
		isScanning = true;

	}

	/**
	 * 
	 * Stops an ongoing Bluetooth LE device scan.
	 * */
	public void stopLEScan() {
		adapter.stopLeScan(callback);
		isScanning = false;
	}

	/**
	 
	 * Return true if the local Bluetooth adapter is currently in the Bluetooth LE device discovery process
	 * 
	 * 
	 * @return true if  scaning
	 * */
	public boolean isScanning() {
		return isScanning;
	}

	/**
	 * 
	 * 
	 * Conncet all Bluetooth LE devices
	 * 
	 * */
	public void connect() {

		for (YeelightDevice device : devices) {
			device.connect();
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

	}

	/**
	 * 
	 * 
	 * Connect a a single device.
	 * 
	 * @param address a given MAC address
	 *             
	 * 
	 * */
	public void connect(String address) {
		for (YeelightDevice device : devices) {
			if (device.getAddress().equals(address)) {
				device.connect();
				break;
			}
		}
	}

	/**
	 * 
	 * 
	 * 
	 * Control the brightness and color of a single device
	 * 
	 * 
	 * @param bright  a given brightness value<br>
	 * @param color   a given color <br>
	 * @param device  a given device<br>
	 * */
	//控制单个设备的颜色和亮度
	public void writeBrightAndColor(int bright, int color, YeelightDevice device) {
		String data = "";
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		if (bright > 100) {
			bright = 100;
		}
		if (bright < 0) {
			bright = 0;
		}
		data = r + "," + g + "," + b + "," + bright + ",";
		while (data.length() < 18) {
			data += ",";
		}
		if (device == null) {
			for (YeelightDevice device1 : connected_devices) {
                Log.i(TAG, "Writing to " + device1.getAddress());
                device1.write(YeelightDevice.CHARACTERISTIC_CONTORL, data);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
			}
		} else {
			for (YeelightDevice device1 : connected_devices) {
				if (device1.equals(device)) {
					device1.write(YeelightDevice.CHARACTERISTIC_CONTORL, data);
					break;
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * Control  brightness and color of all devices
	 * 
	 * @param bright a given brightness<br>
	 * @param color  a given color<br>
	 * 
	 * */
	//控制全部设备的颜色和亮度
	public void writeBrightAndColor(int bright, int color) {

		writeBrightAndColor(bright, color, null);

	}

	/**
	 * 
	 * 
	 * Set time delay ON/OFF of all deivces
	 * 
	 * @param time  delay time(give 0 if you want cancel delay )<br>
	 * @param state delay on({@link com.yeelight.blue.SDK.YeelightDevice#STATE_ON}
	 *            )or delay off({@link com.yeelight.blue.SDK.YeelightDevice#STATE_OFF})
	 * 
	 * */
	//设置全部设备的延时开关
	public void writeDelay(int time, int state) {
		writeDelay(time, state, null);

	}

	/**
	 * 
	 * Set time delay ON/OFF of a single device
	 * 
	 * 
	 * @param time  delay time(give 0 if you want cancel delay )<br>
	 * @param state delay on({@link com.yeelight.blue.SDK.YeelightDevice#STATE_ON}
	 *            )or delay off({@link com.yeelight.blue.SDK.YeelightDevice#STATE_OFF})
	 * @param device a given device
	 * */
	//设置单个设备的延时开关
	public void writeDelay(int time, int state, YeelightDevice device) {
		String data = "";
		data = time + "," + state + ",";
		while (data.length() < 8) {
			data += ",";
		}
		if (device == null) { 
			for (YeelightDevice device1 : connected_devices) {
				device1.write(YeelightDevice.CHARACTERISTIC_DELAY, data);
			}
		} else {
			for (YeelightDevice device1 : connected_devices) {
				if (device1.equals(device)) {
					device1.write(YeelightDevice.CHARACTERISTIC_DELAY, data);
					break;
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * Set colorflow data of a single device
	 * 
	 * @param index
	 *             the index of this command.
	 * @param color
	 *             a given color 
	 * @param bright
	 *             a given brightness
	 * @param time
	 *             time to show
	 * @param device
	 *             a given device
	 * 
	 * 
	 * */
	//设置单个设备的ColorFlow
	public void writeColorFlow(int index, int color, int bright, int time,
			YeelightDevice device) {

		String data = "";

		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		if (bright > 100) {
			bright = 100;
		}
		if (bright < 0) {
			bright = 0;
		}
		data = index + "," + r + "," + g + "," + b + "," + bright + "," + time
				+ ",";
		while (data.length() < 20) {
			data += ",";
		}
		if (device == null) {
			for (YeelightDevice device1 : connected_devices) {
				device1.write(YeelightDevice.CHARACTERISTIC_COLORFLOW, data);
			}
		} else {
			for (YeelightDevice device1 : connected_devices) {
				if (device1.equals(device)) {
					device1.write(YeelightDevice.CHARACTERISTIC_COLORFLOW, data);
					break;
				}
			}
		}

	}

	/**
	 * 
	 * 
	 * Set colorflow data of all devices
	 * 
	 * @param index
	 *             the index of this command.
	 * @param color
	 *             a given color 
	 * @param bright
	 *             a given brightness
	 * @param time
	 *             time to show
	 * 
	 * 
	 * */
	public void writeColorFlow(int index, int color, int bright, int time) {
		writeColorFlow(index, color, bright, time, null);
	}

	/**
	 * 
	 * 
	 * Start colorflow of all devices<br>
	 * 
	 * Make sure you have set data for colorflow, before you call this method.
	 * 
	 * */
	//启动全部设备的ColorFlow
	public void startColorFlow() {
		startColorFlow(null);

	}

	/**
	 * 
	 * 
	 * Start colorflow of a single device.<br>
	 * 
	 * Make sure you have set data for colorflow, before you call this method.
	 * 
	 * @param device
	 *             a given device
	 * 
	 * */
//启动某个设备的ColorFlow
	public void startColorFlow(YeelightDevice device) {
		writeColorFlow("CB", device);

	}

	/**
	 * 
	 * Turn off colorflow of all devices
	 * */
	//关闭全部设备的ColorFlow
	public void stopColorFlow() {
		stopColorFlow(null);
	}

	/**
	 * 
	 * 
	 * Turn off colorflow of a single device
	 * 
	 * */
	//关闭某个设备的ColorFlow
	public void stopColorFlow(YeelightDevice device) {

		writeColorFlow("CE", device);
	}

	/**
	 *  
	 * 
	 * */
	private void writeColorFlow(String data, YeelightDevice device) {
		if (device == null) {
			for (YeelightDevice device1 : connected_devices) {
				device1.write(YeelightDevice.CHARACTERISTIC_COLORFLOW, data);
			}
		} else {
			for (YeelightDevice device1 : connected_devices) {
				if (device1.equals(device)) {
					device1.write(YeelightDevice.CHARACTERISTIC_COLORFLOW, data);
					break;
				}
			}
		}
	}

	
	
	/**
	 * 
	*
	*	Read the status of a connected device.
	*   This is an asynchronous call.
	*   Register for {@link com.yeelight.blue.SDK.YeelightCallBack#STATUS_NOTIFICATION} to be notified when the result is returned.
	*
	*
	*   @param device a given device
 	 * */
	/*//查询灯的状态 查询结果会异步返回。
	 * 通过广播{@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#STATUS_NOTIFICATION}返回<br>
	 * 改广播中总是会包含两个数据项 : {@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS}和{@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}即设备的mac地址和返回的数据
	 * <br>
	 * 返回结果格式如下：<br>
	 * R,G,B,L,CF,DL,… （注：所有数据长度不足一致补逗号,字符串长度20）<br>
	 *	参数详解：<br>
	*	R: 数字, 0~255, 红色分量;<br>
	*	G : 数字, 0~255, 绿色分量;<br>
	*	B : 数字, 0~255, 蓝色分量;<br>
	*	L: 数字, 0~100, 亮度;<br>
	*	CF: 数字, 1：color flow 进行状态； 0：color flow 停止状态<br>
	*	DL : 数字，1：延时开关进行状态； 0：延时开关停止状态 <br>*/
	public void getDeviceState(final YeelightDevice device){
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				query(YeelightDevice.CHARACTERISTIC_STATUS_QUERY, "S", device);
			}
		}, 100);


		
	}
	
	/**
	
     *   Read the delay state of a connected device.
	 *   This is an asynchronous call.
	 *   Register for {@link com.yeelight.blue.SDK.YeelightCallBack#DELAY_NOTIFICATION} to be notified when the result is returned.
	 *
     * 
     *  @param device a given device
     * 
	 * */
/*	 * 查询灯的延时状态 查询结果会异步返回。
	 * 通过广播{@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#DELAY_NOTIFICATION}返回<br>
	 * 改广播中总是会包含两个数据项 : {@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS}和{@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}即设备的mac地址和返回的数据
	 * <br>
	 * 返回结果格式如下：<br>
	 * RTB T,S,… （注：所有数据长度不足一致补逗号） <br>
	 * 参数详解： <br>
     * T :数字, 表示延迟开关的时间, 单位分钟了; 0表示未设置延时开关;<br> 
     * S:数字, 为0 – 延时关灯, 1 – 延时开灯;<br>
     * */
	public void getDelayState(final YeelightDevice device){
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				query(YeelightDevice.CHARACTERISTIC_DELAY_QUERY,"RT",device);			
			}
		}, 100);
	
		
	}
	/**
	 * 
     * 
     *   Read the colorflow state of a connected device.
	 *   This is an asynchronous call.
	 *   Register for {@link com.yeelight.blue.SDK.YeelightCallBack#COLORFLOW_NOTIFICATION} to be notified when the result is returned.
     * 
     * @param device a given device
     *  
	 * */
	/*查询灯的COLORFLOW状态 查询结果会异步返回。
	 * 通过广播{@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#COLORFLOW_NOTIFICATION}返回<br>
	 * 改广播中总是会包含两个数据项 : {@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS}和{@link com.yeelight.blue.SDK.com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}即设备的mac地址和返回的数据
	 * <br>
	 * 返回结果格式如下：<br>
	 * RTB T,S,… （注：所有数据长度不足一致补逗号） <br>
	 * 参数详解： <br>
     * T :数字, 表示延迟开关的时间, 单位分钟了; 0表示未设置延时开关;<br> 
     * S:数字, 为0 – 延时关灯, 1 – 延时开灯;<br>*/
	public void getColorFlowState(final YeelightDevice device){
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				query(YeelightDevice.CHARACTERISTIC_COLORFLOW_QUERY,"CF",device);		
			}
		}, 100);
		
	}
	
	
	private void query(final String uuid,final String data ,YeelightDevice device){
		for ( YeelightDevice device1 : connected_devices) {
			if (device1.equals(device)) {
				final YeelightDevice device2 = device1;
				BluetoothGattCharacteristic notificationCharac = null;
				
				if(uuid.equals(YeelightDevice.CHARACTERISTIC_DELAY_QUERY)){
					
					notificationCharac = device1.getCharacteristic(YeelightDevice.CHARACTERISTIC_DELAY_NOTIFICATION);
					
				}else if(uuid.equals(YeelightDevice.CHARACTERISTIC_STATUS_QUERY)){
					
					notificationCharac = device1.getCharacteristic(YeelightDevice.CHARACTERISTIC_STATE_NOTIFICATION);
					
				}else if(uuid.equals(YeelightDevice.CHARACTERISTIC_COLORFLOW_QUERY)){
					
					notificationCharac = device1.getCharacteristic(YeelightDevice.CHARACTERISTIC_COLORFLOW_NOTIFICATION);
					
				}else{
					
				}
				
				
				if(notificationCharac!=null){
					System.out.println(device1.enableNotification(true, notificationCharac));;
					System.out.println(device1.enableNotification(true, notificationCharac));;
				}
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					device2.write(uuid, data);
				}
			}, 200);
				
				
				break;
			}
		}
	}
	
	
	/**
	
	 * 
	 * Disconncet all devices
	 * 
	 * */
	// * 断开所有设备连接
	public void disconnect() {
        synchronized (lock_connecte_devices_list) {
            for (YeelightDevice device : connected_devices) {
                device.disconnect();
            }
        }
	}

	/**
	 * 
	 * Disconncet a single device
	 * 
	 * 
	 * @param address a given MAC address
	 * 
	 * */
	//断开单个设备连接 @param address 某个要断开的设备MAC地址
	public void disconnect(String address) {
        synchronized (lock_connecte_devices_list) {
            for (YeelightDevice device : connected_devices) {
                if (device.getAddress().equals(address)) {
                    device.disconnect();
                    break;
                }
            }
        }
	}
	/**
	 * 
	 * 
	 * Set gradual mode for color change of all devices
	 * 
	 * @param mode The mode for device .Can be one of:{@link YeelightDevice#SHADE_START},{@link YeelightDevice#SHADE_STOP} or {@link YeelightDevice#SHADE_HALF}
	 * 
	 * */
	//设置所有设备的渐变模式 @param mode 渐变模式
	public void setGradual(int mode){
		for (YeelightDevice device : connected_devices) {
			device.setGradual(mode);
		}
	}
	
	/**
	 * 
	 * 
	 * Set gradual mode for color change of a single device
	 * 

	 * 
	 * @param mode The mode for device .Can be one of:{@link YeelightDevice#SHADE_START},{@link YeelightDevice#SHADE_STOP} or {@link YeelightDevice#SHADE_HALF}
	 * @param address A given MAC address
	 *  
	 * 
	 * */	
	//设置单个设备的渐变模式  	 * @param mode 渐变模式	 * @param address 要设置的设备的MAC地址
	public void setGradual(int mode,String address){
		for (YeelightDevice device : connected_devices) {
			if (device.getAddress().equals(address)) {
				device.setGradual(mode);
				break;
			}
		}
	}
	
	/**
	 *   Read the RSSI of a remote device.
	 *   This is an asynchronous call.
	 *   Register for {@link com.yeelight.blue.SDK.YeelightCallBack#REMOTERSSI} to be notified when the result is returned.
     * 
     *  @param device a given device MAC address
	 * 
	 * */
	public void getRemoteRSSI(String address){
		for (YeelightDevice device : connected_devices) {
			if (device.getAddress().equals(address)) {
				device.readRemoteRSSI();
				break;
			}
		}
	}
}
