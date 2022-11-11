package com.linhua.locky.bean;

import android.bluetooth.BluetoothDevice;

import java.util.Date;

/**
 * @author zhoushaolin
 * @description BleDevice
 */
public class BleDevice {
    private BluetoothDevice device;
    private int rssi;

    private String bleId;
    private String deviceId;
    private Date lastSeen;
    private Boolean hasData;

    public BleDevice() {

    }

    public BleDevice(BluetoothDevice device, int rssi, String bleId, String deviceId, Date lastSeen, Boolean hasData) {
        this.device = device;
        this.rssi = rssi;
        this.bleId = bleId;
        this.deviceId = deviceId;
        this.lastSeen = lastSeen;
        this.hasData = hasData;
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

    @Override
    public boolean equals(Object object) {
        if(object instanceof BleDevice){
            final BleDevice that =(BleDevice) object;
            return this.getDeviceId().equals(that.getDeviceId());
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
