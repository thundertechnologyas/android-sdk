package com.thundertech.locky.utils;

import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * Ble
 * @author zhoushaolin
 * @description BleConstant
 * @date 2021/9/7 20:11
 */
public class BleConfig {

    public static final String SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";

    public static final String CHARACTERISTIC_WRITE_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";

    public static final String CHARACTERISTIC_READ_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

    public static final int scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY;

}
