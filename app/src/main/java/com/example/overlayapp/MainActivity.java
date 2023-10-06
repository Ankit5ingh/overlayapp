package com.example.overlayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Intent intent;
    private Timer timer = null;
    private int secondsLeft = 60;
    TextView tv;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, ForegroundService.class);
        checkOverlayPermission();
        tv = (TextView) findViewById(R.id.main_timer);
        handler = new Handler(Looper.getMainLooper());
        scheduleTimer();
    }

    // method for starting the service
    public void startService(){
        intent.putExtra("timer", secondsLeft);
        intent.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(Settings.canDrawOverlays(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
                else startService(intent);
            }
            else checkOverlayPermission();
        }
        else startService(intent);
    }

    public void stopForeService(){
        stopService(intent);
    }

    private boolean isServiceRunning(){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : l) {
            if (runningServiceInfo.service.getClassName().equals("ForegroundService")) {
                serviceRunning = true;
            }
        }
        return serviceRunning;
    }


    public void checkOverlayPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
    }

    private void scheduleTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (secondsLeft > 0) {
                        secondsLeft--;
                        updateTimerOnUiThread();
                    } else {
                        timer.cancel();
                        timer = null;
                    }
                }
            }, 1000, 1000);
        }
    }

    private void updateTimerOnUiThread() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateTimer();
            }
        });
    }

    private void updateTimer() {
        tv.setText(formatTime(secondsLeft));
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }


    @Override
    protected void onResume() {
        super.onResume();
        stopForeService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopForeService();
    }
}
