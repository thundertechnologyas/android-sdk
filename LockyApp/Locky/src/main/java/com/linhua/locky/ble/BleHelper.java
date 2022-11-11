package com.linhua.locky.ble;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.linhua.locky.utils.AppMgr;
import com.linhua.locky.utils.BleConfig;
import com.linhua.locky.utils.ByteUtils;

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
        //获取Gatt 服务
        BluetoothGattService service = gatt.getService(UUID.fromString(BleConfig.SERVICE_UUID));
        if (service == null) {
            return false;
        }
        //获取Gatt 特征（特性）
        BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID.fromString(BleConfig.CHARACTERISTIC_READ_UUID));
        return setCharacteristicNotification(gatt, gattCharacteristic);
    }

    /**
     * return true, if the write operation was initiated successfully
     */
    private static boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic gattCharacteristic) {
        //如果特性具备Notification功能，返回true就代表设备设置成功
        if (ActivityCompat.checkSelfPermission(AppMgr.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        boolean isEnableNotification = gatt.setCharacteristicNotification(gattCharacteristic, true);
        if (isEnableNotification) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * send command
     * @param gatt gatt
     * @param command
     * @param isResponse
     * @return
     */
    public static boolean sendCommand(BluetoothGatt gatt, byte[] command, boolean isResponse) {
        //获取服务
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
        characteristic.setWriteType(isResponse ?
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT : BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(command);
        if (ActivityCompat.checkSelfPermission(AppMgr.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        boolean result = gatt.writeCharacteristic(characteristic);
        //执行可靠写入
        gatt.executeReliableWrite();
        Log.d("TAG", result ? "write successfully：" + command : "fail to write：" + command);
        return result;
    }
}
