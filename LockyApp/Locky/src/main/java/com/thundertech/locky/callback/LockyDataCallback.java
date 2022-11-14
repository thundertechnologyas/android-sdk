package com.thundertech.locky.callback;

public interface LockyDataCallback<T> {

    void onSuccess(T response);

    void onFailure();
}

