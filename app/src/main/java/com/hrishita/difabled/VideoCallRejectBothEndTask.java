package com.hrishita.difabled;

import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoCallRejectBothEndTask extends AsyncTask<String, Void, Void>
{
    //purpose
    //send an FCM to other end that will remove notification

    @Override
    protected Void doInBackground(String... strings) {
        String receiver = strings[0];
        String videocallid = strings[1];
//        String uid = strings[2];

        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        String jsonBody = "{" +
                "\"to\": \"/topics/"+ receiver.substring(1) +"\",\n" +
                "\"priority\": \"high\",\n" +
                "\"data\": {\n" +
                "\"receiver\": \""+receiver+"\",\n" +
                "\"video_call_id\": \""+videocallid+"\",\n" +
                "\"type\": \"VIDEO_CALL_REJECT_BOTH\"\n" +
                "}," +
                "\"ttl\":\"0s\"" +
                "}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        System.out.println(jsonBody + " both reject");

        Request request = new Request.Builder()
                .addHeader("Authorization", "key=AAAA0ZGEB6w:APA91bEDDZnlpiqmnBWogfL_Lg-Q8kfZDd0CdiqpGCaS62h3_xvQ2Tw2P39BzJZVmzsxCniBgpe1-fn1t0q-kNa7o31ffkcYCjBND3W32Q63m1qKyWF-vDCY3dbxeOba0SAVb6BtLIZh")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .url(url)
                .build();

        try {
            Response res =  client.newCall(request).execute();
            System.out.println(res.body().string() + " both reject 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
