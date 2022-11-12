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

import androidx.core.app.ActivityCompat;

/**
 * @author zhoushaolin
 * @description BleCallback
 * @date 2022/10/31 20:51
 */


public class BleCallback extends BluetoothGattCallback {

    public interface LockyBleCallBack {
        void onConnect();
        void onWrite();
    }

    private static final String TAG = BleCallback.class.getSimpleName();

    public LockyBleCallBack lockyBleCallBack;

    /**
     * 连接状态改变回调
     *
     * @param gatt     gatt
     * @param status   gatt连接状态
     * @param newState 新状态
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, Thread.currentThread().getName());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED://连接成功
                    Log.d(TAG, "连接成功");
                    gatt.discoverServices();
                    //获取MtuSize
//                    gatt.requestMtu(512);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED://断开连接
                    Log.e(TAG, "断开连接");
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
     * @param txPhy  1M 2M
     * @param rxPhy  1M 2M
     * @param status
     */
    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        Log.d(TAG, "onPhyUpdate: txPhy: " + txPhy + " rxPhy: " + rxPhy);
    }

    /**
     *
     * @param gatt   gatt
     * @param txPhy   1M 2M
     * @param rxPhy   1M 2M
     * @param status
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        Log.d(TAG, "onPhyRead: txPhy：" + txPhy + " rxPhy：" + rxPhy);
        if (txPhy == BluetoothDevice.PHY_LE_1M && rxPhy == BluetoothDevice.PHY_LE_1M) {
            //获取2M的发送和接收速率
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M,
                        BluetoothDevice.PHY_OPTION_NO_PREFERRED);
            }
        }
    }

    /**
     *
     * @param gatt   gatt
     * @param status gatt状态
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
     * 特性读取回调
     *
     * @param gatt           gatt
     * @param characteristic 特性
     * @param status         gatt状态
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead: characteristic: " + characteristic.getUuid().toString());
    }

    /**
     * 特性写入回调
     *
     * @param gatt           gatt
     * @param characteristic 特性
     * @param status         gatt状态
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        String command = ByteUtils.bytesToHexString(characteristic.getValue());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicWrite: 写入成功：" + command);
        } else {
            Log.d(TAG, "onCharacteristicWrite: 写入失败：" + command);
        }

        Log.d(TAG, "onCharacteristicChanged: 读取特性 " + gatt.readCharacteristic(characteristic));
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
        Log.d(TAG, "onCharacteristicChanged: receive content：" + content);

        Log.d(TAG, "onCharacteristicChanged: read  characteristic" + gatt.readCharacteristic(characteristic));
    }

    /**
     * 描述符获取回调
     *
     * @param gatt       gatt
     * @param descriptor 描述符
     * @param status     gatt状态
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorRead: descriptor: " + descriptor.getUuid().toString());
    }

    /**
     * 描述符写入回调
     *
     * @param gatt       gatt
     * @param descriptor 描述符
     * @param status     gatt状态
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status == GATT_SUCCESS) {
            Log.d(TAG, "onDescriptorWrite: success to start notification");
            //获取phy
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.readPhy();
            }
            //读取描述符
            gatt.readDescriptor(descriptor);
            //读取RSSI
            gatt.readRemoteRssi();
        } else {
            Log.d(TAG, "onDescriptorWrite: fail to change notification");
        }

    }

    /**
     *
     * @param gatt   gatt
     * @param status gatt状态
     */
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onReliableWriteCompleted: Reliable Write");
    }

    /**
     *
     * @param gatt   gatt
     * @param rssi   信号强度
     * @param status gatt状态
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.d(TAG, "onReadRemoteRssi: rssi: " + rssi);
    }

    /**
     *
     * @param gatt   gatt
     * @param mtu    new MTU size
     * @param status gatt状态
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Log.d(TAG, "onMtuChanged：mtu： " + mtu);
        gatt.discoverServices();
    }

}

