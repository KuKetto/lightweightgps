package pe.saydomonkos.lightweightgps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.sql.Timestamp;

public class LocationService extends Service {
    private Context context;
    private LocationHandler locationHandler;
    private IBinder ibinder;
    private Timestamp startTime;
    private Utils utils;

    public void stop() {
        locationHandler.stop(startTime);
        stopForeground(true);
        locationHandler = null;
    }

    public String getLocationState() {
        return locationHandler.getCurrentState();
    }

    public void locate() {
        locationHandler = new LocationHandler(this.context);
        startTime = utils.getTimestamp();
        locationHandler.start(startTime);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.context = this;
        this.utils = new Utils();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String CHANNEL_ID = "1";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.gemba)
                .setPriority(Notification.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle("Lightweight GPS")
                .setContentText("Tracking your position \uD83D\uDC40")
                .setTicker("TICKER")
                .setChannelId(CHANNEL_ID)
                .setVibrate(new long[]{0L})
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "pe.saydomonkos.lightweightgps", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Tracking location in the background");
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1337, notification);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        ibinder = new LocalBinder();
        return ibinder;
    }

    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of MyService so clients can call public methods
            return LocationService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
