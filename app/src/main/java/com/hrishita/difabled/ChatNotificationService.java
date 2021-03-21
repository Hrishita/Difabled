package com.hrishita.difabled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ChatNotificationService extends Service
{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String sender = intent.getStringExtra("sender");
        String message = intent.getStringExtra("chat_message");

        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 2021, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "jid");
        builder
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Message from " + sender)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                    .setFullScreenIntent(receiveCallPendingIntent,true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true);

        Notification incomingCallNotification = null;
        incomingCallNotification = builder.build();

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(2021, incomingCallNotification);

        return START_NOT_STICKY;
    }
}
