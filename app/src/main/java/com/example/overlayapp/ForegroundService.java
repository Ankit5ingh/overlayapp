package com.example.overlayapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.PixelFormat;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service implements CallBackListener{
    Window window;
    DraggableTouchListener touchListener;
    int secondsLeft;
    Timer timer = null;
    TextView tv;
    Handler handler;
    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground();
        else startForeground(1, new Notification());
        window = new Window(this, R.layout.popup_window);
        touchListener = new DraggableTouchListener(ForegroundService.this, window.mView, new Point(window.getParams().x, window.getParams().y), this);
        window.open();
        tv = (TextView) window.mView.findViewById(R.id.timer);
        handler = new Handler(Looper.getMainLooper());
        scheduleTimer();
        clickListener();
    }

    private void clickListener() {
        window.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForegroundService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        secondsLeft = intent.getIntExtra("timer", 60);
        if (intent != null && "STOP_SERVICE_ACTION".equals(intent.getAction())) {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        window.close();
    }

    // for android version >=O we need to create
    // custom notification stating
    // foreground service is running
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent cancelIntent = new Intent(this, ForegroundService.class);
        cancelIntent.setAction("STOP_SERVICE_ACTION");
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Service running")
                .setContentText("Displaying over other apps")
                // this is important, otherwise the notification will show the way
                // you want i.e. it will show some default notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_close_white_24, "cancel", pendingIntent)
                .build();
        startForeground(2, notification);
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

    private static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public void onPositionChanged(int x, int y) {
        window.setPosition(x, y);
    }
}

