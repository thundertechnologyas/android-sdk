package com.linhua.locky;

import java.util.List;

public interface LockyEmailCallback<T> {

    void onResponse(T response);

    void onFailure();
}

