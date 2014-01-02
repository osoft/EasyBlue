package com.yeelight.blue.SDK;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

/**
 * Represents Yeelight device.The {@link YeelightDevice}  can read and write equipment directly through the API18+.
 * 
 */
//Yeelight的实体类主要用于Yeelight对象的表示 包括Yeelight用到的服务UUID、特征UUID BluetoothDevice对象
@TargetApi(18)
public class YeelightDevice {
	/**
	 * define 
	 */

	/**
	
	 * Used as a String extra field in {@link ConnectionManager#DEVICE_FOUND} intents.
	 * 
	 */
	// * 用与广播中查找rssi的Key
	public static final String EXTRA_RSSI = "rssi";
	
	/**
	 * 
	 * Used as a Parcelable BluetoothDevice extra field in {@link ConnectionManager#DEVICE_FOUND} intents.
	 * 
	 */
	//* 用与广播中查找BluetoothDeivce对象的Key
	public static final String EXTRA_DEVICE = "device";
	
	
	/**
	 * Device gradual : STOP. In this mode the device will has no gradual.
	 * */
	public static final int SHADE_STOP =  1;
	
	/**
	 * Device gradual : START. In this mode the device will has a smooth-gradient.
	 * */
	
	public static final int SHADE_START = 2;
	
	/**
	 * Device gradual : HALF. In this mode the brightnes of the device will quickly drop to 5%.
	 * */
	
	public static final int SHADE_HALF =  3;
	
	
	/**
	 * 
	 * 
	 * */
	public static final int STATE_ON = 1; 
	public static final int STATE_OFF = 0;
	
	/**
	 * 
	 * The UUID of Yeelight Service
	 * 
	 */
	// * Yeelight所用的BLE服务UUID，用于查找Alight服务
	public static final String YEELIGHT_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";

	/**
	
	 * 
	 * 
	 * This characteristic control brightness and color of the device.<br>
	 * 
	 * <b>Length:</b>18.<br>
	 * <b>TYPE:</b>R/W<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * R,G,B,L,...(note：fill with comma)<br>
	 * 
	 * R : Number , 0~255 , Red component;<br>
	 * G : Number , 0~255 , Green component; <br>
	 * B : Number , 0~255 , Blue component;<br>
	 * L : Number , 0~100 , brightness;<br>
	 * 
	 */
	/* 控制调光调色的UUID，命令长度18位 ,可读写。
	 * 该特性用来控制灯的状态（开、关、变颜色、变亮度），其数据格式如下：
	 *  R,G,B,L,…（注：所有数据长度不足一致补逗号） 
	 * 参数详解： <br>
	 * R : 数字, 0~255, 红色分量;<br> 
	 * G : 数字, 0~255, 绿色分量; <br>
	 * B : 数字, 0~255, 蓝色分量;<br>
	 * L : 数字, 0~100, 亮度;<br>*/
	public static final String CHARACTERISTIC_CONTORL = "0000fff1-0000-1000-8000-00805f9b34fb";

	/**
	 * 
	 * This characteristic control delay of the device.<br>
	 * 
	 *  
	 *  Length:8. R/W.<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 *  T,S,...(note:fill with comma)<br>
	 *  
	 * T :Number,the time to delay.Fundamental Unit For Minute; 0 is special,used to cancel the delay; <br> 
	 * S :Number, case:0 – delay off;case:1 – delay on.
	 *  
	 *  
	 */
	/*	 * 控制延时的UUID，命令长度8位，可读写 
	 * 该特性用来设置延时开关设置消息, 其数据格式如下：<br>
	 *  T,S,…（注：所有数据长度不足一致补逗号）<br>
	 * 参数详解：<br>
	 * T :数字,表示延迟开关的时间, 单位为分钟; 0为特殊用途, 用作取消设置; <br> 
	 * S:数字, 为0 – 延时关灯, 1 – 延时开灯;
*/
	public static final String CHARACTERISTIC_DELAY = "0000fff2-0000-1000-8000-00805f9b34fb";

	/**
	
	 * 
	 * This characteristic use to query the delay status of the device.
	 * 
	 * <br>Length:2.R/W<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * 
	 * RT<br>
	 * 
	 * Fixed fromat.Send "RT" to this characteristic,you will receive a Notification {@link #CHARACTERISTIC_DELAY_NOTIFICATION}
	 * 
	 * 
	 */
	/* * 查询延时状态UUID，命令长度2位，可读写
	 * 该特性用来查询当前延时开关状态, 其数据格式如下<br>
	 * RT
	 * 固定格式 ，向此characteristic发送“RT”命令，会收到Notification
	 * */
	public static final String CHARACTERISTIC_DELAY_QUERY = "0000fff3-0000-1000-8000-00805f9b34fb";
	
	
	/**
     * 
     * This characteristic use to return the information of delay status.
     * 
     * <br>Length:10.R/W<br>
     *  
     * The format of this characteristic is as follows:<br>
     * 
     * RTB T,S,...(note:fill with comma)<br>
     * 
     * T : Number,the time to delay.Fundamental Unit For Minute.0 means no delay setted.<br>
     * 
     * S : Number,case:0 - delay off;case:1 - means delay on
     * 
     * 
     * 
	 */
	/* 查询延时状态查询回传通知UUID，数据长度10位，通知
	 * 该特性用来回传当前延时开关状态, 其数据格式如下
	 * RTB T,S,… （注：所有数据长度不足一致补逗号） <br>
	 * 参数详解： <br>
     * T :数字, 表示延迟开关的时间, 单位分钟了; 0表示未设置延时开关;<br> 
     * S:数字, 为0 – 延时关灯, 1 – 延时开灯;<br>*/
	public static final String CHARACTERISTIC_DELAY_NOTIFICATION = "0000fff4-0000-1000-8000-00805f9b34fb";
	
	
	/**
	 * 
	 * This characteristic use to query the status of the device.
	 * 
	 * 
	 * <br>Length:1.R/W<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * 
	 * S<br>
	 * 
	 * Fixed fromat.Send "S" to this characteristic,you will receive a Notification {@link #CHARACTERISTIC_STATE_NOTIFICATION}
	 * 
	 */
	/*查询灯状态UUID，数据长度1位，可读写
	 * 该特性用来查询灯光状态, 其数据格式如下： <br>j
     *   S <br>
	 * 固定格式 ，向此characteristic发送“S”命令，会收到Notification<br>
	 * */
	public static final String CHARACTERISTIC_STATUS_QUERY = "0000fff5-0000-1000-8000-00805f9b34fb";
	
	/**
	
	 * This characteristic use to return the  information of device state. Length:20. Notification.<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * R,G,B,L,CF,DL,...(note：fill with comma)<br>
	 * 
	 * R : Number , 0~255 , Red component;<br>
	 * G : Number , 0~255 , Green component; <br>
	 * B : Number , 0~255 , Blue component;<br>
	 * L : Number , 0~100 , brightness;<br>
	 * CF: Number , case 0:Colorflow is running; case 1:colorflow has stoped
	 * DL: Number , case 0:delay switch is already open; case 1:delay switch has closed  
	 */
	/* * 灯光状态查询回传通知UUID，数据长度20位，通知
	 * 该特性用来返回当前灯光状态, 其数据格式如下： <br>
     * R,G,B,L,CF,DL,… （注：所有数据长度不足一致补逗号） <br>
	 * 参数详解： <br>
	 * R: 数字, 0~255, 红色分量; <br>
	 * G : 数字, 0~255, 绿色分量; <br>
	 * B : 数字, 0~255, 蓝色分量; <br>
	 * L: 数字, 0~100, 亮度; <br>
	 * CF:     数字, 1：color flow 进行状态； 0：color flow 停止状态 <br>
	 * DL : 数字，1：延时开关进行状态； 0：延时开关停止状态 <br>
	 * */
	public static final String CHARACTERISTIC_STATE_NOTIFICATION = "0000fff6-0000-1000-8000-00805f9b34fb";
	
	/**
	 * This characteristic control colorflow of the device. Length:20. R/W.<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * N,R,G,B,L,T,...(note：fill with comma)<br>
	 * -
	 * N : Number , 0~9   , index of color<br>
	 * R : Number , 0~255 , Red component;<br>
	 * G : Number , 0~255 , Green component; <br>
	 * B : Number , 0~255 , Blue component;<br>
	 * L : Number , 0~100 , brightness;<br>
	 * T : Number , 0~255 , Fundamental Unit For Seconds,the time to show;<br>
	 * 
	 *  You will first need to set data for colorflow,then you can call {@link ConnectionManager#startColorFlow()} to start colorflow.<br>
	 *    
	 */
	/*
	 ** 用于设置ColorFlow的UUID，数据长度20位，可读写
	 * 该特性用来设置color flow 的属性、开始和结束，其数据格式如下： <br>
	 * N,R,G,B,L,T,…  （设置color flow 属性） <br>
	 * CB,…                            （color flow 开始） <br>
	 * CE,…                            （color flow 结束） <br>
	 *（注：所有数据长度不足一致补逗号） <br>
	 * 参数详解： <br>
	 * N : 数字，0~9，颜色渐变序列号 <br>
	 * R: 数字, 0~255, 红色分量; <br>
	 * G : 数字, 0~255, 绿色分量; <br>
	 * B : 数字, 0~255, 蓝色分量; <br>
	 * L: 数字, 0~100, 亮度; <br>
	 * T : 数字, 0~255, 时间;<br>
	 * 
	 * 首先设置ColorFlow的属性 然后发送CB命令开始变色。发送CE命名停止变色<br>
	 * */
	public static final String CHARACTERISTIC_COLORFLOW = "0000fff7-0000-1000-8000-00805f9b34fb";
	
	/**
	
	 * This characteristic use to query the status of the colorflow.Length:2.R/W<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * 
	 * CF<br>
	 * 
	 * Fixed fromat.Send "CF" to this characteristic,you will receive a Notification {@link #CHARACTERISTIC_COLORFLOW_NOTIFICATION}
	 */
	/*
	 *  * 查询ColorFlow状态UUID，数据长度2位，可读写
	 * 该特性用来查询ColorFlow状态, 其数据格式如下： <br>
     *   CF <br>
	 * 固定格式 ，向此characteristic发送“CF”命令，会收到Notification<br>*/
	public static final String CHARACTERISTIC_COLORFLOW_QUERY = "0000fffa-0000-1000-8000-00805f9b34fb";
	
	/**
	 *  This characteristic use to return the information of colorflow.Length:20.Notification<br>
     *  
     * The format of this characteristic is as follows:<br>
     * 
     * N,R,G,B,L,T,...(note：fill with comma)<br>
	 * 
	 * N : Number , 0~9   , index of color<br>
	 * R : Number , 0~255 , Red component;<br>
	 * G : Number , 0~255 , Green component; <br>
	 * B : Number , 0~255 , Blue component;<br>
	 * L : Number , 0~100 , brightness;<br>
	 * T : Number , 0~255 , Fundamental Unit For Seconds,the time to show;<br>
	 */
	/*ColorFlow状态查询回传通知UUID，数据长度20位，通知
	 * 该特性用来返回当前color flow 的状态，其数据格式如下：<br> 
	 * N,R,G,B,L,T,… （注：所有数据长度不足一致补逗号） <br>
	 * 参数详解： <br>
	 * N : 数字，0~9，颜色渐变序列号 <br>
	 * R: 数字, 0~255, 红色分量; <br>
	 * G : 数字, 0~255, 绿色分量; <br>
	 * B : 数字, 0~255, 蓝色分量; <br>
	 * L: 数字, 0~100, 亮度; <br>
	 * T : 数字, 0~255, 时间;<br>*/
	public static final String CHARACTERISTIC_COLORFLOW_NOTIFICATION = "0000fffb-0000-1000-8000-00805f9b34fb";
	
	
	/**
	 *   This characteristic control gradual mode of the device.Length:2.R/W<br>
	 * 
	 * The format of this characteristic is as follows:<br>
	 * 
	 * TS , TE or TH<br>
	 * 
	 * TS : open the gradient. In this mode device will have a very smooth color changing;<br>
	 * TE : close the gradient. In this mode device will have no gradient when change color;<br>
	 * TH : In this mode the brightnes of the device will quickly drop to 5%;<br>
	 * 
	 * Fixed fromat.The mode will set "TS" each time the device powers up.<br>
	 */
	/*
	 * * 设置渐变是否可用UUID，数据长度2位，可读写
	 * 该特性用来控制灯在颜色亮度发生变化时，是否有渐变效果，其数据格式如下： <br>
	 *   TS  或者      TE  或者   TH<br>
	 * 参数详解： <br>
	 * TS :开启渐变效果 （程序上电默认开启渐变） <br>
	 * TE :关闭渐变效果<br>
	 * TH :迅速由当前亮度渐变到亮度为5的状态<br>
	 * */
	public static final String CHARACTERISTIC_GRADUAL = "0000fffc-0000-1000-8000-00805f9b34fb";
	
	
	
	
	public static final String CHARACTERISTIC_INFO = "0000fffd-0000-1000-8000-00805f9b34fb";
	
	
	
	 /*
	  *用于配置标准Notification的UUID ，Descriptor需要根据这个UUID获取。 
	  */
	
	public static final String NOTIFICATION_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String TAG = "EasyBlueControlService";

    //蓝牙设备address
	private String address;
	
	//
	private BluetoothDevice device;
	
	//保存连接后的gatt对象
	private BluetoothGatt gatt;
	
	//保存所有特征
	private List<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();
	
	//由于发送广播
	private Context context;
	
	//连接回调
	private YeelightCallBack callBack;
	
	private BluetoothAdapter adapter;
	 
	/**
	 * 
	 * */
	public YeelightDevice(Context context,String address,BluetoothDevice device,BluetoothAdapter adapter) {
		this.context = context;
		callBack = new YeelightCallBack(context,this);
		this.address = address;
		this.device = device;
		this.adapter = adapter;
	}
	
	/**
	 * Connect  device;
	 * */
	public void connect(){
        Log.i(TAG, "connecting addr:  " + address);
		device   = adapter.getRemoteDevice(address);
		if(gatt!=null){
			gatt.connect();
		}else{
			device.connectGatt(context, false, callBack);
		}
	}
	
	
	/**
	 * Disconnect
	 * */
	public void disconnect(){
		gatt.disconnect();
		gatt = null;
	}
	
	/**
	 * Return if the device is connceted;
	 * 
	 * @return true if device has connceted.
	 * 
	 * */
	public boolean isConnected(){
		return gatt==null;
	}
	
	
	/**
	 * Write data to characteristic.
	 * 
	 * @param uuid a given characteristic 
	 * @param data value  
	 * */
	public void write(String uuid,String data){
		if(gatt==null){
            Log.w(TAG, "write: gatt==null");
			return;
		}
		BluetoothGattCharacteristic characteristic = null;
		for(int i=0;i<characteristics.size();i++){
			if(characteristics.get(i).getUuid().toString().equals(uuid)){
				characteristic = characteristics.get(i);
				break;
			}
			
		}
		if(characteristic!=null){
			characteristic.setValue(data.getBytes());
			boolean res = gatt.writeCharacteristic(characteristic);
            if (!res) {
                Log.w(TAG, "write: write fail");
            }
		} else {
            Log.w(TAG, "write: characteristic==null");
        }
		
	}
	
	/*
	 * 设置蓝牙Adapter
	 * */
	public void setAdapter(BluetoothAdapter adapter) {
		this.adapter = adapter;
	}
	
	/**
	 *  
	 *  The characteristics which belongs to service fff0;
	 * 
	 * */
	public void setCharacteristics(
			List<BluetoothGattCharacteristic> characteristics) {
		this.characteristics = characteristics;
	}
	
	
	/**
	 * 
	 * Set gradual mode. 
	 * @param mode One of {@link YeelightDevice#SHADE_START} ;{@link YeelightDevice#SHADE_STOP} ; {@link YeelightDevice#SHADE_HALF}
	 * */
	public void setGradual(int mode){
		switch (mode) {
		case SHADE_START:
			write(CHARACTERISTIC_GRADUAL, "TS");
			break;
		case SHADE_STOP:
			write(CHARACTERISTIC_GRADUAL, "TE");
			break;
		case SHADE_HALF:
			write(CHARACTERISTIC_GRADUAL, "TH");
			break;
		default:
			write(CHARACTERISTIC_GRADUAL, "TS");
			break;
		}
	}
	
	
	/**
	 * Set BluetoothGatt
	 * 
	 * */
	public void setGatt(BluetoothGatt gatt) {
		this.gatt = gatt;
	}
	
	/**
	 * Get MAC address
	 * @return MAC address
	 * */
	public String getAddress() {
		return address;
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o==null){
			return false;
		}
		if(o==this){
			return true;
		}
		if(o instanceof YeelightDevice){
			YeelightDevice d = (YeelightDevice) o;
			return d.getAddress().equals(address);
		}
		
		return false;
	}
	
	// characteristic
	// public static final String YEELIGHT_SERVICE =
	// "0000fff0-0000-1000-8000-00805f9b34fb"; // very important to get this
	// right. Discover characteristic will fail otherwise.
	/*public static final String UUID_CONTROL = "0000fff1-0000-1000-8000-00805f9b34fb";
	public static final String UUID_DELAY = "0000fff2-0000-1000-8000-00805f9b34fb";
	public static final String UUID_DELAY_QUERY = "0000fff3-0000-1000-8000-00805f9b34fb";
	public static final String UUID_DELAY_NOTIFICATION = "0000fff4-0000-1000-8000-00805f9b34fb";
	
	public static final String UUID_STATE_NOTIFICATION = "0000fff6-0000-1000-8000-00805f9b34fb";
	public static final String UUID_COLORFLOW = "0000fff7-0000-1000-8000-00805f9b34fb";
	public static final String UUID_NAME = "0000fff8-0000-1000-8000-00805f9b34fb";
	public static final String UUID_NAME_NOTIFICATION = "0000fff9-0000-1000-8000-00805f9b34fb";
	public static final String UUID_QUERY_COLORFLOW = "0000fffa-0000-1000-8000-00805f9b34fb";
	public static final String UUID_COLORFLOW_NOTIFICATION = "0000fffb-0000-1000-8000-00805f9b34fb";
	public static final String UUID_SHADOW = "0000fffc-0000-1000-8000-00805f9b34fb";
	public static final String UUID_INFO = "0000fffd-0000-1000-8000-00805f9b34fb";
	public static final String UUID_PWD = "0000fffe-0000-1000-8000-00805f9b34fb";
	public static final String UUID_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";*/
	
	
	/**
	 * Enable or disable notifications for a given characteristic. 
	 * 
	 * @param enable Set to true to enable notifications/indications
	 * @param characteristic  The characteristic for which to enable notifications
	 * */
	public  boolean enableNotification(boolean enable,BluetoothGattCharacteristic characteristic) {
		
		boolean flag =false;
		gatt.setCharacteristicNotification(characteristic, enable);
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(NOTIFICATION_CONFIG));
		if(descriptor!=null){
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		    flag =	gatt.writeDescriptor(descriptor);
		}
		return flag;
	}
	
	
	/**
	 * Get BlueToothGattCharacteristic Object by a given UUID.
	 * @param uuid a given uuid
	 * @return GATT characteristic object or null if no characteristic with the given UUID was found. 
	 * 
	 * */
	public BluetoothGattCharacteristic getCharacteristic(String uuid){
		for(BluetoothGattCharacteristic characteristic : characteristics){
			if(characteristic.getUuid().toString().equals(uuid)){
				return characteristic;
			};
		}
		
		return null;
		
	}
	public void readRemoteRSSI(){
		gatt.readRemoteRssi();
	}
}
