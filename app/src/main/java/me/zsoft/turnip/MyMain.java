package me.zsoft.turnip;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by mikezank on 6/19/16.
 */
public class MyMain extends MainActivity {

    public Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(Constants.TAG, String.format("Handler.handleMessage(): msg=%s", msg));
            // This is where main activity thread receives messages
            // Put here your handling of incoming messages posted by other threads
            super.handleMessage(msg);
        }

    };
}
