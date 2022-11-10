package com.linhua.locky.callback;

public interface LockyDataCallback<T> {

    void onSuccess(T response);

    void onFailure();
}

