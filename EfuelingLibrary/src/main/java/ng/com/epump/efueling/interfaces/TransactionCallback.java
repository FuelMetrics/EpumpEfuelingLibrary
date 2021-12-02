package ng.com.epump.efueling.interfaces;

import android.content.Intent;

public interface TransactionCallback {
    void onStarted();
    void onCompleted(int resultCode, Intent intent);
}
