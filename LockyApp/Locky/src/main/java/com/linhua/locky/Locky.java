package com.linhua.locky;

import static com.linhua.locky.utils.AppMgr.context;
import static com.linhua.locky.utils.BleConfig.SERVICE_UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.linhua.locky.api.ApiAuthManager;
import com.linhua.locky.api.ApiManager;
import com.linhua.locky.bean.BleDevice;
import com.linhua.locky.bean.LockDevice;
import com.linhua.locky.bean.LockModel;
import com.linhua.locky.bean.LockyMobileKey;
import com.linhua.locky.bean.LockyPackage;
import com.linhua.locky.bean.TokenModel;
import com.linhua.locky.ble.BleHelper;
import com.linhua.locky.callback.LockyDataCallback;
import com.linhua.locky.callback.LockyListCallback;
import com.linhua.locky.utils.BleConfig;
import com.linhua.locky.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Date;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Locky {
    private final String TAG = "Locky";
    private final String domain = "mobilekey";
    private String token = "";
    private final String TokenKey = "locky_token";
    private ArrayList<LockyMobileKey> mobileKeys;
    private ArrayList<LockModel> lockList;
    private long mobileKeyIndex;
    private BleHelper bleHelper;
    private ArrayList<BleDevice> deviceList = new ArrayList<>();
    private Boolean supportBluetooth = false;

    private static final int REQUEST_ENABLE_BLUETOOTH = 100;

    public static final int REQUEST_PERMISSION_CODE = 9527;

    private BluetoothAdapter bluetoothAdapter;

    private ScanCallback scanCallback;
    private LockyListCallback lockListCallback;

    /**
     * Gatt
     */
    private BluetoothGatt bluetoothGatt;

    private boolean isConnected = false;
    private boolean isScanning = false;

    public Locky() {
        bleHelper = new BleHelper();
        checkAndroidVersion();
        //扫描结果回调
        scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                //添加到设备列表
                if (result.getDevice().getName().startsWith("TT")) {
                    BleDevice bleDevice = new BleDevice();
                    bleDevice.setBleId(result.getDevice().getAddress());
                    bleDevice.setRssi(result.getRssi());
                    bleDevice.setLastSeen(new Date());

                    bleDevice.setHasData(false);
                    ScanRecord record = result.getScanRecord();
                    byte[] bytes = null;
                    if (record != null) {
                        bytes = record.getBytes();
                        String command = ByteUtils.bytesToHexString(bytes);
                        if (command.length() >= 40) {
                            String advertiseStr = command.substring(10, 40);
                            String deviceId = advertiseStr.substring(6, 30);
                            bleDevice.setDeviceId(deviceId);
                            String hasData = advertiseStr.substring(4, 6);
                            if (hasData.equals("02")) {
                                bleDevice.setHasData(true);
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
            token = PreferenceManager.getDefaultSharedPreferences(context).getString(TokenKey, "");
            return !token.isEmpty();
        }
    }


    public void startVerify(String emailText, LockyDataCallback callback) {
        String email = emailText.trim();
        if (email.isEmpty()) {
            return;
        }
        Call<Void> call = ApiAuthManager.getInstance().getHttpApi().startVerify(email, domain);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.v(TAG, "success");
                callback.onSuccess(true);
                // successful
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.v(TAG, "error");
                callback.onFailure();
                // fail
            }
        });
    }

    public void verify(String email, String code, LockyDataCallback callback) {
        Call<TokenModel> call = ApiAuthManager.getInstance().getHttpApi().verify(email, code, domain);
        call.enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                TokenModel tokenModel = response.body();

                if (tokenModel != null && !tokenModel.getToken().isEmpty()) {
                    token = tokenModel.getToken();
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(TokenKey, token).apply();
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

    public void getAllLocks(LockyListCallback callback) {
        if (token.isEmpty()) {
            token = PreferenceManager.getDefaultSharedPreferences(context).getString(TokenKey, "");
            if (token.isEmpty()) {
                return;
            }
        }
        lockListCallback = callback;
        Call<ArrayList<String>> call = ApiAuthManager.getInstance().getHttpApi().getMobileKeys(domain, token);
        call.enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                ArrayList<String> keyList = (ArrayList<String>) response.body();
                if (keyList == null) {
                    callback.onSuccess(null);
                } else {
                    ArrayList<LockyMobileKey> keys = new ArrayList<LockyMobileKey>();
                    for (String item : keyList) {
                        if (item.length() > 24) {
                            String tenantId = item.substring(0, 24);
                            String token = item.substring(24, item.length());
                            keys.add(new LockyMobileKey(tenantId, token));
                        }
                    }
                    mobileKeys = keys;
                    if (keys.size() > 0) {
                        ArrayList<LockModel> dataList = new ArrayList<LockModel>();
                        mobileKeyIndex = 0;
                        for (LockyMobileKey item : keys) {
                            getAllLockItem(item.getTenantId(), item.getToken(), new LockyListCallback() {
                                @Override
                                public void onSuccess(ArrayList response) {
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

    private void handleLocks(ArrayList<LockModel> dataList, LockyListCallback callback) {
        lockList = dataList;
        startScanDevice();
        ArrayList<LockDevice> items = new ArrayList<LockDevice>();
        for (LockModel lock : dataList) {
            LockDevice device = new LockDevice();
            device.setId(lock.getId());
            device.setName(lock.getName());
            items.add(device);
        }

        callback.onSuccess(items);
    }


    private void getAllLockItem(String tenantId, String token, LockyListCallback callback) {
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

    public void downloadPackage(String signal, String deviceId, String tenantId, String token, LockyDataCallback callback) {
        Call<LockyPackage> call = ApiManager.getInstance().getHttpApi().downloadPackage(signal, deviceId, tenantId, token);
        call.enqueue(new Callback<LockyPackage>() {
            @Override
            public void onResponse(Call<LockyPackage> call, Response<LockyPackage> response) {

            }

            @Override
            public void onFailure(Call<LockyPackage> call, Throwable t) {

            }
        });
    }

    public void messageDelivered(String deviceId, LockyPackage payload, String tenantId, String token, LockyDataCallback callback) {
        Call<Void> call = ApiManager.getInstance().getHttpApi().messageDelivered(deviceId, payload, "application/json", tenantId, token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void connectGatt(BleDevice bleDevice) {
        BluetoothDevice device = bleDevice.getDevice();
        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        isConnected = true;
                        Log.d(TAG, "connect successfully");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        isConnected = false;
                        Log.d(TAG, "disconnected");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * disconnect
     */
    @SuppressLint("MissingPermission")
    private void disconnectDevice() {
        if (isConnected && bluetoothGatt != null) {
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
            for (BleDevice device : deviceList) {
                device.setRssi(bleDevice.getRssi());
            }
            return;
        }
        ArrayList<LockDevice> items = new ArrayList<LockDevice>();
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
    public void startScanDevice() {
        if (isScanning == true)return;
        isScanning = true;
        deviceList.clear();

        final ArrayList<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SERVICE_UUID)).build();
        scanFilters.add(scanFilter);
        BluetoothLeScannerCompat.getScanner().startScan(scanFilters, null, scanCallback);
    }

    /**
     * stop scan
     */
    public void stopScanDevice() {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
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
            //检查蓝牙是否打开
            openBluetooth();
        }
    }

    /**
     * request permission
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_CODE)
    private void requestPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,};
        if (EasyPermissions.hasPermissions(context, perms)) {
            openBluetooth();
        }
    }

    /**
     * open bluetooth
     */
    public void openBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {//是否支持蓝牙
            if (bluetoothAdapter.isEnabled()) {//打开
                supportBluetooth = true;
            }
        }
    }

}
