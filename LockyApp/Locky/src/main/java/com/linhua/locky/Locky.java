package com.linhua.locky;

import static com.linhua.locky.utils.AppMgr.context;

import android.preference.PreferenceManager;
import android.util.Log;

import com.linhua.locky.api.ApiAuthManager;
import com.linhua.locky.api.ApiManager;
import com.linhua.locky.bean.LockDevice;
import com.linhua.locky.bean.LockModel;
import com.linhua.locky.bean.LockyMobileKey;
import com.linhua.locky.bean.TokenModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

enum EventType {
    DiscoveredDevice,       // it has discovered the device
    ConnectingDevice,       // it is connecting the device
    DidConnectDevice,       // it has connected the device
    DisConnectDevice,       // it disconnects the device
    WritingDevice,          // it is writing to the device
    DidWriteDevice,         // it has written to the device
    FailureWriteDevice,     // it fails to write to the device
    DownloadPackage,        // it is downloading package for the device
    DeliveringMessage,      // messge is delivering
    MessageDelivered,       // the message is delivered
}

interface LockyEventCallback<T> {
    void postEvent(EventType event);
}

public class Locky {
    private String TAG = "Locky";
    private String domain = "mobilekey";
    private String token = "";
    private final String TokenKey = "locky_token";
    private ArrayList<LockyMobileKey> mobileKeys;
    private ArrayList<LockModel> lockList;
    private long mobileKeyIndex;

    public Boolean isAuthenticated() {
        if (!token.isEmpty()) {
            return true;
        } else {
            token = PreferenceManager.getDefaultSharedPreferences(context).getString(TokenKey, "");
            return !token.isEmpty();
        }
    }


    public void startVerify(String emailText, LockyEmailCallback callback) {
        String email = emailText.trim();
        if (email.isEmpty()) {
            return;
        }
        Call<Void> call = ApiAuthManager.getInstance().getHttpApi().startVerify(email, domain);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.v(TAG, "success");
                callback.onResponse(true);
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

    public void verify(String email, String code, LockyTokenCallback callback) {
        Call<TokenModel> call = ApiAuthManager.getInstance().getHttpApi().verify(email, code, domain);
        call.enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                 TokenModel tokenModel = response.body();

                 if (tokenModel != null && !tokenModel.getToken().isEmpty()) {
                     token = tokenModel.getToken();
                     PreferenceManager.getDefaultSharedPreferences(context).edit().putString(TokenKey, token).commit();
                     callback.onResponse(token);
                 } else {
                     callback.onResponse("");
                 }
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    public  void getAllLocks(LockyLocksCallback callback) {
        if (!token.isEmpty()) {
            return;
        } else {
            token = PreferenceManager.getDefaultSharedPreferences(context).getString(TokenKey, "");
            if (token.isEmpty()) {
                return;
            }
        }
        Call<ArrayList<String>> call = ApiAuthManager.getInstance().getHttpApi().getMobileKeys(domain, token);
        call.enqueue(new Callback<ArrayList<String>>() {

            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                ArrayList<String> keyList = (ArrayList<String>) response.body();
                if (keyList == null) {
                    callback.onResponse(null);
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
                        ArrayList<LockModel>dataList = new ArrayList<LockModel>();
                        mobileKeyIndex = 0;
                        for (LockyMobileKey item : keys) {
                            getAllLockItem(item.getTenantId(), item.getToken(), new LockyLocksCallback() {
                                @Override
                                public void onResponse(ArrayList response) {
                                    if (response != null) {
                                        dataList.addAll(response);
                                    }
                                    mobileKeyIndex++;
                                    if (mobileKeyIndex >= dataList.size()) {
                                        handleLocks(dataList, callback);
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    mobileKeyIndex++;
                                    if (mobileKeyIndex >= dataList.size()) {
                                        handleLocks(dataList, callback);
                                    }
                                }
                            });
                        }

                    } else {
                        callback.onResponse(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    private void handleLocks(ArrayList<LockModel>dataList, LockyLocksCallback callback) {
        lockList = dataList;
        ArrayList items = new ArrayList<LockDevice>();
        for (LockModel lock : dataList) {
            LockDevice device = new LockDevice();
            device.setId(lock.getId());
            device.setName(lock.getName());
            items.add(device);
        }
        callback.onResponse(items);
    }


    private void getAllLockItem(String tenantId, String token, LockyLocksCallback callback) {
        Call<ArrayList<LockModel>> call = ApiManager.getInstance().getHttpApi().getAllLocks(tenantId, token);
        call.enqueue(new Callback<ArrayList<LockModel>>() {
            @Override
            public void onResponse(Call<ArrayList<LockModel>> call, Response<ArrayList<LockModel>> response) {
                ArrayList<LockModel> dataList = response.body();
                for (LockModel lock : dataList) {
                    lock.setToken(token);
                    lock.setTenantId(tenantId);
                }
                callback.onResponse(dataList);
            }

            @Override
            public void onFailure(Call<ArrayList<LockModel>> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

}
