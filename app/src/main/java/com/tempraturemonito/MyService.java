package com.tempraturemonito;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tempraturemonitor.R;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service implements batReceiver.MyBroadcastListener {
    public static final String CHANNEL_ID = "10001";
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    int Your_X_SECS = 5;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    int temp;
    float far_temp;
    String message = "Mobile Temperature is ", msg2;
    batReceiver bat;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        bat = new batReceiver(this);

        getApplication().registerReceiver(bat,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopTimerTask();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000); //
    }

    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        createNotification();
                    }
                });
            }
        };
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        createNotificationChannel();
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(message)
                .setOngoing(true)
                .setContentText(msg2)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        ;

        builder.setColor(Color.RED);


        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.

        mNotificationManager.notify(1, builder.build());

//        notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(1, builder.build());
    }

    @Override
    public void tempChanged(float t, float l, boolean isC, float far) {
        temp = (int) t;
        far_temp =(float) far;

        if (temp >= 45) {
            Toast.makeText(getApplicationContext(), "Phone is Overheating", Toast.LENGTH_SHORT).show();
        }

        if (l == 100) {
            Toast.makeText(getApplicationContext(), "Phone is Fully charged", Toast.LENGTH_SHORT).show();
        }

        message = "Mobile Temperature is " + t + "\u2103";
        if (isC) {
            msg2 = "\n Charger Plugged!!";
        } else {
            msg2 = null;
        }
        Log.e(TAG, "tempChanged: " + message);
//        notificationManager.notify(1, builder.build());
    }
}