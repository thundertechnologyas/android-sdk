package com.linhua.locky.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.linhua.locky.utils.AppMgr;
import com.linhua.locky.utils.BleConfig;
import com.linhua.locky.utils.ByteUtils;

import java.util.List;
import java.util.UUID;

/**
 * @author zhoushaolin
 * @description BleHelper
 * @date 2022/11/10 20:09
 */
public class BleHelper {

    /**
     * enable notification
     */
    public static boolean enableIndicateNotification(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(BleConfig.SERVICE_UUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID.fromString(BleConfig.CHARACTERISTIC_READ_UUID));
        return setCharacteristicNotification(gatt, gattCharacteristic);
    }

    /**
     * return true, if the write operation was initiated successfully
     */
    @SuppressLint("MissingPermission")
    private static boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic gattCharacteristic) {
        boolean isEnableNotification = gatt.setCharacteristicNotification(gattCharacteristic, true);
        if (isEnableNotification) {
            List<BluetoothGattDescriptor> descList = gattCharacteristic.getDescriptors();
            for (BluetoothGattDescriptor desc : descList) {
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean success = gatt.writeDescriptor(desc);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * send command
     * @param gatt gatt
     * @param command
     * @return
     */
    @SuppressLint("MissingPermission")
    public static boolean sendCommand(BluetoothGatt gatt, byte[] command) {
        BluetoothGattService service = gatt.getService(UUID.fromString(BleConfig.SERVICE_UUID));
        if (service == null) {
            Log.e("TAG", "sendCommand: service not found");
            return false;
        }
        //获取特性
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BleConfig.CHARACTERISTIC_WRITE_UUID));
        if (characteristic == null) {
            Log.e("TAG", "sendCommand: characteristic not found");
            return false;
        }

        //  WRITE_TYPE_DEFAULT  default with response， WRITE_TYPE_NO_RESPONSE no response
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        characteristic.setValue(command);
        boolean result = gatt.writeCharacteristic(characteristic);
        gatt.executeReliableWrite();
        Log.d("TAG", result ? "write successfully：" + command : "fail to write：" + command);
        return result;
    }
}
