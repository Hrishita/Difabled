package com.hrishita.difabled;


import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendVideoCallFcMNotificationTask extends AsyncTask<String, Void, String>
{
    Context context;
    private SendVideoCallTaskCallback mInterface;
    SendVideoCallFcMNotificationTask(Context context, SendVideoCallTaskCallback mCallback)
    {
        this.context = context;
        this.mInterface = mCallback;
    }
    @Override
    protected String doInBackground(String... strings) {
        String caller = strings[0];
        String receiver = strings[1];
        String videocallid = strings[2];
//        String uid = strings[2];

        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        String jsonBody = "{\n" +
                "\"to\": \"/topics/"+ receiver.substring(1) +"\",\n" +
                "\"priority\": \"high\",\n" +
                "\"data\": {\n" +
                "\"caller\": \""+caller+"\",\n" +
                "\"receiver\": \""+receiver+"\",\n" +
                "\"video_call_id\": \""+videocallid+"\",\n" +
                "\"type\": \"VIDEO_CALL_START\"\n" +
                "}," +
                "\"ttl\":\"0s\"" +
                "}\n" +
                "\n";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        Request request = new Request.Builder()
                .addHeader("Authorization", "key=AAAAs9qrN6Q:APA91bFaMrQ-tnqvhNxH2QhJ4f1Og1wmICZm5V0aMnHwf4wU9MqS3Yeh1uc5yahScYvaSuPyF-D_s1P56Pj2mpjVKB5I-lAUcSje9VsFOQjXVaQtl8I10eaUo3JMmPuT-h9OUe2EZEa8")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .url(url)
                .build();

        try {
            //response format {"message_id":19_char_int}
            Response res = client.newCall(request).execute();
            String jsonOp = res.body().string();
            try {
                JSONObject jsonObject = new JSONObject(jsonOp);
                String messageId = jsonObject.getString("message_id");
                mInterface.onVideoCallDialSuccess();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                mInterface.onVideoCallDialFailed();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    public interface SendVideoCallTaskCallback
    {
        void onVideoCallDialSuccess();
        void onVideoCallDialFailed();
    }
}
