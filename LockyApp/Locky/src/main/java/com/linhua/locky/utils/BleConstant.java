package com.linhua.locky.utils;

/**
 * Ble常量
 * @author llw
 * @description BleConstant
 * @date 2021/9/7 20:11
 */
public class BleConstant {

    /**
     * 服务 UUID
     */

//      6E400001-B5A3-F393-E0A9-E50E24DCCA9E ( service_uuid )
//      6E400002-B5A3-F393-E0A9-E50E24DCCA9E ( characteristic_uuid - Transmit data )
//      6E400003-B5A3-F393-E0A9-E50E24DCCA9E ( chara

    public static final String SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    /**
     * 特性写入 UUID
     */
    public static final String CHARACTERISTIC_WRITE_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    /**
     * 特性读取 UUID
     */
    public static final String CHARACTERISTIC_READ_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    /**
     * 描述 UUID
     */
    public static final String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
}
