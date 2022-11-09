package com.linhua.locky;

import java.util.ArrayList;

public interface LockyLocksCallback<T> {

    void onResponse(ArrayList<T> response);

    void onFailure();
}
