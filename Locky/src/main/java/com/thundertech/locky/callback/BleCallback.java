package com.thundertech.locky.callback;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import com.thundertech.locky.ble.BleHelper;
import com.thundertech.locky.utils.ByteUtils;

/**
 * @author zhoushaolin
 * @description BleCallback
 * @date 2022/10/31 20:51
 */


public class BleCallback extends BluetoothGattCallback {

    public interface LockyBleCallBack {
        void onConnect();
        void onRead(byte[] data);
    }

    private static final String TAG = BleCallback.class.getSimpleName();

    public LockyBleCallBack lockyBleCallBack;

    /**
     * onConnectionStateChange
     *
     * @param gatt     gatt
     * @param status   gatt status
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "connect successfully");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "fail to connect");
                    break;
                default:
                    break;
            }
        } else {
            Log.e(TAG, "onConnectionStateChange: " + status);
        }
    }

    /**
     * onServicesDiscovered
     * @param gatt   gatt
     * @param status gatt status
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered");
        boolean notifyOpen = BleHelper.enableIndicateNotification(gatt);
        if (!notifyOpen) {
            Log.e(TAG, "fail to open notify");

            gatt.disconnect();
            return;
        }
        if (lockyBleCallBack != null) {
            lockyBleCallBack.onConnect();
        }
    }

    /**
     * onCharacteristicWrite
     *
     * @param gatt           gatt
     * @param status         gatt status
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        String command = ByteUtils.bytesToHexString(characteristic.getValue());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicWrite success：" + command);
        } else {
            Log.d(TAG, "onCharacteristicWrite failure：" + command);
        }
    }

    /**
     * onCharacteristicChanged
     * @param gatt           gatt
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        String content = ByteUtils.bytesToHexString(characteristic.getValue());
        if (data.length > 0) {
            if (lockyBleCallBack != null) {
                lockyBleCallBack.onRead(data);
            }
        }
        Log.d(TAG, "onCharacteristicChanged: receive content：" + content);
    }

    @Override
    public void onCharacteristicRead(final BluetoothGatt gatt,
                                     final BluetoothGattCharacteristic characteristic,
                                     final int status) {
    }

}

