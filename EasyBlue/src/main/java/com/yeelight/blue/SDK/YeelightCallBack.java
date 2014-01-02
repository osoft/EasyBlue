package com.yeelight.blue.SDK;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressLint("NewApi")
public class YeelightCallBack extends BluetoothGattCallback{
	
	private static final String TAG = "EasyBlueControlService";
	/**
	 * 
	 *
	 * 
	 * Broadcast Action: The device has connected successful
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device. 
	 * */
	// 设备连接时会发送此广播,包含{@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS}
	public static final String DEVICE_CONNECTED ="com.yeelight.blue.DEVICE_CONNECTED";
	
	
	/**
	 * 
	 * 
	 * Broadcast Action: The device has disconnected
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device.
	 * 
	 * */
	//设备断开后 会发送此广播,包含{@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS}
	public static final String DEVICE_DISCONNECTED ="com.yeelight.blue.DEVICE_DISCONNECTED";
	
	
	
	
	private static final String SERVICE_FOUND ="com.yeelight.blue.SERVICE_FOUND";
	
	/**
	 
	 *  
	 * Broadcast Action: BluetoothCharacteristic has written successful
	 * 
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device;{@link YeelightCallBack#EXTRA_RESULT} The value has been written;
	 *              					 
	 * 
	 * 
	 * */
	//* 写入Characteristic成功后会发送此广播,包含{@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS} 和 {@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}
	public static final String WRITE_SUCCESS = "com.yeelight.blue.WRITE_SUCCESS";
	
	
	/**
	 *
	 * 
	 * Broadcast Action: Receive delay information of remote device. 
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device;{@link YeelightCallBack#EXTRA_RESULT} Status information of the connected device; 
	 * 
	 * */
	
	public static final String DELAY_NOTIFICATION=  "com.yeelight.blue.DELAY_NOTIFICATION";
	/**
	
	 * 
	 * Broadcast Action: Receive status  of remote device.  
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device;{@link YeelightCallBack#EXTRA_RESULT} Status information of the connected device; 
	 * 
	 * 
	 * 
	 * */
	// * 收到状态广播后会发送此广播,包含{@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS} 和 {@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}
	
	public static final String STATUS_NOTIFICATION =  "com.yeelight.blue.STATUS_NOTIFICATION";
	/**
	 
	 * 
	 * Broadcast Action: Receive colorflow status  of remote device.  
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device;{@link YeelightCallBack#EXTRA_RESULT} Colorflow information of the connected device;
	 * 
	 * 
	 * */
	//* 收到ColorFlow广播后会发送此广播,包含{@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS} 和 {@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}
	public static final String COLORFLOW_NOTIFICATION = "com.yeelight.blue.COLORFLOW_NOTIFICATION";
	/**
	
	 * Broadcast Action: Receive RSSI value  of remote device.  
	 * Constant Value:{@link YeelightCallBack#EXTRA_ADDRESS} The MAC address of the connected device;{@link YeelightCallBack#EXTRA_RESULT} RSSI value of the connected device;
	 * 
	 * */
	// * 收到rssi后会发送此广播,包含{@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_ADDRESS} 和 {@link com.yeelight.blue.SDK.YeelightCallBack#EXTRA_RESULT}
	public static final String REMOTERSSI = "com.yeelight.blue.REMOTERSSI";
	
	/**
	 
	 * 
	 * Used as an int extra field in some intents to request the current MAC address.
	 * 
	 **/
	//* 用于标识地址信息
	public static final String EXTRA_ADDRESS = "address";
	/**
	 
	 * 
	 * Used as an int extra field in some intents to request the current result.
	 * 
	 * */
	//* 用于标识结果信息
	public static final String EXTRA_RESULT  = "result";
	
	private YeelightDevice device;
	private Context context;
	private boolean debug = true;
	
	
	/**
	 * 构造函数
	 * */
	YeelightCallBack(Context context,YeelightDevice device) {
		this.context = context;
		this.device = device;
		debug = ConnectionUtils.DEBUG;
		debug = true;
	}

	
	
	
	/**
	 * 
	 
	 * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
	 * */
	//* 当设备连接状态改变后会回调此函数
	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status,
			int newState) {
		super.onConnectionStateChange(gatt, status, newState);
		if(debug)
			Log.i(TAG, "newstate:"+newState +" === status:"+status);
		if(newState==2 && status==0){
			if(debug)
				Log.i(TAG, "Yeelightdevice ====> startDiscoverServices");
			gatt.discoverServices();
		}else{
			if(ConnectionManager.connected_devices.contains(device)){
                synchronized (ConnectionManager.lock_connecte_devices_list) {
				    ConnectionManager.connected_devices.remove(device);
                }
			}
			
		}
		
	}

	/**
	
	 * Callback reporting the RSSI for a remote device connection. This callback is triggered in response to the readRemoteRssi() function.
	 * */
	// * 当设备返回RSSI值时会回调此函数
	@Override
	public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
		super.onReadRemoteRssi(gatt, rssi, status);
		Intent i = new Intent(REMOTERSSI);
		i.putExtra(EXTRA_RESULT, rssi);
		i.putExtra(EXTRA_ADDRESS, gatt.getDevice().getAddress());
		context.sendBroadcast(i);
	}
	
	/**
	 *Callback indicating the result of a characteristic write operation. 
	 * 
	 * */
	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
		// TODO Auto-generated method stub
		super.onCharacteristicWrite(gatt, characteristic, status);
		if(debug){
			if(status==0){
				Log.i(TAG, "Write success ,characteristic uuid=:"+characteristic.getUuid().toString());
			}else{
				Log.i(TAG, "Write fail ,characteristic uuid=:"+characteristic.getUuid().toString()+" status="+status);
			}
		}
	}
	
	/**
	 * 
	 * Callback triggered as a result of a remote characteristic notification.
	 * */
	//当收到设备的Notification后会回调此函数
	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic) {
		// TODO Auto-generated method stub
		super.onCharacteristicChanged(gatt, characteristic);
		String uuid = characteristic.getUuid().toString();
		if(debug)
			Log.i(TAG, "NOTIFICATION ===> "+ConnectionUtils.bytesToString(characteristic.getValue()));
		if(uuid.equals(YeelightDevice.CHARACTERISTIC_DELAY_NOTIFICATION)){
			
			sendBroadcast(DELAY_NOTIFICATION, gatt, characteristic,true);
			
		}else if(uuid.equals(YeelightDevice.CHARACTERISTIC_COLORFLOW_NOTIFICATION)){
			
			sendBroadcast(COLORFLOW_NOTIFICATION, gatt, characteristic,true);
			
		}else if(uuid.equals(YeelightDevice.CHARACTERISTIC_STATE_NOTIFICATION)){
			
			sendBroadcast(STATUS_NOTIFICATION, gatt, characteristic,true);
			
		}
		
	}
	
	/*
	 * 用于发送广播
	 * 
	 * 
	 * 
	 * */
	private void sendBroadcast(String action,BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,boolean result){
		Intent intent = new Intent(action);
		intent.putExtra(EXTRA_ADDRESS, gatt.getDevice().getAddress());
		if(result){
			intent.putExtra(EXTRA_RESULT, ConnectionUtils.toStringValue(characteristic.getValue()));
		}
		context.sendBroadcast(intent);
		return;
	}
	
	
	/**
	 * Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered
	 * */
	//* 当发现设备包含的服务之后会回调此函数
	@Override
	public void  onServicesDiscovered(BluetoothGatt gatt, int status) {
		// TODO Auto-generated method stub
		super.onServicesDiscovered(gatt, status);
		if(debug)
			Log.i(TAG, "com.yeelight.blue.SDK.YeelightDevice ===> servicesDiscovered");
		device.setGatt(gatt);
		//for(BluetoothGattCharacteristic cBluetoothGattCharacteristic : gatt.getService(UUID.fromString(YeelightDevice.YEELIGHT_SERVICE)).getCharacteristics()){
	//		System.out.println(cBluetoothGattCharacteristic.getUuid().toString());
		//}
		device.setCharacteristics(gatt.getService(UUID.fromString(YeelightDevice.YEELIGHT_SERVICE)).getCharacteristics());
	//	Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
		synchronized (ConnectionManager.lock_connecte_devices_list) {//new Byte[]{0x1}) {
			ConnectionManager.connected_devices.add(device);
		}

        Log.i("EasyBlueControlService", "callback Sending DEVICE_CONNECTED: "
            + gatt.getDevice().getAddress());
		Intent intent = new Intent(DEVICE_CONNECTED);
		intent.putExtra(EXTRA_ADDRESS, gatt.getDevice().getAddress());
		context.sendBroadcast(intent);
		
	}
	
	/**
	 * Callback reporting the result of a characteristic read operation.
	 * */
	//读characteristic成功后会回调此方法
	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
		// TODO Auto-generated method stub
		super.onCharacteristicRead(gatt, characteristic, status);
		if(debug)
		if(status==0){
			Log.i(TAG, "Read from:"+characteristic.getUuid().toString()+" value:"+ConnectionUtils.bytesToString(characteristic.getValue()));
		}
	}
	
	/**
	 * Callback indicating the result of a characteristic write operation.
	 * If this callback is invoked while a reliable write transaction is in progress, the value of the characteristic represents the value reported by the remote device. <br>
	 * An application should compare this value to the desired value to be written. If the values don't match, the application must abort the reliable write transaction.
	 * */
//写入descriptor成功后会回调此函数 	
	@Override
	public void onDescriptorWrite(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
		// TODO Auto-generated method stub
		super.onDescriptorWrite(gatt, descriptor, status);
		System.out.println("descriptor:"+status);
	}
}
