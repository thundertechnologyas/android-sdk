package com.linhua.locky;

public interface LockyTokenCallback<T> {

    void onResponse(T token);

    void onFailure();
}
