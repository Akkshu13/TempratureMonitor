package com.tempraturemonito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class batReceiver extends BroadcastReceiver {

    public float temp, level,far;
    public MyBroadcastListener listener;
    public boolean isCharging;
    Context context;

    public batReceiver(MyBroadcastListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        this.context = c;

        temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 2);
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 2);

        isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                BatteryManager.BATTERY_STATUS_CHARGING;


        Log.e("br temp ", String.valueOf(temp) + "\n is charging " + isCharging);
        temp = temp / 10;
        far = temp*9/5+32;
        listener.tempChanged(temp, level, isCharging,far);
    }


    public interface MyBroadcastListener {

        void tempChanged(float temp, float level, boolean isCharging, float f);
    }
}



