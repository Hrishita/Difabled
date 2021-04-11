package com.hrishita.difabled;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service {
    private int ReceiveCallRequestCode = 9090;
    private int RejectCallRequestCode = 9091;
    private int MuteCallRequestCode = 9092;


    private int notificationID;
    private Ringtone defaultRingtone;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals("ACTION_START_MY_SERVICE")) {
            startMe(intent.getExtras().getString("caller"), intent.getExtras().getString("video_call_id"));
        }
        else if(intent.getAction().equals("ACTION_STOP_MY_SERVICE")) {
            if(intent.getExtras()==null)
            {
                stopMe();
            }
            else
                stopMe(intent.getExtras().getString("video_call_id"), intent.getExtras().getString("caller"));
        }
        else if(intent.getAction().equals("ACTION_MUTE_MY_SERVICE")) {
            muteMe();
        }

        return START_NOT_STICKY;
    }

    private void stopMe() {
        stopForeground(true);
        stopSelf();
        if(defaultRingtone.isPlaying()) {
            defaultRingtone.stop();
        }
    }

    private void startMe(String caller, String video_call_id) {
        try {

            notificationID = 555;

            System.out.println("service started");
            System.out.println(video_call_id + "vcall");
            Intent receiveCallAction = new Intent(getApplicationContext(), WebViewActivity.class);
            receiveCallAction.setAction("RECEIVER");
            receiveCallAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            receiveCallAction.putExtra("caller", caller);
            receiveCallAction.putExtra("video_call_id", video_call_id);

            PendingIntent receiveCallPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent answerIntent = new Intent(getApplicationContext(), WebViewActivity.class);
            answerIntent.setAction("ANSWER");

            Intent rejectIntent = new Intent(getApplicationContext(), RejectOrMuteListener.class);
            Intent muteIntent = new Intent(getApplicationContext(), RejectOrMuteListener.class);

            rejectIntent.setAction("com.hrishita.difabled.REJECT");
            muteIntent.setAction("com.hrishita.difabled.MUTE");

            Bundle bundle1 = new Bundle();
            Bundle bundle2 = new Bundle();


            bundle1.putInt("notificationID", notificationID);
            bundle2.putInt("notificationID", notificationID);

            bundle1.putString("video_call_id", video_call_id);
            bundle1.putString("caller", caller);

            bundle2.putString("video_call_id", video_call_id);
            bundle2.putString("caller", caller);

            rejectIntent.putExtras(bundle1);
            muteIntent.putExtras(bundle2);

            PendingIntent pendingIntentReject = PendingIntent.getBroadcast(getApplicationContext(),RejectCallRequestCode, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntentMute = PendingIntent.getBroadcast(getApplicationContext(),MuteCallRequestCode, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
/*

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
*/




            Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
            defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtoneUri);
            defaultRingtone.play();

//            Intent ringtoneIntent = new Intent(getApplicationContext(),RingtoneService.class);
//            ringtoneIntent.setAction("ACTION_START_RINGTONE");
//            startService(ringtoneIntent);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "jid");
            builder
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setContentTitle("VIDEO CALL")
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                    .setContentIntent(receiveCallPendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setContentText(caller)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
//                    .setFullScreenIntent(receiveCallPendingIntent,true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(caller))
                    .setOngoing(true)
                    .addAction(R.drawable.common_full_open_on_phone,"Answer", receiveCallPendingIntent)
                    .addAction(R.drawable.common_full_open_on_phone,"Reject", pendingIntentReject)
                    .addAction(R.drawable.common_full_open_on_phone,"Mute", pendingIntentMute)
                    .setAutoCancel(true);



            Notification incomingCallNotification = null;
            incomingCallNotification = builder.build();

            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            mNotifyMgr.notify(notificationID, incomingCallNotification);
            startForeground(notificationID, incomingCallNotification);

//            startForeground(132131, incomingCallNotification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMe(final String video_call_id, final String phoneNumber) {
        VideoCallInteractionTask2 task2 = new VideoCallInteractionTask2();
        task2.execute(video_call_id, phoneNumber);
//        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
//            @SuppressLint("StaticFieldLeak")
//            @Override
//            protected String doInBackground(String... strings) {
//                @SuppressLint("StaticFieldLeak")
//
//                String url = "https://fcm.googleapis.com/fcm/send";
//                String jsonBody = "{\n" +
//                        "\"to\": \"/topics/"+ receiver.substring(1) +"\",\n" +
//                        "\"priority\": \"high\",\n" +
//                        "\"data\": {\n" +
//                        "\"caller\": \""+caller+"\",\n" +
//                        "\"receiver\": \""+receiver+"\",\n" +
//                        "\"video_call_id\": \""+videocallid+"\",\n" +
//                        "\"type\": \"VIDEO_CALL_START\"\n" +
//                        "}\n" +
//                        "}\n" +
//                        "\n";
//
//                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postData);
//                Request request = new Request.Builder().url(url).post(body).build();
//                OkHttpClient client = new OkHttpClient();
//                try {
//                    client.newCall(request).execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//        };

//        VideoCallInteractionTask task = new VideoCallInteractionTask(new VideoCallInteractionTask.VideoCallInteractionInterface() {
//            @Override
//            public void success(Response response) {
//
//            }
//
//            @Override
//            public void failure() {
//
//            }
//        });
//        String jsonBody = "{" +
//                "\"video_call_id\":\"" + video_call_id + "\"," +
//                "\"ending_party\": \"" + phoneNumber + "\"" +
//                "}";
//        task.execute("https://difabledwebservicefinal.herokuapp.com/api/endCall", jsonBody);
        stopForeground(true);
        stopSelf();
        if(defaultRingtone.isPlaying()) {
            defaultRingtone.stop();
        }
    }

    private void muteMe() {
        if(defaultRingtone.isPlaying()) {
            defaultRingtone.stop();
        }
    }

    public static class VideoCallInteractionTask2 extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings) {
            String videocallid = strings[0];
            String receiver = strings[1];

            String url = "https://fcm.googleapis.com/fcm/send";
            String jsonBody = "{" +
                    "\"to\": \"/topics/"+ receiver.substring(1) +"\",\n" +
                    "\"priority\": \"high\",\n" +
                    "\"data\": {\n" +
                    "\"receiver\": \""+receiver+"\",\n" +
                    "\"video_call_id\": \""+videocallid+"\",\n" +
                    "\"type\": \"VIDEO_CALL_END\"\n" +
                    "}," +
                    "\"ttl\":\"0s\"" +
                    "}";

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
            Request request = new Request.Builder()
                    .addHeader("Authorization", "key=AAAAs9qrN6Q:APA91bFaMrQ-tnqvhNxH2QhJ4f1Og1wmICZm5V0aMnHwf4wU9MqS3Yeh1uc5yahScYvaSuPyF-D_s1P56Pj2mpjVKB5I-lAUcSje9VsFOQjXVaQtl8I10eaUo3JMmPuT-h9OUe2EZEa8")
                    .addHeader("Content-Type", "application/json")
                    .url(url).post(body).build();
            OkHttpClient client = new OkHttpClient();
            try {
                Response response = client.newCall(request).execute();
                System.out.println(response.body().string() + " Response Of FCM");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
