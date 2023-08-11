package com.yeonfish.hybrid_app_frame.common.push;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import com.yeonfish.hybrid_app_frame.R;
import com.yeonfish.hybrid_app_frame.common.activity.IntroActivity;
import com.yeonfish.hybrid_app_frame.common.constant.CommonConstant;


/**
 * Created by yeonfish6040 on 10, april, 2023
 */

public class PushService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("PushService", "onNewToken: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        if (remoteMessage == null) return;

        if (data.get("event").equals("notification")) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            String CHANNEL_ID = !data.containsKey("channel_id") ? "app_frame": data.get("channel_id");
            String CHANNEL_NAME = !data.containsKey("channel_name") ? "app_frame": data.get("channel_name");

            NotificationCompat.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            } else {
                builder = new NotificationCompat.Builder(getApplicationContext());
            }

            String title = null;
            String body = null;
            String url = null;
            try {
                JSONObject notificationData = new JSONObject(data.get("body"));
                title = notificationData.getString("title");
                body = notificationData.getString("text");
                url = notificationData.getString("url");
            } catch (JSONException e) { Log.e(e.getMessage(), e.toString()); }

            Intent notificationIntent = new Intent(this, IntroActivity.class);
            if (url != null) {
                notificationIntent.putExtra(CommonConstant.INTENT_NOTIFICATION_URL, url);
            }

            PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            builder.setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(notificationPendingIntent)
                    .setAutoCancel(true);

            Notification notification = builder.build();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(1, notification);
        }
    }
}