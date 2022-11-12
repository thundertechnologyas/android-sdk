package com.linhua.locky.callback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.linhua.locky.utils.AppMgr;
import com.linhua.locky.utils.BleConfig;
import com.linhua.locky.ble.BleHelper;
import com.linhua.locky.utils.ByteUtils;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

/**
 * @author zhoushaolin
 * @description BleCallback
 * @date 2022/10/31 20:51
 */


public class BleCallback extends BluetoothGattCallback {

    public interface LockyBleCallBack {
        void onConnect();
        void onRead(String data);
    }

    private static final String TAG = BleCallback.class.getSimpleName();

    public LockyBleCallBack lockyBleCallBack;

    /**
     * onConnectionStateChange
     *
     * @param gatt     gatt
     * @param status   gatt status
     * @param newState
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, Thread.currentThread().getName());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "connect successfully");
                    gatt.discoverServices();
                    //获取MtuSize
//                    gatt.requestMtu(512);
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
     *
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
     * @param characteristic
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
     *
     * @param gatt           gatt
     * @param characteristic
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String content = ByteUtils.bytesToHexString(characteristic.getValue());
        if (content.length() > 0) {
            if (lockyBleCallBack != null) {
                lockyBleCallBack.onRead(content);
            }
        }
        Log.d(TAG, "onCharacteristicChanged: receive content：" + content);
    }

    /**
     * onDescriptorRead
     *
     * @param gatt       gatt
     * @param descriptor
     * @param status     gatt status
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    /**
     * onDescriptorWrite
     *
     * @param gatt       gatt
     * @param descriptor
     * @param status     gatt status
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status == GATT_SUCCESS) {
            Log.d(TAG, "onDescriptorWrite: success to start notification");
        } else {
            Log.d(TAG, "onDescriptorWrite: fail to change notification");
        }
    }

    /**
     *
     * @param gatt   gatt
     * @param status gatt status
     */
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onReliableWriteCompleted: Reliable Write");
    }

    @Override
    public void onCharacteristicRead(final BluetoothGatt gatt,
                                     final BluetoothGattCharacteristic characteristic,
                                     final int status) {
    }

}

