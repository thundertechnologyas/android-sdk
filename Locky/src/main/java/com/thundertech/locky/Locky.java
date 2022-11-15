package com.thundertech.locky;

import static com.thundertech.locky.callback.LockyEventCallback.EventType.*;
import static com.thundertech.locky.callback.PackageSignalType.PulseOpen;
import static com.thundertech.locky.utils.BleConfig.SERVICE_UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.thundertech.locky.api.ApiAuthManager;
import com.thundertech.locky.api.ApiManager;
import com.thundertech.locky.bean.BleDevice;
import com.thundertech.locky.bean.LockDevice;
import com.thundertech.locky.bean.LockModel;
import com.thundertech.locky.bean.LockyMobileKey;
import com.thundertech.locky.bean.LockyPackage;
import com.thundertech.locky.bean.TokenModel;
import com.thundertech.locky.ble.BleHelper;
import com.thundertech.locky.callback.BleCallback;
import com.thundertech.locky.callback.LockyDataCallback;
import com.thundertech.locky.callback.LockyEventCallback;
import com.thundertech.locky.callback.LockyListCallback;
import com.thundertech.locky.callback.LockyPermissionCallback;
import com.thundertech.locky.utils.BleConfig;
import com.thundertech.locky.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Date;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Locky {
    private final String TAG = "Locky";
    private final String domain = "mobilekey";
    private String token = "";
    private final String TokenKey = "locky_token";
    private ArrayList<LockModel> lockList;
    private long mobileKeyIndex;
    private final ArrayList<BleDevice> deviceList = new ArrayList<>();
    private final Handler hasDataHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<BleDevice> hasDataDeviceList = new ArrayList<>();
    private Boolean autoCollectBLEData = true;
    private int deltaTime = 0;
    private String email = "";

    private Boolean supportBluetooth = false;
    private BleCallback bleCallback;
    private BleCallback autoHasDataCallback;

    private BluetoothAdapter bluetoothAdapter;

    private ScanCallback scanCallback;
    private LockyListCallback<LockDevice> lockListCallback;
    private BluetoothGatt bluetoothGatt;

    private boolean isScanning = false;
    private final Context mContext;
    private LockyEventCallback mEventCallback;
    private LockyPermissionCallback mPermissionCallback;

    public Locky(Context context, LockyPermissionCallback permissionCallback) {
        mContext = context;
        mPermissionCallback = permissionCallback;
        init();
    }

    public Locky(Context context, LockyPermissionCallback permissionCallback, LockyEventCallback eventCallback) {
        mContext = context;
        mPermissionCallback = permissionCallback;
        mEventCallback = eventCallback;
        init();
    }

    private void init() {
        checkAndroidVersion();

        scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                if (result.getDevice().getName().startsWith("TT")) {
                    BleDevice bleDevice = new BleDevice();
                    bleDevice.setBleId(result.getDevice().getAddress());
                    bleDevice.setRssi(result.getRssi());
                    bleDevice.setDevice(result.getDevice());
                    bleDevice.setLastSeen(new Date());

                    bleDevice.setHasData(false);
                    ScanRecord record = result.getScanRecord();
                    byte[] bytes;
                    if (record != null) {
                        bytes = record.getBytes();
                        String command = ByteUtils.bytesToHexString(bytes);
                        if (command != null && command.length() >= 40) {
                            String advertiseStr = command.substring(10, 40);
                            String deviceId = advertiseStr.substring(6, 30);
                            if (mEventCallback != null) {
                                mEventCallback.postEvent(deviceId, DiscoveredDevice);
                            }
                            bleDevice.setDeviceId(deviceId);
                            String hasData = advertiseStr.substring(4, 6);
                            if (hasData.equals("02")) {
                                if (!hasDataDeviceList.contains(bleDevice)) {
                                    hasDataDeviceList.add(bleDevice);
                                    handleHasData();
                                    deltaTime += 3;
                                }
                            }
                        }
                    }
                    addDeviceList(bleDevice);
                }

            }

            @Override
            public void onScanFailed(int errorCode) {
                throw new RuntimeException("Scan error");
            }
        };
    }


    public Boolean isAuthenticated() {
        if (!token.isEmpty()) {
            return true;
        } else {
            token = PreferenceManager.getDefaultSharedPreferences(mContext).getString(TokenKey, "");
            return !token.isEmpty();
        }
    }


    public void startVerify(String emailText, LockyDataCallback<Boolean> callback) {
        String emailStr = emailText.trim();
        if (emailStr.isEmpty()) {
            return;
        }
        Call<Void> call = ApiAuthManager.getInstance().getHttpApi().startVerify(email, domain);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.v(TAG, "success");
                if (response.code() == 200) {
                    email = emailStr;
                    if (callback != null) {
                        callback.onSuccess(true);
                    }
                } else {
                    if (callback != null) {
                        callback.onFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.v(TAG, "error");
                callback.onFailure();
                // fail
            }
        });
    }

    public void verify(String code, LockyDataCallback<String> callback) {
        if (email.isEmpty() || code.isEmpty()) {
            return;
        }
        Call<TokenModel> call = ApiAuthManager.getInstance().getHttpApi().verify(email, code, domain);
        call.enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                TokenModel tokenModel = response.body();

                if (tokenModel != null && !tokenModel.getToken().isEmpty()) {
                    token = tokenModel.getToken();
                    PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(TokenKey, token).apply();
                    callback.onSuccess(token);
                } else {
                    callback.onSuccess("");
                }
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    public void getAllLocks(LockyListCallback<LockDevice> callback) {
        if (token.isEmpty()) {
            token = PreferenceManager.getDefaultSharedPreferences(mContext).getString(TokenKey, "");
            if (token.isEmpty()) {
                callback.onFailure();
                return;
            }
        }
        lockListCallback = callback;
        Call<ArrayList<String>> call = ApiAuthManager.getInstance().getHttpApi().getMobileKeys(domain, token);
        call.enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                ArrayList<String> keyList = response.body();
                if (keyList == null) {
                    callback.onSuccess(null);
                } else {
                    ArrayList<LockyMobileKey> keys = new ArrayList<>();
                    for (String item : keyList) {
                        if (item.length() > 24) {
                            String tenantId = item.substring(0, 24);
                            String token = item.substring(24);
                            keys.add(new LockyMobileKey(tenantId, token));
                        }
                    }
                    if (keys.size() > 0) {
                        ArrayList<LockModel> dataList = new ArrayList<>();
                        mobileKeyIndex = 0;
                        for (LockyMobileKey item : keys) {
                            getAllLockItem(item.getTenantId(), item.getToken(), new LockyListCallback<LockModel>() {
                                @Override
                                public void onSuccess(ArrayList<LockModel> response) {
                                    if (response != null) {
                                        dataList.addAll(response);
                                    }
                                    mobileKeyIndex++;
                                    if (mobileKeyIndex >= keys.size()) {
                                        handleLocks(dataList, callback);
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    mobileKeyIndex++;
                                    if (mobileKeyIndex >= keys.size()) {
                                        handleLocks(dataList, callback);
                                    }
                                }
                            });
                        }

                    } else {
                        callback.onSuccess(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    public void pulseOpen(String deviceId) {
        connectWriteDevice(deviceId, PulseOpen.toString());
    }

    private void handleLocks(ArrayList<LockModel> dataList, LockyListCallback<LockDevice> callback) {
        lockList = dataList;
        startScanDevice();
        ArrayList<LockDevice> items = new ArrayList<>();
        for (LockModel lock : dataList) {
            LockDevice device = new LockDevice();
            device.setId(lock.getId());
            device.setName(lock.getName());
            for (BleDevice ble: deviceList) {
                if (lock.getId().equals(ble.getDeviceId())) {
                    device.setHasBLE(true);
                }
            }
            items.add(device);
        }

        callback.onSuccess(items);
    }

    private void getAllLockItem(String tenantId, String token, LockyListCallback<LockModel> callback) {
        Call<ArrayList<LockModel>> call = ApiManager.getInstance().getHttpApi().getAllLocks(tenantId, token);
        call.enqueue(new Callback<ArrayList<LockModel>>() {
            @Override
            public void onResponse(Call<ArrayList<LockModel>> call, Response<ArrayList<LockModel>> response) {
                ArrayList<LockModel> dataList = response.body();
                for (LockModel lock : dataList) {
                    lock.setToken(token);
                    lock.setTenantId(tenantId);
                }
                callback.onSuccess(dataList);
            }

            @Override
            public void onFailure(Call<ArrayList<LockModel>> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    private void downloadPackage(String signal, String deviceId, String tenantId, String token, LockyDataCallback<LockyPackage> callback) {
        Call<LockyPackage> call = ApiManager.getInstance().getHttpApi().downloadPackage(signal, deviceId, tenantId, token);
        call.enqueue(new Callback<LockyPackage>() {
            @Override
            public void onResponse(Call<LockyPackage> call, Response<LockyPackage> response) {
                if (response.code() == 200) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<LockyPackage> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    private void messageDelivered(String deviceId, LockyPackage payload, String tenantId, String token, LockyDataCallback<Boolean> callback) {
        Call<Void> call = ApiManager.getInstance().getHttpApi().messageDelivered(deviceId, payload, "application/json", tenantId, token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    callback.onSuccess(true);
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void connectWriteDevice(String deviceId, String signal) {
        BluetoothDevice device = null;
        for (BleDevice ble : deviceList) {
            if (ble.getDeviceId().equals(deviceId)) {
                device = ble.getDevice();
                break;
            }
        }
        if (device == null) {
            return;
        }
        LockModel lockModel = null;
        for (LockModel lock : lockList) {
            if (lock.getId().equals(deviceId)) {
                lockModel = lock;
                break;
            }
        }
        if (lockModel == null) {
            return;
        }
        autoCollectBLEData = false;
        hasDataHandler.removeCallbacks(null);

        deltaTime = 0;
        bleCallback = new BleCallback();
        LockModel finalLockModel = lockModel;
        bleCallback.lockyBleCallBack = new BleCallback.LockyBleCallBack() {
            @Override
            public void onConnect() {
                if (mEventCallback != null) {
                    mEventCallback.postEvent(deviceId, DidConnectDevice);
                }
                writeDevice(signal, deviceId, finalLockModel.getTenantId(), finalLockModel.getToken());
            }

            @Override
            public void onRead(byte[] data) {
                if (mEventCallback != null) {
                    mEventCallback.postEvent(deviceId, DeliveringMessage);
                }
                String pack = Base64.encodeToString(data, Base64.NO_WRAP);
                LockyPackage payload = new LockyPackage();
                payload.setData(pack);
                messageDelivered(deviceId, payload, finalLockModel.getTenantId(), finalLockModel.getToken(), new LockyDataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean response) {
                        if (mEventCallback != null) {
                            mEventCallback.postEvent(deviceId, MessageDelivered);
                        }
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        };
        bluetoothGatt = device.connectGatt(mContext, false, bleCallback);

    }

    private void writeDevice(String signal, String deviceId, String tenantId, String token) {
        if (bluetoothGatt == null) {
            return;
        }
        resetAutoHasData();
        if (mEventCallback != null) {
            mEventCallback.postEvent(deviceId, DownloadPackage);
        }
        downloadPackage(signal, deviceId, tenantId, token, new LockyDataCallback<LockyPackage>() {
            @Override
            public void onSuccess(LockyPackage response) {
                if (mEventCallback != null) {
                    mEventCallback.postEvent(deviceId, WritingDevice);
                }
                String data = response.getData();
                byte[] command = Base64.decode(data, Base64.DEFAULT);
                BleHelper.sendCommand(bluetoothGatt, command);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * disconnect Device
     */
    @SuppressLint("MissingPermission")
    private void disconnectDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    /**
     * add to list
     *
     * @param bleDevice blue tooth
     */
    private void addDeviceList(BleDevice bleDevice) {
        if (!deviceList.contains(bleDevice)) {
            deviceList.add(bleDevice);
        } else {
            return;
        }
        ArrayList<LockDevice> items = new ArrayList<>();
        for (LockModel lock : lockList) {
            LockDevice device = new LockDevice();
            device.setId(lock.getId());
            device.setName(lock.getName());
            for (BleDevice ble: deviceList) {
                if (lock.getId().equals(ble.getDeviceId())) {
                    device.setHasBLE(true);
                }
            }
            items.add(device);
        }
        if (lockListCallback != null) {
            lockListCallback.onSuccess(items);
        }
    }

    /**
     * start scan
     */
    private void startScanDevice() {
        if (isScanning || !supportBluetooth)return;
        isScanning = true;
        deviceList.clear();

        final ArrayList<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SERVICE_UUID)).build();
        scanFilters.add(scanFilter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(BleConfig.scanMode)
                .build();
        BluetoothLeScannerCompat.getScanner().startScan(scanFilters, settings, scanCallback);
    }

    /**
     * stop scan
     */
    public void stop() {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
        if (bluetoothGatt != null) {
            disconnectDevice();
        }
        if (bleCallback != null) {
            bleCallback.lockyBleCallBack = null;
            bleCallback = null;
        }
        scanCallback = null;
        isScanning = false;
    }

    /**
     * check android version
     */
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android 6.0 above
            requestPermission();
        } else {
            openBluetooth();
        }
    }

    /**
     * request permission
     */
    private void requestPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,};
        if (EasyPermissions.hasPermissions(mContext, perms)) {
            openBluetooth();
        } else {
            if (mPermissionCallback != null) {
                mPermissionCallback.requestPermission(LockyPermissionCallback.PermissionType.NeedLocation);
            }
        }
    }

    /**
     * open bluetooth
     */
    private void openBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                supportBluetooth = true;
            } else {
                if (mPermissionCallback != null) {
                    mPermissionCallback.requestPermission(LockyPermissionCallback.PermissionType.NeedOpenBlueTooth);
                }
            }
        } else {
            if (mPermissionCallback != null) {
                mPermissionCallback.requestPermission(LockyPermissionCallback.PermissionType.PhoneNotSupport);
            }
        }
    }

    private void handleHasData() {
        if (!autoCollectBLEData || !isScanning || hasDataDeviceList.size() == 0) {
            return;
        }

        hasDataHandler.postDelayed(() -> {
            if (!autoCollectBLEData || !isScanning || hasDataDeviceList.size() == 0) {
                deltaTime = 0;
                return;
            }
            BleDevice device = hasDataDeviceList.get(0);
            autoCollectHasDataDevice(device.getDeviceId());

        }, deltaTime * 1000L);
    }

    @SuppressLint("MissingPermission")
    private void autoCollectHasDataDevice(String deviceId) {
        BluetoothDevice device = null;
        for (BleDevice ble : deviceList) {
            if (ble.getDeviceId().equals(deviceId)) {
                device = ble.getDevice();
                break;
            }
        }
        if (device == null) {
            return;
        }
        LockModel lockModel = null;
        for (LockModel lock : lockList) {
            if (lock.getId().equals(deviceId)) {
                lockModel = lock;
                break;
            }
        }
        if (lockModel == null) {
            return;
        }

        autoHasDataCallback = new BleCallback();
        LockModel finalLockModel = lockModel;
        autoHasDataCallback.lockyBleCallBack = new BleCallback.LockyBleCallBack() {
            @Override
            public void onConnect() {

            }

            @Override
            public void onRead(byte[] data) {
                if (hasDataDeviceList.size() > 0) {
                    hasDataDeviceList.remove(0);
                }
                if (hasDataDeviceList.size() == 0) {
                    deltaTime = 0;
                }
                String pack = Base64.encodeToString(data, Base64.NO_WRAP);
                LockyPackage payload = new LockyPackage();
                payload.setData(pack);
                messageDelivered(finalLockModel.getId(), payload, finalLockModel.getTenantId(), finalLockModel.getToken(), new LockyDataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean response) {

                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        };
        bluetoothGatt = device.connectGatt(mContext, false, autoHasDataCallback);
    }

    private void resetAutoHasData() {
        if (autoCollectBLEData ) {
            return;
        }

        hasDataHandler.postDelayed(() -> autoCollectBLEData = true, 5 * 1000);
    }
}
