package com.linhua.locky.bean;

import android.bluetooth.BluetoothDevice;

import java.util.Date;

/**
 * @author llw
 * @description BleDevice
 * @date 2021/7/21 19:20
 */
public class BleDevice {
    private BluetoothDevice device;
    private int rssi;
    private String realName;//真实名称

    private String bleId;
    private String deviceId;
    private Date lastSeen;
    private Boolean hasData;


    /**
     * 构造Device
     * @param device 蓝牙设备
     * @param rssi 信号强度
     * @param realName 真实名称
     * @param bleId
     * @param deviceId
     * @param lastSeen
     * @param hasData
     */
    public BleDevice(BluetoothDevice device, int rssi, String realName, String bleId, String deviceId, Date lastSeen, Boolean hasData) {
        this.device = device;
        this.rssi = rssi;
        this.realName = realName;
        this.bleId = bleId;
        this.deviceId = deviceId;
        this.lastSeen = lastSeen;
        this.hasData = hasData;
    }

    public BleDevice(BluetoothDevice device, int rssi, String realName) {
        this.device = device;
        this.rssi = rssi;
        this.realName = realName;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public int getRssi(){
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getRealName(){
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof BleDevice){
            final BleDevice that =(BleDevice) object;
            return device.getAddress().equals(that.device.getAddress());
        }
        return super.equals(object);
    }

    public String getBleId() {
        return bleId;
    }

    public void setBleId(String bleId) {
        this.bleId = bleId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Boolean getHasData() {
        return hasData;
    }

    public void setHasData(Boolean hasData) {
        this.hasData = hasData;
    }
}
