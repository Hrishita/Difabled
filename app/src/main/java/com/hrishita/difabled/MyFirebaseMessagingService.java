package com.hrishita.difabled;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        System.out.println("hello");
        String type = remoteMessage.getData().get("type");

        if (type != null) {
            if(type.equals(VIDEO_CALL_CONSTANTS.VIDEO_CALL_START)) {
                //someone is calling user show notification

                String caller_name = remoteMessage.getData().get("caller");
                String video_call_id = remoteMessage.getData().get("video_call_id");
                Intent service = new Intent(getBaseContext(), MyService.class);
                Bundle bundle = new Bundle();
                bundle.putString("caller", caller_name);
                bundle.putString("video_call_id", video_call_id);
                service.putExtras(bundle);
                service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                service.setAction("ACTION_START_MY_SERVICE");
                MyFirebaseMessagingService.this.startService(service);
            } else if(type.equals(VIDEO_CALL_CONSTANTS.VIDEO_CALL_END)) {
                //receiver ended video call
                System.out.println("ending call");
                Intent i = new Intent("MyData");
                i.putExtra("message", "call ended");
                broadcastManager.sendBroadcast(i);
            } else if(type.equals(VIDEO_CALL_CONSTANTS.VIDEO_CALL_ANSWER)) {
                //receiver picked up call
                System.out.println("ANSWER");
                Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
                String video_call_id = remoteMessage.getData().get("video_call_id");
                i.putExtra("video_call_id", video_call_id);
                i.setAction("CALLER");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            else if(type.equals(VIDEO_CALL_CONSTANTS.VIDEO_CALL_REJECT_BOTH))
            {
                System.out.println("ENDING CALL FOR BOTH ENDS");
                //caller ends call
                Intent i = new Intent(getApplicationContext(), MyService.class);
                i.setAction("ACTION_STOP_MY_SERVICE");
                startService(i);
//                i.setAction("ACTION_STOP_MY_SERVICE");
//                startService(i);
            }
            else if(type.equals(VIDEO_CALL_CONSTANTS.CHAT_MESSAGE_RECVD))
            {
                Intent i = new Intent(getApplicationContext(), ChatNotificationService.class);
                i.putExtra("chat_message", remoteMessage.getData().get("chat_message"));
                i.putExtra("sender", remoteMessage.getData().get("sender"));
                startService(i);
            }
        }


    }

    @Override
    public void onNewToken(@NonNull String s) {
        System.out.println(s);
    }
}
