package com.xdja.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice.DeviceState;

/***
 * ���ڹ���������pc��device�Լ�device��Ӧ��process �ײ��Ǻ�AndroidDeviceBridge���н���
 * 
 * @author zlw
 *
 */
public class DeviceManager {
	private Logger logger = Logger.getLogger(DeviceManager.class);
	private final static int TRY_COUNT = 20;
	
	// volatile ���ָֹ���������Ż�����ȡֵ�Ĳ��������ڸ�ֵ�Ĳ������֮��Ż�
	private volatile static DeviceManager mInstance;
	private DeviceStateListener mDeviceStateChange;
	private ArrayList<AndroidDevice> devices = new ArrayList<AndroidDevice>();

	private DeviceManager() {
		addDeviceChangeListener();
	}

	/**
	 * ��ȡDeviceManager��һ������
	 * 
	 * @return
	 */
	public static DeviceManager getInstance() {
		if (mInstance == null) {
			synchronized (DeviceManager.class) {
				if (mInstance == null) {
					mInstance = new DeviceManager();
				}
			}

		}
		return mInstance;
	}

	/***
	 * ���ڼ���Device ״̬�仯
	 * 
	 * @author zlw
	 *
	 */
	public interface DeviceStateListener {
		public void deviceDisconnected(AndroidDevice ad);

		public void deviceConnected(AndroidDevice idevice);
	}

	/**
	 * �����ⲿ�����������豸�ļ���
	 * 
	 * @param listener
	 */
	public void setDeviceStateListener(DeviceStateListener listener) {
		mDeviceStateChange = listener;
	}

	/**
	 * ���ص�ǰ���豸�б�
	 * 
	 * @return
	 */
	public ArrayList<AndroidDevice> getAndroidDevice() {
		return devices;
	}

	public void addAndroidDevice(AndroidDevice device) {
		if (!devices.contains(device)) {
			devices.add(device);
		}
	}

	/**
	 * ��AndroidDeviceBridge�������豸״���仯�ļ���
	 */
	private void addDeviceChangeListener() {
		AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {

			@Override
			public void deviceDisconnected(IDevice idevice) {
				for (AndroidDevice dev : devices) {
					if (dev.getDevice().getSerialNumber().equals(idevice.getSerialNumber())) {
						logger.info(String.format("device %s disconnected!", idevice.getSerialNumber()));
						mDeviceStateChange.deviceConnected(dev);
						break;
					}
				}
			}

			@Override
			public void deviceConnected(IDevice idevice) {
				if (idevice.isOnline()) {
					logger.info(String.format("device %s connected!", idevice.getSerialNumber()));
					AndroidDevice device = new AndroidDevice(idevice);
					mDeviceStateChange.deviceDisconnected(device);
				}
			}

			@Override
			public void deviceChanged(IDevice idevice, int i) {
				if (i != IDevice.CHANGE_STATE) {
					return;
				}
				DeviceState state = idevice.getState();
				AndroidDevice device = new AndroidDevice(idevice);
				if (state == DeviceState.ONLINE) {
					logger.info(String.format("device %s changed state: ONLINE", idevice.getSerialNumber()));
					mDeviceStateChange.deviceConnected(device);
				} else if (state == DeviceState.OFFLINE) {
					logger.info(String.format("device %s changed state: OFFLINE", idevice.getSerialNumber()));
					mDeviceStateChange.deviceDisconnected(device);
				}
			}
		});
	}

	/***
	 * ���ڷ����豸�ı�ʶ
	 * @param device �豸��װ
	 * @return �豸��_�豸��
	 */
	public String getDeviceName(AndroidDevice device) {
		String serialnum  = device.getDevice().getSerialNumber();
		String deviceName = device.getDevice().getName(); 
		if (serialnum == null || deviceName == null) {
			return "unknow";
		}
		String comboDevicesName = deviceName + "_" + serialnum;
		return comboDevicesName;
	}
	
	/**
	 * ����ȥ��ȡ���ӵ�pc���豸
	 * ��launchView��һ�δ�ʱȥ��ȡ
	 * @return
	 */
	public IDevice[] getDeviceListDirect() {
		AndroidDebugBridge.init(false);
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();
		//���ϳ��Ի�ȡ�豸
		int count = 0;
		while(!bridge.hasInitialDeviceList()){
			try {
				Thread.sleep(1000);
				count++;
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			if (count > TRY_COUNT) {
				break;
			}
		}
		
		IDevice devices[] = bridge.getDevices();
		return devices;
	}
}
