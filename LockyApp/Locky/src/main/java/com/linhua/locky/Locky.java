package com.linhua.locky;

import static com.linhua.locky.utils.AppMgr.context;

import android.preference.PreferenceManager;
import android.util.Log;

import com.linhua.locky.api.ApiAuthManager;
import com.linhua.locky.api.ApiManager;
import com.linhua.locky.bean.LockDevice;
import com.linhua.locky.bean.LockModel;
import com.linhua.locky.bean.LockyMobileKey;
import com.linhua.locky.bean.LockyPackage;
import com.linhua.locky.bean.TokenModel;
import com.linhua.locky.callback.LockyDataCallback;
import com.linhua.locky.callback.LockyListCallback;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                     PreferenceManager.getDefaultSharedPreferences(context).edit().putString(TokenKey, token).commit();
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

    public  void getAllLocks(LockyListCallback callback) {
        if (token.isEmpty()) {
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
                        ArrayList<LockModel>dataList = new ArrayList<LockModel>();
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

    private void handleLocks(ArrayList<LockModel>dataList, LockyListCallback callback) {
        lockList = dataList;
        ArrayList items = new ArrayList<LockDevice>();
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

    public void messageDelivered(String deviceId, LockyPackage payload,String tenantId, String token, LockyDataCallback callback) {
        Call<Void> call = ApiManager.getInstance().getHttpApi().messageDelivered(deviceId, payload, "application/json",tenantId, token)
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
